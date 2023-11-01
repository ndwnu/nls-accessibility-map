package nu.ndw.nls.routingapi.jobs.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import lombok.SneakyThrows;
import nu.ndw.nls.routingapi.jobs.nwb.services.NwbNetworkService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateOrUpdateNetworkCommandTest {

    @Mock
    private NwbNetworkService networkImportService;

    @InjectMocks
    private CreateOrUpdateNetworkCommand createOrUpdateNetworkCommand;

    @SneakyThrows
    @Test
    void call_ok() {
        int result = createOrUpdateNetworkCommand.call();
        verify(networkImportService).storeLatestNetworkOnDisk();
        assertThat(result).isEqualTo(0);
    }

    @SneakyThrows
    @Test
    void call_exception() {
        doThrow(new RuntimeException("exception")).when(networkImportService).storeLatestNetworkOnDisk();
        int result = createOrUpdateNetworkCommand.call();
        assertThat(result).isEqualTo(1);
    }
}
