package nu.ndw.nls.accessibilitymap.accessibility.cache.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@Slf4j
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler cacheWatcherTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("cache-watcher-");
        scheduler.initialize();
        return scheduler;
    }
}
