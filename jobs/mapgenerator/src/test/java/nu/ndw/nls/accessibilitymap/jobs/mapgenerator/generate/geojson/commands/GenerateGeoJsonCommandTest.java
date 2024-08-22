package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services.GenerateGeoJsonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
class GenerateGeoJsonCommandTest {

    private static final String CMD_ARG_TRUCKS_FORBIDDEN = CmdGenerateGeoJsonType.C7.toString();

    @Mock
    private GenerateGeoJsonService generateGeoJsonService;

    @InjectMocks
    private GenerateGeoJsonCommand generateGeoJsonCommand;

    @Test
    void call_ok_returnsExitCode0() {
        CommandLine commandLine = new CommandLine(generateGeoJsonCommand);
        assertEquals(0, commandLine.execute(CMD_ARG_TRUCKS_FORBIDDEN));
        verify(generateGeoJsonService).generate(CmdGenerateGeoJsonType.C7);
    }

    @Test
    void call_fail_exceptionThrownReturnExitCode1() {
        generateGeoJsonCommand = new GenerateGeoJsonCommand(generateGeoJsonService);
        doThrow(IllegalStateException.class).when(generateGeoJsonService)
                .generate(CmdGenerateGeoJsonType.C7);

        CommandLine commandLine = new CommandLine(generateGeoJsonCommand);
        assertEquals(1, commandLine.execute(CMD_ARG_TRUCKS_FORBIDDEN));

        verify(generateGeoJsonService).generate(CmdGenerateGeoJsonType.C7);
    }

}