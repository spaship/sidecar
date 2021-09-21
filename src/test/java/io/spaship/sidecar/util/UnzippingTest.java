package io.spaship.sidecar.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnzippingTest {

    File sourceFile = null;
    File destFile = null;
    @TempDir
    File tempDir;
    private String zipFIlePath = "";
    private String destDirPath = "";

    @SneakyThrows
    @BeforeAll
    void setup() {
        sourceFile = new File(tempDir, "sample.zip");
        destFile = new File(tempDir, "unzip");

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(sourceFile));

        ZipEntry e = new ZipEntry("mytext.txt");
        out.putNextEntry(e);

        byte[] data = ("Test String" + UUID.randomUUID().toString()).getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();
        out.close();

        this.zipFIlePath = sourceFile.getAbsolutePath();
        this.destDirPath = destFile.getAbsolutePath();


        System.out.println("created zip with path " + zipFIlePath);
        System.out.println("folder tobe extracted in path " + destDirPath);

    }


    @SneakyThrows
    @AfterAll
    void tearDown() {
        if (destFile.exists())
            Files.walk(destFile.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        if (sourceFile.exists())
            new File(zipFIlePath).delete();
        System.out.println("deleted files/folders after testing");
    }


    @Test
    void testUnzipFunctionality() {

        String dest = null;
        try {
            dest = CommonOps.unzip(zipFIlePath, destDirPath);
        } catch (Exception ex) {
            // some errors occurred
            ex.printStackTrace();
        }

        Assertions.assertEquals(destDirPath, dest);
        Assertions.assertFalse(destDirPath.isBlank(), "destDirPath must not be empty");
        Assertions.assertTrue(sourceFile.exists(), "source zip file not found!");
        Assertions.assertTrue(destFile.exists(), "destination file not created");
    }

    @Test
    void testUnzipFunctionalityWhenDestIsNull() {

        String dest = null;
        try {
            dest = CommonOps.unzip(zipFIlePath, null);
        } catch (Exception ex) {
            // some errors occurred
            ex.printStackTrace();
        }

        System.out.println("destination directory is " + dest);

        Assertions.assertNotNull(dest, "destination is null");
        Assertions.assertFalse(destDirPath.isBlank(), "destDirPath must not be empty");
        Assertions.assertTrue(sourceFile.exists(), "source zip file not found!");
        Assertions.assertTrue(new File(dest).exists(), "destination file not created");
    }

}

