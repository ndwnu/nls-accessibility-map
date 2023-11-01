package nu.ndw.nls.routingapi.jobs.commands;

import java.io.IOException;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.routingapi.jobs.nwb.services.NwbNetworkService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Slf4j
@Component
@Command(name = "createOrUpdateNetwork")
@RequiredArgsConstructor
public class CreateOrUpdateNetworkCommand implements Callable<Integer> {

    private final NwbNetworkService nwbNetworkService;

    @Override
    public Integer call() {
        try {
            nwbNetworkService.storeLatestNetworkOnDisk();
            return 0;
        } catch (IOException | RuntimeException e) {
            log.error("And error occurred while creating or updating latest network", e);
            return 1;
        }
    }
}
