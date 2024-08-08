package nu.ndw.nls.accessibilitymap.trafficsignclient.repositories;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractor.Context;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class StreamingJsonBodyExtractorTest {

    @Mock
    private Context context;

    private static final String FEATURE_1_UUID = "007d3859-d6d6-42b7-9c0c-90ebf981743f";
    private static final String FEATURE_2_UUID = "010221c8-c4ba-4e88-ad3a-1127a46495bb";
    private static final String FEATURES = "/features";


    @Test
    void toFlux_ok() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        StreamingJsonBodyExtractor extractor = new StreamingJsonBodyExtractor(mapper);

        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(createJson().getBytes(StandardCharsets.UTF_8));
        Flux<DataBuffer> dataBufferFlux = Flux.just(dataBuffer);

        ServerHttpRequest request = MockServerHttpRequest.post("/").body(dataBufferFlux);

        JsonPointer pointer = JsonPointer.compile(FEATURES);

        BodyExtractor<Flux<TrafficSignGeoJsonDto>, ServerHttpRequest> bodyExtractor = extractor.toFlux(
                TrafficSignGeoJsonDto.class, pointer);

        Flux<TrafficSignGeoJsonDto> result = bodyExtractor.extract(request, context);

        StepVerifier.create(result)
                .expectNextMatches(
                        feature -> UUID.fromString(FEATURE_1_UUID).equals(feature.getId()))
                .expectNextMatches(
                        feature -> UUID.fromString(FEATURE_2_UUID).equals(feature.getId()))
                .verifyComplete();
    }

    @Test
    void toFlux_throwsIOException() {
        ObjectMapper mapper = mock(ObjectMapper.class);
        StreamingJsonBodyExtractor extractor = new StreamingJsonBodyExtractor(mapper);

        // Simulate IOException during parsing
        when(mapper.getFactory()).thenAnswer(invocation -> {
            throw new IOException("Simulated IOException");
        });

        String json = "{\"features\": []}";
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(json.getBytes());
        Flux<DataBuffer> dataBufferFlux = Flux.just(dataBuffer);

        ServerHttpRequest request = MockServerHttpRequest.post("/").body(dataBufferFlux);
        JsonPointer pointer = JsonPointer.compile(FEATURES);

        BodyExtractor<Flux<Object>, ServerHttpRequest> bodyExtractor = extractor.toFlux(Object.class, pointer);
        Flux<Object> result = bodyExtractor.extract(request, context);

        StepVerifier.create(result)
                .expectError(IOException.class)
                .verify();
    }

    @Test
    void toFlux_throwsRuntimeException() {
        ObjectMapper mapper = mock(ObjectMapper.class);
        StreamingJsonBodyExtractor extractor = new StreamingJsonBodyExtractor(mapper);

        when(mapper.getFactory()).thenThrow(new RuntimeException("Simulated RuntimeException"));

        String json = "{\"features\": []}";
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(json.getBytes());
        Flux<DataBuffer> dataBufferFlux = Flux.just(dataBuffer);

        ServerHttpRequest request = MockServerHttpRequest.post("/").body(dataBufferFlux);
        JsonPointer pointer = JsonPointer.compile(FEATURES);

        BodyExtractor<Flux<Object>, ServerHttpRequest> bodyExtractor = extractor.toFlux(Object.class, pointer);
        Flux<Object> result = bodyExtractor.extract(request, context);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    private String createJson() {
        return """
                {
                    "type": "FeatureCollection",
                    "features": [
                        {
                            "type": "Feature",
                            "id": "007d3859-d6d6-42b7-9c0c-90ebf981743f",
                            "geometry": {
                                "type": "Point",
                                "coordinates": [5.3878848407251, 52.205033343868]
                            },
                            "properties": {
                                "externalId": "some-external-id",
                                "validated": "n",
                                "validatedOn": "2020-05-07",
                                "rvvCode": "C7",
                                "blackCode": "some-black-code",
                                "zoneCode": "some-zone-code",
                                "status": "PLACED",
                                "textSigns": [],
                                "placement": "L",
                                "side": "W",
                                "bearing": 270,
                                "nenTurningDirection": 1,
                                "fraction": 0.043469179421663284,
                                "drivingDirection": "T",
                                "roadName": "Lindeboomseweg",
                                "roadType": 1,
                                "roadNumber": 123,
                                "roadSectionId": 310337011,
                                "nwbVersion": "2024-07-01",
                                "countyName": "Amersfoort",
                                "countyCode": "GM0307",
                                "townName": "Amersfoort",
                                "bgtCode": "some-bgt-code",
                                "imageUrl": "https://wegkenmerken.ndw.nu/api/images/9986c108-9802-4710-97f4-fbb186b6c81f",
                                "firstSeenOn": "2020-05-07",
                                "lastSeenOn": "2024-02-19"
                            }
                        },
                        {
                            "type": "Feature",
                            "id": "010221c8-c4ba-4e88-ad3a-1127a46495bb",
                            "geometry": {
                                "type": "Point",
                                "coordinates": [5.4574392250096, 52.154982361918]
                            },
                            "properties": {
                                "externalId": "another-external-id",
                                "validated": "n",
                                "validatedOn": "2016-06-10",
                                "rvvCode": "C12",
                                "blackCode": "another-black-code",
                                "zoneCode": "another-zone-code",
                                "status": "PLACED",
                                "textSigns": [
                                    {
                                        "type": "TIJD",
                                        "text": "ma t/m vr 07.00-09.30 b uitgezonderd antheffingbouders en landbouwvdertulg-controle"
                                    }
                                ],
                                "placement": "L",
                                "side": "W",
                                "bearing": 270,
                                "nenTurningDirection": 2,
                                "fraction": 0.13677139580249786,
                                "drivingDirection": "T",
                                "roadName": "Stoutenburgerlaan",
                                "roadType": 2,
                                "roadNumber": 456,
                                "roadSectionId": 319325003,
                                "nwbVersion": "2024-07-01",
                                "countyName": "Amersfoort",
                                "countyCode": "GM0307",
                                "townName": "Stoutenburg Noord",
                                "bgtCode": "another-bgt-code",
                                "imageUrl": "https://wegkenmerken.ndw.nu/api/images/9cd196cf-4114-4ede-90d2-75115419a3e2",
                                "firstSeenOn": "2016-06-10",
                                "lastSeenOn": "2023-12-03"
                            }
                        }
                    ]
                }
                """;
    }

}