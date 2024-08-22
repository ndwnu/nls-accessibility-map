package nu.ndw.nls.accessibilitymap.trafficsignclient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.springboot.client.feign.ClientFeignConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Getter
@Configuration
@ComponentScan
@Import(ClientFeignConfiguration.class)
@RequiredArgsConstructor
@EnableFeignClients
@EnableConfigurationProperties(TrafficSignProperties.class)
public class TrafficSignConfiguration {

    private final TrafficSignProperties trafficSignProperties;

}
