package io.spaship.sidecar.util.sync;

import org.junit.jupiter.api.*;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcessBuilderTest {

    String curlCommand = "curl~-k~-H~'Cache-Control: no-cache, no-store'~--proxy~http://squid.corp.redhat.com:3128~https://access.qa.redhat.com/services/chrome/head/en?legacy=false~-o~test.html";
    String fileLocation = "/home/arbhatta/app/data/test.html";

    @BeforeAll
    void destroy(){
        File fileToDelete = new File(fileLocation);
        if(fileToDelete.exists())
            fileToDelete.delete();
    }

    @Test
    @DisplayName("Should not throw an exception")
    void testProcessBuilding() throws IOException, InterruptedException {
        var processStatus = Assertions.assertDoesNotThrow(
                ()-> processCommand(curlCommand,"/home/arbhatta/app/data",true));
        Assertions.assertEquals(0, (int) processStatus);
    }

    private int processCommand(String curlCommand, String executionDirectory, boolean debug) throws IOException, InterruptedException {
        String[] commands = curlCommand.split("~");
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(new File(executionDirectory));
        Process process = processBuilder.start();
        AtomicInteger status= new AtomicInteger();
        process.onExit().thenRun(() -> {
            System.out.println("curl command "+curlCommand+" exited with status "+process.exitValue());
            status.set(process.exitValue());
        });
        if(debug)
            debugCommandOutput(process);
        process.waitFor();
        process.destroy();
        return status.get();
    }

    private void debugCommandOutput(Process process) {
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



    public String constructRemoteFileDownloadCommand(String url,String fileName,
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

    @Test
    void commandOutputTest() {

        var command = constructRemoteFileDownloadCommand(
                "https://access.qa.redhat.com/services/chrome/head/en?legacy=false",
                "test.html",
                "-H~'Cache-Control: no-cache, no-store'",
                "--proxy~http://squid.corp.redhat.com:3128");
        System.out.println(curlCommand);
        System.out.println(command);
        System.out.println(curlCommand.equals(command));
        Assertions.assertTrue(curlCommand.equals(command));
    }

}
