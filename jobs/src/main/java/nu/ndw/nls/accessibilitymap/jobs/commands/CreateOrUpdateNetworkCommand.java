package nu.ndw.nls.accessibilitymap.jobs.commands;

import java.io.IOException;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.services.AccessibilityNetworkService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@Command(name = "createOrUpdateNetwork")
@RequiredArgsConstructor
public class CreateOrUpdateNetworkCommand implements Callable<Integer> {

    private final AccessibilityNetworkService accessibilityNetworkService;

    @Override
    public Integer call() {
        try {
            accessibilityNetworkService.storeLatestNetworkOnDisk();
            return 0;
        } catch (IOException | RuntimeException e) {
            log.error("And error occurred while creating or updating latest network", e);
            return 1;
        }
    }
}
