/*
 * Copyright (C) 2023 Information Management Services, Inc.
 */
package com.imsweb.seerutils.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;

/**
 * This class wraps a {@link ZipFile} in order to check the entries for <a href="https://en.wikipedia.org/wiki/Zip_bomb">zip bombs</a>
 * while reading the archive.
 */
@SuppressWarnings("unused")
public class ZipSecureFile extends ZipFile {

    /**
     * The ratio between de- and inflated bytes to detect zip-bomb. It defaults to 0.75% (= 0.0075d), i.e. when the compression is better than
     * 1% for any given read package part, the parsing will fail indicating a Zip-Bomb.
     */
    private final double _minInflateRatio;

    /**
     * Sets the maximum file size of a single zip entry. It defaults to 4GB, i.e. the 32-bit zip format maximum.
     */
    private final long _maxEntrySize;

    public ZipSecureFile(File file) throws IOException {
        this(file, 0.0075, 4294967296L);
    }

    public ZipSecureFile(File file, double minInflationRatio, long maxEntrySize) throws IOException {
        super(file);

        _minInflateRatio = minInflationRatio;
        _maxEntrySize = maxEntrySize;
    }

    /**
     * Returns the current minimum compression rate that is used.
     * @return The min accepted compression-ratio.
     */
    public double getMinInflateRatio() {
        return _minInflateRatio;
    }

    /**
     * Returns the current maximum allowed uncompressed file size.
     * @return The max accepted uncompressed file size.
     */
    public long getMaxEntrySize() {
        return _maxEntrySize;
    }

    /**
     * Returns an input stream for reading the contents of the specified zip file entry.
     * <p>
     * Closing this ZIP file will, in turn, close all input streams that have been returned by invocations of this method.
     * @param entry the zip file entry
     * @return the input stream for reading the contents of the specified zip file entry.
     * @throws IOException if an I/O error has occurred
     * @throws IllegalStateException if the zip file has been closed
     */
    @Override
    public InputStream getInputStream(ZipArchiveEntry entry) throws IOException {
        ZipArchiveThresholdInputStream is = new ZipArchiveThresholdInputStream(super.getInputStream(entry));

        is.setEntry(entry);
        is.setMinInflateRatio(_minInflateRatio);
        is.setMaxEntrySize(_maxEntrySize);

        return is;
    }

    /**
     * In-memory test of a ZIP file to ensure it is not a zip-bomb
     * @param url location of zip file
     * @param maxEntries maximum number of entries allowed in teh ZIP file
     * @return true if a possible zip-bomb
     * @throws IOException if a problem with reading the file
     */
    public static boolean isZipBomb(URL url, int maxEntries) throws IOException {
        boolean detected = false;
        long numEntries = 0;

        try (ZipSecureFile z = new ZipSecureFile(new File(url.getFile()))) {
            Enumeration<? extends ZipArchiveEntry> entries = z.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();

                numEntries++;
                if (numEntries > maxEntries) {
                    detected = true;
                    break;
                }

                try (InputStream inputStream = z.getInputStream(entry)) {
                    if (IOUtils.toByteArray(inputStream).length == 0)
                        throw new IllegalStateException("Error processing file");
                }
            }
        }
        catch (ZipEntryTooLargeException | ZipInvalidCompressionRatioException e) {
            detected = true;
        }

        return detected;
    }

}

