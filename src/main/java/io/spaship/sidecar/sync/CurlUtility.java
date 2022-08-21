package io.spaship.sidecar.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class CurlUtility {

    private static final Logger LOG = LoggerFactory.getLogger(CurlUtility.class);

    public static int processCommand(String curlCommand, String executionDirectory, boolean debug) throws IOException, InterruptedException {
        String[] commands = curlCommand.split("~");
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(new File(executionDirectory));
        Process process = processBuilder.start();
        AtomicInteger status= new AtomicInteger();
        process.onExit().thenRun(() -> {
            var exitValue = process.exitValue();
            LOG.info("curl command {} exited with status {}",curlCommand,exitValue);
            status.set(exitValue);
        });
        if(debug)
            debugCommandOutput(process);
        process.waitFor();
        process.destroy();
        return status.get();
    }

    public static  void debugCommandOutput(Process process) {
        System.out.println("debugging curl command");
        final Thread ioThread = new Thread(() -> {
            try {
                final BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                reader.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });

        ioThread.start();
    }

    public static  String constructRemoteFileDownloadCommand(String url,String fileName,
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
