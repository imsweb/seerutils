/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.seerutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        Assert.assertEquals(SeerUtils.trimLeft(""), "");
        Assert.assertEquals(SeerUtils.trimLeft("  Hello"), "Hello");
        Assert.assertEquals(SeerUtils.trimLeft("  Hello  "), "Hello  ");
        Assert.assertEquals(SeerUtils.trimLeft("Hello  "), "Hello  ");
        Assert.assertEquals(SeerUtils.trimLeft("  Hello Hello  "), "Hello Hello  ");
    }

    @Test
    public void testTrimRight() {
        Assert.assertNull(null, SeerUtils.trimRight(null));
        Assert.assertEquals(SeerUtils.trimRight(""), "");
        Assert.assertEquals(SeerUtils.trimRight("  Hello"), "  Hello");
        Assert.assertEquals(SeerUtils.trimRight("  Hello  "), "  Hello");
        Assert.assertEquals(SeerUtils.trimRight("Hello  "), "Hello");
        Assert.assertEquals(SeerUtils.trimRight("  Hello Hello  "), "  Hello Hello");
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

    @Test
    public void testIsPureAscii() throws Exception {

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
        Assert.assertTrue(SeerUtils.isPureAscii("".getBytes("US-ASCII"), null));
        Assert.assertTrue(SeerUtils.isPureAscii("   ".getBytes("US-ASCII"), null));
        Assert.assertTrue(SeerUtils.isPureAscii("abc".getBytes("US-ASCII"), null));
        Assert.assertTrue(SeerUtils.isPureAscii("ABC".getBytes("US-ASCII"), null));
        Assert.assertTrue(SeerUtils.isPureAscii("123".getBytes("US-ASCII"), null));
        Assert.assertTrue(SeerUtils.isPureAscii("!@#`".getBytes("US-ASCII"), null));
        Assert.assertTrue(SeerUtils.isPureAscii("A multi-line\nvalue...".getBytes("US-ASCII"), null));
        Assert.assertTrue(SeerUtils.isPureAscii("A multi-line\r\nvalue...".getBytes("US-ASCII"), null));
        Assert.assertTrue(SeerUtils.isPureAscii("A\ttab...".getBytes("US-ASCII"), null));

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
        String outputDirPath = SeerUtils.getWorkingDirectory() + "tempDir\\";
        File tempDir = new File(outputDirPath);
        tempDir.mkdir();

        //Test zipping files
        File testFile1 = new File(outputDirPath + "testFile1.txt");
        File testFile2 = new File(outputDirPath + "testFile2.txt");
        String file1Txt = "This is test file 1.";
        String file2Txt = "This is test file 2.";
        try (FileWriter writer1 = new FileWriter(testFile1); FileWriter writer2 = new FileWriter(testFile2)) {
            writer1.write(file1Txt);
            writer2.write(file2Txt);
        }

        File zipFile = new File(outputDirPath + "testZipFiles.zip");
        SeerUtils.zipFiles(Arrays.asList(testFile1, testFile2), zipFile);

        List<String> fileTxt = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        try (ZipInputStream is = new ZipInputStream(new FileInputStream(zipFile)); LineNumberReader reader = new LineNumberReader(new InputStreamReader(is))) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                fileNames.add(entry.getName());
                fileTxt.add(reader.readLine());
            }
        }
        Assert.assertEquals(2, fileNames.size());
        Assert.assertEquals(testFile1.getName(), fileNames.get(0));
        Assert.assertEquals(testFile2.getName(), fileNames.get(1));
        Assert.assertEquals(file1Txt, fileTxt.get(0));
        Assert.assertEquals(file2Txt, fileTxt.get(1));

        //Test zipping directories
        String testingDirPath = outputDirPath + "dirToZip\\";
        File tempTestDir = new File(testingDirPath);
        tempTestDir.mkdir();

        File testFile3 = new File(testingDirPath + "testFile3.txt");
        File testFile4 = new File(testingDirPath + "testFile4.txt");
        String file3Txt = "This is test file 3.";
        String file4Txt = "This is test file 4.";
        try (FileWriter writer3 = new FileWriter(testFile3); FileWriter writer4 = new FileWriter(testFile4)) {
            writer3.write(file3Txt);
            writer4.write(file4Txt);
        }

        File zipFileDir = new File(outputDirPath + "testZipDir.zip");
        SeerUtils.zipFiles(Collections.singletonList(tempTestDir), zipFileDir);

        fileTxt = new ArrayList<>();
        fileNames = new ArrayList<>();
        try (ZipInputStream is = new ZipInputStream(new FileInputStream(zipFileDir)); LineNumberReader reader = new LineNumberReader(new InputStreamReader(is))) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                fileNames.add(entry.getName());
                if (!entry.isDirectory())
                    fileTxt.add(reader.readLine());
            }
        }
        Assert.assertEquals(3, fileNames.size());
        Assert.assertEquals(tempTestDir.getName() + "/", fileNames.get(0));
        Assert.assertEquals(tempTestDir.getName() + "/" + testFile3.getName(), fileNames.get(1));
        Assert.assertEquals(tempTestDir.getName() + "/" + testFile4.getName(), fileNames.get(2));
        Assert.assertEquals(file3Txt, fileTxt.get(0));
        Assert.assertEquals(file4Txt, fileTxt.get(1));

        //Remove testing directory
        SeerUtils.deleteDirectory(tempDir);
    }
}