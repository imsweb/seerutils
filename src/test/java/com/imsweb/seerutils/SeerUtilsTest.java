/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.seerutils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class SeerUtilsTest {

    @Test
    public void testPad() {
        Assert.assertNull(null, SeerUtils.pad(null, 4, "0", true));
        Assert.assertEquals("0000", SeerUtils.pad("", 4, "0", true));
        Assert.assertEquals("0000", SeerUtils.pad("0", 4, "0", true));
        Assert.assertEquals("0000", SeerUtils.pad("0", 4, "0", false));
        Assert.assertEquals("00000", SeerUtils.pad("00000", 4, "0", true));
        Assert.assertEquals("   0", SeerUtils.pad("0", 4, " ", true));
        Assert.assertEquals("0   ", SeerUtils.pad("0", 4, " ", false));
        Assert.assertEquals("WeirdWeirdWeirdWeird", SeerUtils.pad("Weird", 20, "Weird", false));
    }

    @Test
    public void testTrimLeft() {
        Assert.assertNull(null, SeerUtils.trimLeft(null));
        Assert.assertEquals("", SeerUtils.trimLeft(""));
        Assert.assertEquals("Hello", SeerUtils.trimLeft("  Hello"));
        Assert.assertEquals("Hello  ", SeerUtils.trimLeft("  Hello  "));
        Assert.assertEquals("Hello  ", SeerUtils.trimLeft("Hello  "));
        Assert.assertEquals("Hello Hello  ", SeerUtils.trimLeft("  Hello Hello  "));
    }

    @Test
    public void testTrimRight() {
        Assert.assertNull(null, SeerUtils.trimRight(null));
        Assert.assertEquals("", SeerUtils.trimRight(""));
        Assert.assertEquals("  Hello", SeerUtils.trimRight("  Hello"));
        Assert.assertEquals("  Hello", SeerUtils.trimRight("  Hello  "));
        Assert.assertEquals("Hello", SeerUtils.trimRight("Hello  "));
        Assert.assertEquals("  Hello Hello", SeerUtils.trimRight("  Hello Hello  "));
    }

    @Test
    public void testFormatNumber() {
        Assert.assertEquals("-1", SeerUtils.formatNumber(-1));
        Assert.assertEquals("1", SeerUtils.formatNumber(1));
        Assert.assertEquals("100", SeerUtils.formatNumber(100));
        Assert.assertEquals("1,000", SeerUtils.formatNumber(1000));
        Assert.assertEquals("1,000,000,000", SeerUtils.formatNumber(1000000000));
    }

    @Test
    public void testFormatTime() {
        Assert.assertNotNull(null, SeerUtils.formatTime(1L));
    }

    @Test
    public void testFormatFileSize() {
        Assert.assertEquals("2.4 GB", (SeerUtils.formatFileSize(2523456789L)));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testIsPureAscii() {

        // string flavor
        Assert.assertTrue(SeerUtils.isPureAscii((String)null));
        Assert.assertTrue(SeerUtils.isPureAscii(""));
        Assert.assertTrue(SeerUtils.isPureAscii("   "));
        Assert.assertTrue(SeerUtils.isPureAscii("abc"));
        Assert.assertTrue(SeerUtils.isPureAscii("ABC"));
        Assert.assertTrue(SeerUtils.isPureAscii("123"));
        Assert.assertTrue(SeerUtils.isPureAscii("!@#`"));
        Assert.assertTrue(SeerUtils.isPureAscii("A multi-line\nvalue..."));
        Assert.assertTrue(SeerUtils.isPureAscii("A multi-line\r\nvalue..."));
        Assert.assertTrue(SeerUtils.isPureAscii("A\ttab..."));

        // byte array flavor, still using strings
        Assert.assertTrue(SeerUtils.isPureAscii(null, null));
        Assert.assertTrue(SeerUtils.isPureAscii("".getBytes(StandardCharsets.US_ASCII), null));
        Assert.assertTrue(SeerUtils.isPureAscii("   ".getBytes(StandardCharsets.US_ASCII), null));
        Assert.assertTrue(SeerUtils.isPureAscii("abc".getBytes(StandardCharsets.US_ASCII), null));
        Assert.assertTrue(SeerUtils.isPureAscii("ABC".getBytes(StandardCharsets.US_ASCII), null));
        Assert.assertTrue(SeerUtils.isPureAscii("123".getBytes(StandardCharsets.US_ASCII), null));
        Assert.assertTrue(SeerUtils.isPureAscii("!@#`".getBytes(StandardCharsets.US_ASCII), null));
        Assert.assertTrue(SeerUtils.isPureAscii("A multi-line\nvalue...".getBytes(StandardCharsets.US_ASCII), null));
        Assert.assertTrue(SeerUtils.isPureAscii("A multi-line\r\nvalue...".getBytes(StandardCharsets.US_ASCII), null));
        Assert.assertTrue(SeerUtils.isPureAscii("A\ttab...".getBytes(StandardCharsets.US_ASCII), null));

        // byte array flavor using pure bytes
        Assert.assertTrue(SeerUtils.isPureAscii(new byte[] {}, null));
        Assert.assertTrue(SeerUtils.isPureAscii(new byte[] {97, 98, 99}, null)); // abc
        Assert.assertTrue(SeerUtils.isPureAscii(new byte[] {9, 10, 13, 32, 126}, null)); // random bytes falling into the acceptable range
        Assert.assertFalse(SeerUtils.isPureAscii(new byte[] {0}, null)); // control
        Assert.assertFalse(SeerUtils.isPureAscii(new byte[] {16}, null)); // control
        Assert.assertFalse(SeerUtils.isPureAscii(new byte[] {31}, null)); // control
        Assert.assertFalse(SeerUtils.isPureAscii(new byte[] {127}, null)); // control
        Assert.assertTrue(SeerUtils.isPureAscii(new byte[] {97, 98, 99, 16}, new byte[] {16})); // last byte is control, but an exception was added...
        Assert.assertTrue(SeerUtils.isPureAscii(new byte[] {31, 97, 31, 98, 30, 99, 30}, new byte[] {30, 31})); // some bytes are control, but an exception was added...
    }

    @Test
    public void testCompareSeerVersions() {
        Assert.assertEquals(0, SeerUtils.compareSeerVersions("1.0", "1.0"));
        Assert.assertEquals(0, SeerUtils.compareSeerVersions("1.0", "1"));
        Assert.assertEquals(0, SeerUtils.compareSeerVersions("1", "1.0"));
        Assert.assertEquals(0, SeerUtils.compareSeerVersions("1", "1"));
        Assert.assertEquals(0, SeerUtils.compareSeerVersions("1.0.0.0", "1"));
        Assert.assertEquals(0, SeerUtils.compareSeerVersions("1.0", "1.0-SNAPSHOT"));
        Assert.assertEquals(0, SeerUtils.compareSeerVersions("1.0-SNAPSHOT", "1.0"));
        Assert.assertEquals(0, SeerUtils.compareSeerVersions("v1.0", "1.0"));
        Assert.assertEquals(0, SeerUtils.compareSeerVersions("1.0", "V1.0"));

        Assert.assertEquals(1, SeerUtils.compareSeerVersions("2.0", "1.0"));
        Assert.assertEquals(1, SeerUtils.compareSeerVersions("2.0.0", "1.0"));
        Assert.assertEquals(1, SeerUtils.compareSeerVersions("2.0", "1.0.1"));
        Assert.assertEquals(1, SeerUtils.compareSeerVersions("2.0", "1.2.3"));
        Assert.assertEquals(1, SeerUtils.compareSeerVersions("2.0", "1.5"));
        Assert.assertEquals(1, SeerUtils.compareSeerVersions("2.0.1", "2.0"));
        Assert.assertEquals(1, SeerUtils.compareSeerVersions("2.0.1", "2.0.0.1"));

        Assert.assertEquals(-1, SeerUtils.compareSeerVersions("1.0", "2.0"));
        Assert.assertEquals(-1, SeerUtils.compareSeerVersions("1.0", "2.0.0"));
        Assert.assertEquals(-1, SeerUtils.compareSeerVersions("1.0.1", "2.0"));
        Assert.assertEquals(-1, SeerUtils.compareSeerVersions("1.2.3", "2.0"));
        Assert.assertEquals(-1, SeerUtils.compareSeerVersions("1.5", "2.0"));
        Assert.assertEquals(-1, SeerUtils.compareSeerVersions("2.0", "2.0.1"));
        Assert.assertEquals(-1, SeerUtils.compareSeerVersions("2.0.0.1", "2.0.1"));

        Assert.assertEquals(1, SeerUtils.compareSeerVersions("1.0", null));
        Assert.assertEquals(-1, SeerUtils.compareSeerVersions(null, "1.0"));

        Assert.assertEquals(-1, SeerUtils.compareSeerVersions("1.10", "2.0"));
        Assert.assertEquals(-1, SeerUtils.compareSeerVersions("1.1", "2.10"));
        Assert.assertEquals(-1, SeerUtils.compareSeerVersions("1.11", "1.12"));
        Assert.assertEquals(-1, SeerUtils.compareSeerVersions("1.11.111", "2.0.0"));
    }

    @Test(expected = RuntimeException.class)
    public void testCompareSeerVersionsBadInput1() {
        SeerUtils.compareSeerVersions("ABC", "1.0");
    }

    @Test(expected = RuntimeException.class)
    public void testCompareSeerVersionsBadInput2() {
        SeerUtils.compareSeerVersions("1.0", "???");
    }

    @Test
    public void testZipFiles() throws IOException {
        File tempDir = new File(getTestingDirectory(), "tmpDir");
        if (!tempDir.mkdir())
            throw new IOException("Unable to create dir");

        //Test zipping files
        File testFile1 = new File(tempDir, "testFile1.txt");
        File testFile2 = new File(tempDir, "testFile2.txt");
        String file1Txt = "This is test file 1.";
        String file2Txt = "This is test file 2.";
        try (Writer writer1 = new OutputStreamWriter(Files.newOutputStream(testFile1.toPath()), StandardCharsets.US_ASCII);
             Writer writer2 = new OutputStreamWriter(Files.newOutputStream(testFile2.toPath()), StandardCharsets.US_ASCII)) {
            writer1.write(file1Txt);
            writer2.write(file2Txt);
        }

        File zipFile = new File(tempDir, "testZipFiles.zip");
        SeerUtils.zipFiles(Arrays.asList(testFile1, testFile2), zipFile);

        List<String> fileTxt = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        try (ZipInputStream is = new ZipInputStream(Files.newInputStream(zipFile.toPath())); LineNumberReader reader = new LineNumberReader(new InputStreamReader(is, StandardCharsets.US_ASCII))) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                fileNames.add(entry.getName());
                fileTxt.add(reader.readLine());
            }
        }
        Assert.assertEquals(2, fileNames.size());
        Assert.assertTrue(fileNames.contains(testFile1.getName()));
        Assert.assertTrue(fileNames.contains(testFile2.getName()));
        Assert.assertTrue(fileTxt.contains(file1Txt));
        Assert.assertTrue(fileTxt.contains(file2Txt));

        //Test zipping directories
        File tempTestDir = new File(tempDir, "dirToZip");
        if (!tempTestDir.mkdir())
            throw new IOException("Unable to create dir");

        File testFile3 = new File(tempTestDir, "testFile3.txt");
        File testFile4 = new File(tempTestDir, "testFile4.txt");
        String file3Txt = "This is test file 3.";
        String file4Txt = "This is test file 4.";
        try (Writer writer3 = new OutputStreamWriter(Files.newOutputStream(testFile3.toPath()), StandardCharsets.US_ASCII);
             Writer writer4 = new OutputStreamWriter(Files.newOutputStream(testFile4.toPath()), StandardCharsets.US_ASCII)) {
            writer3.write(file3Txt);
            writer4.write(file4Txt);
        }

        File zipFileDir = new File(tempDir, "testZipDir.zip");
        SeerUtils.zipFiles(Collections.singletonList(tempTestDir), zipFileDir);

        fileTxt = new ArrayList<>();
        fileNames = new ArrayList<>();
        try (ZipInputStream is = new ZipInputStream(Files.newInputStream(zipFileDir.toPath())); LineNumberReader reader = new LineNumberReader(new InputStreamReader(is, StandardCharsets.US_ASCII))) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                fileNames.add(entry.getName());
                if (!entry.isDirectory())
                    fileTxt.add(reader.readLine());
            }
        }
        Assert.assertEquals(3, fileNames.size());
        Assert.assertTrue(fileNames.contains(tempTestDir.getName() + "/"));
        Assert.assertTrue(fileNames.contains(tempTestDir.getName() + "/" + testFile3.getName()));
        Assert.assertTrue(fileNames.contains(tempTestDir.getName() + "/" + testFile4.getName()));
        Assert.assertTrue(fileTxt.contains(file3Txt));
        Assert.assertTrue(fileTxt.contains(file4Txt));

        //Remove testing directory
        SeerUtils.deleteDirectory(tempDir);
    }

    @Test
    public void testCopyDirectory() throws IOException {
        File dir = new File(getTestingDirectory(), "test-source");
        if (dir.exists())
            FileUtils.deleteDirectory(dir);
        Assert.assertFalse(dir.exists());
        Assert.assertTrue(dir.mkdir());
        SeerUtils.writeFile("TEST", new File(dir, "test.txt"));
        File subDir1 = new File(dir, "howtos");
        Assert.assertTrue(subDir1.mkdir());
        SeerUtils.writeFile("TEST2", new File(subDir1, "test2.txt"));
        File subDir2 = new File(subDir1, "images");
        Assert.assertTrue(subDir2.mkdir());
        SeerUtils.writeFile("TEST3", new File(subDir2, "test3.txt"));

        File newDir = new File(getTestingDirectory(), "test-target");
        SeerUtils.copyDirectory(dir, newDir);
        Assert.assertEquals("TEST", SeerUtils.readFile(new File(newDir, "test.txt")));
        File newSubDir1 = new File(newDir, "howtos");
        Assert.assertEquals("TEST2", SeerUtils.readFile(new File(newSubDir1, "test2.txt")));
        File newSubDir2 = new File(newSubDir1, "images");
        Assert.assertEquals("TEST3", SeerUtils.readFile(new File(newSubDir2, "test3.txt")));
    }

    private File getTestingDirectory() {
        File workingDir = new File(System.getProperty("user.dir").replace(".idea\\modules\\", ""));
        if (!workingDir.exists())
            throw new RuntimeException("Unable to find " + workingDir.getPath());
        File file = new File(workingDir, "build/test-data");
        if (!file.exists() && !file.mkdir())
            throw new RuntimeException("Unable to crate " + file.getPath());
        return file;
    }
}