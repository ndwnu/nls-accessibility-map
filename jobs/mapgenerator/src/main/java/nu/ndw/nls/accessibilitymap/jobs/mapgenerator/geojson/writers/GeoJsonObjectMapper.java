//package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;
//
//import com.fasterxml.jackson.annotation.JsonInclude.Include;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//
//@Component
//public class GeoJsonObjectMapper extends ObjectMapper {
//
//    public GeoJsonObjectMapper(GenerateConfiguration generateConfiguration) {
//
//        super();
//
//        this.setSerializationInclusion(Include.NON_NULL);
//
//        if (generateConfiguration.isPrettyPrintJson()) {
//            this.enable(SerializationFeature.INDENT_OUTPUT);
//        }
//    }
//}
