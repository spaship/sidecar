package io.spaship.sidecar.sync;

import io.spaship.sidecar.util.QuadFunction;
import io.spaship.sidecar.util.TriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class CurlUtility {

    private static final Logger LOG = LoggerFactory.getLogger(CurlUtility.class);

    static TriFunction<String, String, Boolean, Integer> execution = CurlUtility::processCommand;
    static QuadFunction<String, String,String, String, String> command = CurlUtility::constructRemoteFileDownloadCommand;

    public static QuadFunction<String, String,String, String, Integer> with(String executionDirectory, boolean debug){
        return command.andThen(res->execution.apply(res,executionDirectory,debug));
    }

    static int processCommand(String curlCommand, String executionDirectory, boolean debug){
        try {
            String[] commands = curlCommand.split("~");
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(new File(executionDirectory));
            Process process = processBuilder.start();
            var formattedCurlCommand =  curlCommand.replace("~"," ");
            process.onExit().thenRun(() -> LOG.info("curl command executed for url {}", commands[commands.length-3]));
            if (debug)
                debugCommandOutput(process,formattedCurlCommand);
            process.waitFor();
            process.destroy();
            return process.exitValue();
        }catch(IOException | InterruptedException e){
            LOG.error("error while processing command",e);
            return 1;
        }
    }

    static  void debugCommandOutput(Process process, String curlCommand) {
        LOG.info("debugging curl command {} ",curlCommand);
        final Thread ioThread = new Thread(() -> {
            try {
                final BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    LOG.info(line);
                }
                reader.close();
            } catch (final Exception e) {
                LOG.error("error while reading command output",e);
            }
        });

        ioThread.start();
    }

    static  String constructRemoteFileDownloadCommand(String url,String fileName,
                                                     String noCacheParam,String proxyParam){
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append("curl~-k~");
        if(Objects.nonNull(noCacheParam) && !noCacheParam.isEmpty())
            commandBuilder.append(noCacheParam).append("~");
        if(Objects.nonNull(proxyParam) && !proxyParam.isEmpty())
            commandBuilder.append(proxyParam).append("~");
        commandBuilder.append(url).append("~").append("-o~").append(fileName);

        return commandBuilder.toString();
    }
}
