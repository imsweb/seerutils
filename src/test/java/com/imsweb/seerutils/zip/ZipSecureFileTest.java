/*
 * Copyright (C) 2023 Information Management Services, Inc.
 */
package com.imsweb.seerutils.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import com.imsweb.seerutils.SeerUtils;

public class ZipSecureFileTest {

    @Test
    public void testZipSecureFile() throws IOException {

        File dir = new File(System.getProperty("user.dir") + "/build");
        if (!dir.exists() && !dir.mkdir())
            Assert.fail("Unable to create build directory!");

        File f1 = new File(dir, "f1.txt");
        SeerUtils.writeFile("test1", f1);
        File f2 = new File(dir, "f2.txt");
        SeerUtils.writeFile("test2", f2);

        File f = new File(dir, "f.zip");
        SeerUtils.zipFiles(Arrays.asList(f1, f2), f);

        try (ZipSecureFile secureFile = new ZipSecureFile(f)) {
            Assert.assertTrue(secureFile.getMaxEntrySize() > 0);
            Assert.assertTrue(secureFile.getMinInflateRatio() > 0.0);

            Enumeration<ZipArchiveEntry> entries = secureFile.getEntries();
            while (entries.hasMoreElements()) {
                try (InputStreamReader reader = new InputStreamReader(secureFile.getInputStream(entries.nextElement())); StringWriter writer = new StringWriter()) {
                    SeerUtils.copyReaderToWriter(reader, writer);
                    Assert.assertTrue(writer.toString().startsWith("test"));
                }
            }
        }

        // test too many entries
        try (ZipSecureFile secureFile = new ZipSecureFile(f, 0.0075, 1L)) {
            Enumeration<ZipArchiveEntry> entries = secureFile.getEntries();
            while (entries.hasMoreElements()) {
                try (InputStreamReader reader = new InputStreamReader(secureFile.getInputStream(entries.nextElement())); StringWriter writer = new StringWriter()) {
                    SeerUtils.copyReaderToWriter(reader, writer);
                }
            }
            Assert.fail("Should have been an exception here");
        }
        catch (ZipEntryTooLargeException e) {
            // expected
        }

        // I can't test the compression ratio because there is a hard-coded "grace" size, and I would have to create a big file for this...
    }
}
