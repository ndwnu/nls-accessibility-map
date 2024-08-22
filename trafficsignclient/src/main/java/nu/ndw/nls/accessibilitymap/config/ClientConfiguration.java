package nu.ndw.nls.accessibilitymap.config;

import nu.ndw.nls.springboot.client.feign.configuration.NlsClientConfiguration;

/**
 * In open api generator, set generateSupportingFiles property to false to allow supplying a custom implementation. Do
 * not turn this class into a bean to avoid leaking this configuration in the spring boot context to other feign
 * clients.
 */
public class ClientConfiguration extends NlsClientConfiguration {

}
