package nu.ndw.nls.accessibilitymap.trafficsignclient.repositories;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.async.ByteArrayFeeder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractor;
import reactor.core.publisher.Flux;

/**
 * This source of the following code: https://gist.github.com/HaloFour/148002a3da8e8b5b48c6a55a548a3b97/revisions
 * Implementation of a {@link BodyExtractor} that can asynchronously parse and scan into a JSON document to find an
 * array or object at a specified {@link JsonPointer} and deserialize each entity at that location without buffering the
 * entire response payload into memory.
 * <p>
 * Given the following JSON docuent:
 * <pre>{@code
 * {
 *     "resolveChannelsResponse": {
 *         "channels": [
 *             {
 *                 "channelId": "merlin:linear:channel:5903704021496414104"
 *             },
 *             {
 *                 "channelId": "merlin:linear:channel:8692448035122410104"
 *             }
 *         ],
 *         "version": "123"
 *     }
 * }
 * }</pre>
 * <p>
 * You can use the extractor as follows:
 *
 * <pre>{@code
 * return client.post()
 *         .uri(serviceDiscovery.getServiceEndpoint(ServiceTypes.GRID_WS), builder -> {
 *             builder = withCommonComponents(builder);
 *             return builder.build();
 *         })
 *         .contentType(MediaType.APPLICATION_JSON)
 *         .bodyValue(Map.of("resolveChannels", request))
 *         .accept(MediaType.APPLICATION_JSON)
 *         .exchange()
 *         .flatMapMany(response -> {
 *             if (response.statusCode().isError()) {
 *                 return response.createException();
 *             }
 *             return response.body(streamingJsonBodyExtractor.toFlux(ChannelInfo.class,
 *                     JsonPointer.compile("/resolveChannelsResponse/channels")))
 *         });
 * }</pre>
 * <p>
 * The result will be a {@link Flux} with each element emitted as it is parsed and deserialized.
 */
@Component
public class StreamingJsonBodyExtractor {

    private final ObjectMapper mapper;

    /**
     * Creates an instance of the {@link StreamingJsonBodyExtractor} for the configured {@link ObjectMapper}
     *
     * @param mapper the {@link ObjectMapper}
     */
    public StreamingJsonBodyExtractor(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Extractor to stream deserialized entities from a JSON pointer to a node within the response.
     *
     * @param type    the {@link Class} of the element type to decode to
     * @param pointer the {@link JsonPointer} to the nested location within the JSON body to start streaming
     * @param <T>     the element type to decode to
     * @param <M>     the type of the HTTP input message
     * @return the {@link BodyExtractor} to stream the entities into a {@link Flux}
     */
    public <T, M extends ReactiveHttpInputMessage> BodyExtractor<Flux<T>, M> toFlux(
            Class<T> type,
            JsonPointer pointer) {

        return toFlux(ResolvableType.forClass(type), pointer);
    }

    /**
     * Extractor to stream deserialized entities from a JSON pointer to a node within the response.
     *
     * @param type    the {@link ResolvableType} of the element type to decode to
     * @param pointer the {@link JsonPointer} to the nested location within the JSON body to start streaming
     * @param <T>     the element type to decode to
     * @param <M>     the type of the HTTP input message
     * @return the {@link BodyExtractor} to stream the entities into a {@link Flux}
     */
    public <T, M extends ReactiveHttpInputMessage> BodyExtractor<Flux<T>, M> toFlux(
            ResolvableType type,
            JsonPointer pointer) {

        return (inputMessage, context) -> extract(inputMessage, type, pointer);
    }

    /**
     * Extracts the streaming entities from the given message.
     *
     * @param inputMessage the request to extract from
     * @param type         the {@link ResolvableType} of the element type to decode to
     * @param pointer      the {@link JsonPointer} to the nested location within the JSON body to start streaming
     * @param <T>          the element type to decode to
     * @param <M>          the type of the HTTP input message
     * @return the {@link Flux} of the entities being streamed
     */
    public <T, M extends ReactiveHttpInputMessage> Flux<T> extract(M inputMessage,
            ResolvableType type, JsonPointer pointer) {

        return extract(inputMessage.getBody(), type, pointer);
    }

    /**
     * Extracts the streaming entities from the data buffers.
     *
     * @param dataBuffers the streaming body
     * @param <T>         the element type to decode to
     * @return the {@link Flux} of the entities being streamed
     */
    private <T> Flux<T> extract(Flux<DataBuffer> dataBuffers, ResolvableType type, JsonPointer pointer) {
        try {
            var factory = mapper.getFactory();
            var parser = factory.createNonBlockingByteArrayParser();
            var feeder = (ByteArrayFeeder) parser.getNonBlockingInputFeeder();
            Supplier<TokenBuffer> tokenBufferSupplier = () -> new TokenBuffer(parser,
                    mapper.getDeserializationContext());

            var tokenizer = new StreamingTokenizer(parser, feeder, tokenBufferSupplier, pointer);

            return dataBuffers.concatMap(tokenizer::tokenize).concatWith(tokenizer.endOfInput())
                    .map(deserialize(mapper, type));
        } catch (Exception exception) {
            return Flux.error(exception);
        }
    }

    /**
     * Returns a function to deserialize the buffered tokens into an entity
     *
     * @param mapper the {@link ObjectMapper} used to deserialize the entities
     * @param <T>    the element type to decode to
     * @return the {@link Function} that deserializes each {@link TokenBuffer}
     */
    private <T> Function<TokenBuffer, T> deserialize(ObjectMapper mapper, ResolvableType type) {
        var javaType = mapper.getTypeFactory().constructType(type.getType());
        return tokenBuffer -> {
            var parser = tokenBuffer.asParser();
            try {
                return mapper.readValue(parser, javaType);
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        };
    }

    /**
     * Helper state machine class that tracks the current state and position as it parses through the {@link JsonToken}
     * extracted from the {@link JsonParser} and fills a {@link TokenBuffer} with the complete graph for each entity to
     * be deserialized.
     */
    private static final class StreamingTokenizer {

        private final JsonParser parser;
        private final ByteArrayFeeder feeder;
        private final Supplier<TokenBuffer> tokenBufferSupplier;
        private final String[] segments;
        private final String[] actual;
        private int depth;
        private ParserState state = ParserState.BEFORE_STREAM;
        private TokenBuffer currentTokenBuffer;

        StreamingTokenizer(JsonParser parser,
                ByteArrayFeeder feeder,
                Supplier<TokenBuffer> tokenBufferSupplier,
                JsonPointer pointer) {

            this.parser = parser;
            this.feeder = feeder;
            this.tokenBufferSupplier = tokenBufferSupplier;
            this.segments = pointerToSegments(pointer);
            this.actual = new String[segments.length];
            this.depth = -1;
        }

        /**
         * Converts the {@link JsonPointer} into an array of the path segments to make it easier to detect depth and
         * compare the current location within the {@link JsonParser}.
         *
         * @param pointer the pointer to a node within the JSON document
         * @return an array of the segments
         */
        private String[] pointerToSegments(JsonPointer pointer) {
            var list = new ArrayList<String>();

            var current = pointer;
            while (current != null) {
                var property = current.getMatchingProperty();
                if (StringUtils.isNotBlank(property)) {
                    list.add(property);
                }
                current = current.tail();
            }

            return list.toArray(new String[0]);
        }

        /**
         * Feeds the current {@link DataBuffer} to the {@link JsonParser} and parses through parsed tokens
         *
         * @param dataBuffer the data buffer
         * @return a {@link Flux} of the {@link TokenBuffer} of any object graphs to be deserialized
         */
        Flux<TokenBuffer> tokenize(DataBuffer dataBuffer) {
            try {
                var buffer = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(buffer);
                DataBufferUtils.release(dataBuffer);

                feeder.feedInput(buffer, 0, buffer.length);
                return parseTokens();
            } catch (Exception exception) {
                return Flux.error(exception);
            }
        }

        /**
         * Signals that the end of the message has been reached and to parse any remaining tokens
         *
         * @return a {@link Flux} of the {@link TokenBuffer} of any object graphs to be deserialized
         */
        Flux<TokenBuffer> endOfInput() {
            return Flux.defer(() -> {
                feeder.endOfInput();
                return parseTokens();
            });
        }

        /**
         * Parses any tokens currently available in the {@link JsonParser}
         *
         * @return a {@link Flux} of the {@link TokenBuffer} of any object graphs to be deserialized
         */
        private Flux<TokenBuffer> parseTokens() {

            return Flux.defer(() -> {
                try {
                    List<TokenBuffer> tokenBuffers = Collections.emptyList();
                    while (!parser.isClosed()) {
                        var token = parser.nextToken();
                        if (token == null) {
                            token = parser.nextToken();
                        }
                        if (token == null || token == JsonToken.NOT_AVAILABLE) {
                            break;
                        }

                        tokenBuffers = parseToken(parser, token, tokenBuffers);
                    }
                    return Flux.fromIterable(tokenBuffers);
                } catch (IOException exception) {
                    return Flux.error(exception);
                }
            });
        }

        /**
         * Parses the current {@link JsonToken} from the {@link JsonParser} and tracks the current position and state
         * within the JSON document.
         *
         * @param parser       the json parser
         * @param token        the current token being parsed
         * @param tokenBuffers the current list of token buffers
         * @return a list of the {@link TokenBuffer} for any parsed object graphs to be deserialized
         * @throws IOException an exception occurred parsing the JSON
         */
        private List<TokenBuffer> parseToken(JsonParser parser, JsonToken token, List<TokenBuffer> tokenBuffers)
                throws IOException {

            switch (state) {
                case BEFORE_STREAM:
                    if (isAtStreamingStructStart(parser, token)) {
                        state = ParserState.WITHIN_STREAM;
                        // reset the node depth as we don't care about how deep we are into the
                        // entire JSON document anymore, only the depth from the current node
                        depth = 0;
                    }
                    break;
                case WITHIN_STREAM:
                    if (token.isStructEnd() && depth == 0) {
                        // We've reached the end token after the graph of tokens we intend to deserialize
                        // so change the parser state so that we ignore the rest of the tokens
                        state = ParserState.AFTER_STREAM;
                        return appendTokenBuffer(tokenBuffers);
                    }
                    // Append the current token to the current token buffer
                    appendToken(parser);

                    // Track the start and end of any object or array nodes so that we can keep
                    // count of the nesting depth and know when we reach the end of the object
                    // graph we need to buffer
                    if (token.isStructStart()) {
                        depth += 1;
                    } else if (token.isStructEnd()) {
                        depth -= 1;
                        if (depth == 0) {
                            // We've reached the end token of the current object, append the
                            // current token buffer to the list of token buffers to be emitted
                            // in the flux to the subscriber
                            return appendTokenBuffer(tokenBuffers);
                        }
                    }
                    break;
                case AFTER_STREAM:
                default:
                    // do nothing, ignore the remainder of the tokens
                    break;
            }

            return tokenBuffers;
        }

        /**
         * Determines if the current {@link JsonToken} is at the position specified by the {@link JsonPointer}.
         *
         * @param parser the json parser
         * @param token  the current token being parsed
         * @return {@code true} if the current token is at the position specified by the {@link JsonPointer}
         * @throws IOException an exception occurred parsing the JSON
         */
        private boolean isAtStreamingStructStart(JsonParser parser, JsonToken token) throws IOException {
            if (token.isStructStart()) {
                depth += 1;
                if (depth > 0 && depth <= segments.length) {
                    // record the name of the current node to the array of actual path segments
                    var name = parser.currentName();
                    actual[depth - 1] = name;
                    if (depth == segments.length) {
                        // if the current depth is the same as the expected depth then check whether
                        // or not the path segments match, indicating that we're currently at the node
                        // indicated by the JsonPointer
                        return compareSegments(segments, actual);
                    }
                }
            }
            return false;
        }

        /**
         * Compares the expected JSON path segments with the current position of the {@link JsonParser} to determine if
         * the parser is currently at the position specified by the {@link JsonPointer}.
         *
         * @param segments the expected JSON path segments
         * @param actual   the current JSON path segments
         * @return {@code true} if the segments match; otherwise, {@code false}
         */
        private boolean compareSegments(String[] segments, String[] actual) {
            for (int i = 0; i < segments.length; i++) {
                if (!StringUtils.equals(segments[i], actual[i])) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Appends the current token from the parser to the current {@link TokenBuffer}, creating one if necessary
         *
         * @param parser the JSON parser
         * @throws IOException an exception occurred parsing the JSON
         */
        private void appendToken(JsonParser parser) throws IOException {
            if (currentTokenBuffer == null) {
                currentTokenBuffer = tokenBufferSupplier.get();
            }
            currentTokenBuffer.copyCurrentEvent(parser);
        }

        /**
         * Appends the current token buffer to the list of token buffers
         *
         * @param list the current list of token buffers
         * @return a list containing the additional token buffer
         */
        private List<TokenBuffer> appendTokenBuffer(List<TokenBuffer> list) {
            var tokenBuffer = currentTokenBuffer;
            currentTokenBuffer = null;
            if (tokenBuffer != null) {
                if (list.isEmpty()) {
                    return Collections.singletonList(tokenBuffer);
                } else if (list.size() == 1) {
                    var newList = new ArrayList<TokenBuffer>(2);
                    newList.add(list.get(0));
                    newList.add(tokenBuffer);
                    return newList;
                } else {
                    list.add(tokenBuffer);
                    return list;
                }
            }
            return list;
        }

        /**
         * The current state of the parser state machine.
         */
        private enum ParserState {
            /**
             * Indicates that the {@link JsonParser} has not yet reached the node indicated by the {@link JsonPointer}
             */
            BEFORE_STREAM,

            /**
             * Indicates that the {@link JsonParser} is currently within the object graph under the node indicated by
             * the {@link JsonPointer}
             */
            WITHIN_STREAM,

            /**
             * Indicates that the {@link JsonParser} has passed the node indicated by the {@link JsonPointer} and that
             * the remaining payload will be ignored
             */
            AFTER_STREAM
        }
    }
}