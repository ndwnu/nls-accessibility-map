package nu.ndw.nls.accessibilitymap.jobs.graphhopper;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.springboot.messaging.services.MessagingDeclareAndBindConfigurationService;

@RequiredArgsConstructor
public class ConfigureMessagingApplication {

    private final MessagingDeclareAndBindConfigurationService service;

    public static void main(String[] args) {

    }

}
