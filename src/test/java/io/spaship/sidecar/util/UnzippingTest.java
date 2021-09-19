package io.spaship.sidecar.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnzippingTest {

    private String zipFIlePath = "";
    private String destDirPath = "";

    File sourceFile = null;
    File destFile = null;

    @TempDir
    File tempDir;

    @SneakyThrows
    @BeforeAll
    void setup(){
        sourceFile = new File(tempDir, "sample.zip");
        destFile = new File(tempDir, "unzip");

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(sourceFile));

        StringBuilder sb = new StringBuilder();
        sb.append("Test String");

        ZipEntry e = new ZipEntry("mytext.txt");
        out.putNextEntry(e);

        byte[] data = sb.toString().getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();
        out.close();

        this.zipFIlePath = sourceFile.getAbsolutePath();
        this.destDirPath = destFile.getAbsolutePath();


        System.out.println("created zip with path "+zipFIlePath);
        System.out.println("folder tobe extracted in path "+destDirPath);

    }


    @SneakyThrows
    @AfterAll
    void tearDown(){
        if(destFile.exists())
            Files.walk(destFile.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        if(sourceFile.exists())
            new File(zipFIlePath).delete();
        System.out.println("deleted files/folders after testing");
    }


    @Test
    void testUnzipFunctionality() {

        String dest = null;
        try {
            dest = Unzip.unzip(zipFIlePath, destDirPath);
        } catch (Exception ex) {
            // some errors occurred
            ex.printStackTrace();
        }

        Assertions.assertEquals(destDirPath,dest);
        Assertions.assertFalse(destDirPath.isBlank(), "destDirPath must not be empty");
        Assertions.assertTrue(sourceFile.exists(),"source zip file not found!");
        Assertions.assertTrue(destFile.exists(),"destination file not created");
    }

}

