/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.seerutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This class provides shared functionality that can be used by all the other modules in SEER*Utils.
 */
public final class SeerUtils {

    // cached pattern for the versions
    private static final Pattern _VERSION_CLEANUP_PATTERN = Pattern.compile("^v|-(snapshot|beta)$");
    private static final Pattern _VERSIONS_PATTERN = Pattern.compile("^\\d+(\\.\\d+){0,3}$");

    /**
     * Private constructor, no instanciation.
     * <p/>
     * Created on Feb 7, 2011 by Fabian
     */
    private SeerUtils() {
    }

    /**
     * Compares the two provided SEER version.
     * <br/><br/>
     * If one of the versions doesn't follow the allowed format, an exception is thrown.
     * @param version1 first version to compare
     * @param version2 second version to compare
     * @return a negative integer if first version is smaller than second one, a positive integer if first version is greater than second one, 0 if they are the same
     */
    public static int compareSeerVersions(String version1, String version2) {
        if (version1 == null)
            return -1;
        if (version2 == null)
            return 1;

        String v1 = _VERSION_CLEANUP_PATTERN.matcher(version1.toLowerCase()).replaceAll("");
        String v2 = _VERSION_CLEANUP_PATTERN.matcher(version2.toLowerCase()).replaceAll("");

        if (!_VERSIONS_PATTERN.matcher(v1).matches())
            throw new RuntimeException("Invalid version format: " + v1);
        if (!_VERSIONS_PATTERN.matcher(v2).matches())
            throw new RuntimeException("Invalid version format: " + v2);

        String[] parts1 = StringUtils.split(v1, '.');
        String[] parts2 = StringUtils.split(v2, '.');

        List<Integer> list1 = new ArrayList<>(), list2 = new ArrayList<>();
        for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
            if (i < parts1.length)
                list1.add(Integer.valueOf(parts1[i]));
            else
                list1.add(0);
        }
        for (int i = 0; i < Math.max(parts1.length, parts2.length); i++) {
            if (i < parts2.length)
                list2.add(Integer.valueOf(parts2[i]));
            else
                list2.add(0);
        }

        for (int i = 0; i < list1.size(); i++) {
            int result = list2.get(i).compareTo(list1.get(i)) * -1;
            if (result != 0)
                return result;
        }

        return 0;
    }

    /**
     * Returns true if the provided string contains only printable ASCII characters, false otherwise.
     * <p/>
     * See http://www.asciitable.com/
     * <p/>
     * Considering byte values from -127 to 128, only the range 32-126 is considered printable ASCII with the following exceptions:
     * <ul>
     * <li>9 (tab)</li>
     * <li>10 (line feed)</li>
     * <li>13 (carriage return)</li>
     * </ul>
     * <p/>
     * This method is identical to <code>isPureAscii(s.getBytes())</code>.
     * <p/>
     * Created on Jan 2, 2012 by depryf
     * @param s String to check, the method returns true if it is null or empty
     * @return true if the provided string contains only printable ASCII characters, false otherwise
     */
    public static boolean isPureAscii(String s) {
        return s == null || isPureAscii(s.getBytes(), null);
    }

    /**
     * Returns true if the provided array of bytes contains only printable ASCII characters, false otherwise.
     * <p/>
     * See http://www.asciitable.com/
     * <p/>
     * Considering byte values from -127 to 128, only the range 32-126 is considered printable ASCII with the following exceptions:
     * <ul>
     * <li>9 (tab)</li>
     * <li>10 (line feed)</li>
     * <li>13 (carriage return)</li>
     * </ul>
     * <p/>
     * This method is identical to <code>isPureAscii(bytes, null)</code>.
     * <p/>
     * Created on Jan 2, 2012 by depryf
     * @param bytes array of byes to check, the method returns true if the array is null or empty
     * @return boolean true if the provided array of bytes contains only printable ASCII characters, false otherwise
     */
    public static boolean isPureAscii(byte[] bytes) {
        return isPureAscii(bytes, null);
    }

    /**
     * Returns true if the provided array of bytes contains only printable pure-ASCII characters, false otherwise.
     * <p/>
     * see <url>http://www.asciitable.com/<url>
     * <p/>
     * Considering byte values from -127 to 128, only the range 32-126 is considered printable ASCII with the following exceptions:
     * <ul>
     * <li>9 (tab)</li>
     * <li>10 (line feed)</li>
     * <li>13 (carriage return)</li>
     * </ul>
     * <p/>
     * Created on Feb 25, 2006 by depry
     * @param bytes array of byes to check, the method returns true if the array is null or empty
     * @param exceptions optional array of exceptions (can be null or empty); for example pass <code>new byte[] {26}</code> to allow the ASCII control character "substitue".
     * @return boolean true if the provided array of bytes contains only printable ASCII characters, false otherwise
     */
    public static boolean isPureAscii(byte[] bytes, byte[] exceptions) {
        if (bytes == null || bytes.length == 0)
            return true;

        for (byte b : bytes)
            if ((b < 32 && b != 9 && b != 10 && b != 13) || b == 127)
                if (exceptions == null || exceptions.length == 0 || !ArrayUtils.contains(exceptions, b))
                    return false;

        return true;
    }

    /**
     * Copies the content of the given input stream to the the given output stream
     * <p/>
     * Both the input stream and the output stream will be closed when this method returns.
     * <p/>
     * Created on May 27, 2004 by Fabian Depry
     * @param input where to take the data from
     * @param output where to send the data to
     * @throws IOException if data cannot be copied from input to output
     */
    public static void copyInputStreamToOutputStream(InputStream input, OutputStream output) throws IOException {
        copyInputStreamToOutputStream(input, output, true);
    }

    /**
     * Copies the content of the given input stream to the the given output stream; no assumption is made on the encoding of the streams.
     * <p/>
     * The input stream will be closed when this method returns; the output stream will be closed only if closeOutput is set to true
     * <p/>
     * Created on May 27, 2004 by Fabian Depry
     * @param input where to take the data from
     * @param output where to send the data to
     * @param closeOutput whether or not the output stream should be closed
     * @throws IOException if data cannot be copied from input to output
     */
    public static void copyInputStreamToOutputStream(InputStream input, OutputStream output, boolean closeOutput) throws IOException {
        if (input == null)
            throw new IOException("Input Stream is null");
        if (output == null)
            throw new IOException("Output Stream is null");

        // delegate the work to the IOUtils class...
        IOUtils.copyLarge(input, output);

        output.flush();

        input.close();
        if (closeOutput)
            output.close();
    }

    /**
     * Copies the content of the given reader to the the given writer.
     * <p/>
     * Both readers will be closed when this method returns.
     * <p/>
     * Created on May 27, 2004 by Fabian Depry
     * @param reader where to take the data from
     * @param writer where to send the data to
     * @throws IOException if data cannot be copied from input to output
     */
    public static void copyReaderToWriter(Reader reader, Writer writer) throws IOException {
        copyReaderToWriter(reader, writer, true);
    }

    /**
     * Copies the content of the given reader to the the given writer making no assumption on the encoding.
     * <p/>
     * The input reader will be closed when this method returns; the output writer will be closed only if closeOutput is set to true
     * <p/>
     * Created on May 27, 2004 by Fabian Depry
     * @param input where to take the data from
     * @param output where to send the data to
     * @param closeOutput whether or not the output writer should be closed
     * @throws IOException if data cannot be copied from input to output
     */
    public static void copyReaderToWriter(Reader input, Writer output, boolean closeOutput) throws IOException {
        if (input == null)
            throw new IOException("Input Reader is null");
        if (output == null)
            throw new IOException("Output Writer is null");

        // delegate the work to the IOUtils class...
        IOUtils.copyLarge(input, output);

        output.flush();

        input.close();
        if (closeOutput)
            output.close();
    }

    /**
     * Reads and returns the content of the request file using the OS-specific encoding.
     * <p/>
     * Created on Aug 17, 2010 by depryf
     * @param file <code>File</code> to read
     * @return the content of the request file
     */
    public static String readFile(File file) throws IOException {
        return readFile(file, null);
    }

    /**
     * Reads and returns the content of the request file using the provided encoding.
     * <p/>
     * Created on Aug 17, 2010 by depryf
     * @param file <code>File</code> to read
     * @param encoding encoding to use
     * @return the content of the request file
     */
    public static String readFile(File file, String encoding) throws IOException {
        if (file == null || !file.exists())
            throw new IOException("File does not exist.");

        Writer writer = new StringWriter();
        try (InputStream is = createInputStream(file)) {
            IOUtils.copy(is, writer, encoding);
        }

        return writer.toString();
    }

    /**
     * Reads and returns the content of the request URL using the default OS-specific encoding.
     * <p/>
     * Created on Aug 17, 2010 by depryf
     * @param url the <code>URL</code> to read
     * @return the content of the request URL
     */
    public static String readUrl(URL url) throws IOException {
        return readUrl(url, null);
    }

    /**
     * Reads and returns the content of the request URL using the provided encoding.
     * <p/>
     * Created on Aug 17, 2010 by depryf
     * @param url the <code>URL</code> to read
     * @param encoding encoding to use
     * @return the content of the request URL
     */
    public static String readUrl(URL url, String encoding) throws IOException {
        if (url == null)
            throw new IOException("URL is null");

        StringWriter writer = new StringWriter();
        try (InputStream is = url.openStream()) {
            IOUtils.copy(is, writer, encoding);
        }

        return writer.toString();
    }

    /**
     * Writes the given string to the given file.
     * <p/>
     * Created on Mar 2, 2012 by Fabian
     * @param input input string
     * @param file target file
     */
    public static void writeFile(String input, File file) throws IOException {
        writeFile(input, file, null);
    }

    /**
     * Writes the given string to the given file.
     * <p/>
     * Created on Mar 2, 2012 by Fabian
     * @param input input string
     * @param file target file
     * @param encoding encoding to use
     */
    public static void writeFile(String input, File file, String encoding) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            IOUtils.copy(new StringReader(input), fos, encoding);
        }
    }

    /**
     * Creates an <code>InputStream</code> for the provided file based on its extension:
     * <ul>
     * <li>if it ends with '.gz' or '.gzip', it will be considered as a GZipped file</li>
     * <li>if it ends with '.zip', it will be considered as a Zipped file but an error will be generated if it contains more than one entry</li>
     * <li>otherwise it is considered as a non-compressed file</li>
     * </ul>
     * <p/>
     * Created on Sep 19, 2011 by depryf
     * @param file <code>File</code>, cannot be null (an exception will be thrown if it does not exist)
     * @return an <code>InputStream</code>, never null
     */
    public static InputStream createInputStream(File file) throws IOException {
        return createInputStream(file, null);
    }

    /**
     * Creates an <code>InputStream</code> for the provided file based on its extension:
     * <ul>
     * <li>if it ends with '.gz' or '.gzip', it will be considered as a GZipped file</li>
     * <li>if it ends with '.zip', it will be considered as a Zipped file:
     * <ul>
     * <li>If the file contains no entry, an exception is generated</li>
     * <li>If the file contains a single entry, a stream to that entry will be returned</li>
     * <li>If the file contains more than one entry and zipEntryToUse was provided, a stream to that entry will be returned</li>
     * <li>Otherwise an IOException will be generated</li>
     * </ul>
     * </li>
     * <li>otherwise it is considered as a non-compressed file</li>
     * </ul>
     * <p/>
     * Created on Sep 19, 2011 by depryf
     * @param file <code>File</code>, cannot be null (an exception will be thrown if it does not exist)
     * @param zipEntryToUse if the zip file contains more than one entry
     * @return an <code>InputStream</code>, never null
     */
    @SuppressWarnings("resource")
    public static InputStream createInputStream(File file, String zipEntryToUse) throws IOException {
        if (file == null || !file.exists())
            throw new IOException("File does not exist.");

        String name = file.getName().toLowerCase();

        InputStream is;
        if (name.endsWith(".gz") || name.endsWith(".gzip"))
            is = new GZIPInputStream(new FileInputStream(file));
        else if (name.endsWith(".zip")) {
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            // count the number of entries
            List<String> list = new ArrayList<>();
            while (entries.hasMoreElements())
                list.add(entries.nextElement().getName());
            // can't be empty
            if (list.isEmpty())
                throw new IOException("Zip file is empty.");
            InputStream tmp;
            // if only one, just take that one...
            if (list.size() == 1)
                zipEntryToUse = list.get(0);

            if (list.contains(zipEntryToUse))
                tmp = zipFile.getInputStream(zipFile.getEntry(zipEntryToUse));
            else
                throw new IOException("Zip file contains more than one file.");

            // zip file could contain another compressed file; we are only supporting gzip or uncompressed!
            if ((zipEntryToUse.endsWith(".gz") || zipEntryToUse.endsWith(".gzip")))
                is = new GZIPInputStream(tmp);
            else if (zipEntryToUse.endsWith(".zip"))
                throw new IOException("Zip files inside zip files is not supported.");
            else
                is = tmp;
        }
        else
            is = new FileInputStream(file);

        return is;
    }

    /**
     * Creates an <code>OutputStream</code> for the provided file based on its extension:
     * <ul>
     * <li>if it ends with '.gz' or '.gzip', it will be considered as a GZipped file</li>
     * <li>if it ends with '.zip', it will be considered as a Zipped file (the caller is responsible for adding the entries)</li>
     * <li>otherwise it is considered as a non-compressed file</li>
     * </ul>
     * <p/>
     * Created on Sep 19, 2011 by depryf
     * @param file <code>File</code>, cannot be null (an exception will be thrown if it does not exist)
     * @return an <code>OutputStream</code>, never null
     */
    public static OutputStream createOutputStream(File file) throws IOException {
        OutputStream os;

        String name = file.getName().toLowerCase();

        if (name.endsWith(".gz") || name.endsWith(".gzip"))
            os = new GZIPOutputStream(new FileOutputStream(file));
        else if (name.endsWith(".zip"))
            os = new ZipOutputStream(new FileOutputStream(file));
        else
            os = new FileOutputStream(file);

        return os;
    }

    /**
     * Pad the passed value up to the passed length using the passed string
     * <p/>
     * Created on Dec 3, 2008 by depryf
     * @param value value to pad
     * @param length length of the result
     * @param with character to pad with
     * @param leftPad if true value will be left padded, otherwise it will be right padded
     * @return padded value, maybe null
     */
    public static String pad(String value, int length, String with, boolean leftPad) {
        if (value == null || value.length() >= length)
            return value;

        StringBuilder builder = new StringBuilder(value);
        while (builder.length() < length)
            if (leftPad)
                builder.insert(0, with);
            else
                builder.append(with);

        return builder.toString();
    }

    /**
     * Trims only the left portion of the string.
     * e.g.  "  97  " becomes "97  ".
     * <p/>
     * Created on Jul 19, 2011 by murphyr
     * @param value String that needs to be trimmed, can be null or empty
     * @return left trimmed version of input String, return null if the incoming one is null
     */
    public static String trimLeft(String value) {
        if (value == null || value.isEmpty())
            return value;
        char[] val = value.toCharArray();
        int st = 0;
        while ((st < val.length) && (val[st] <= ' '))
            st++;

        return value.substring(st);
    }

    /**
     * Trims only the right portion of the string.
     * e.g.  "  97  " becomes "  97"
     * <p/>
     * Created on Jul 19, 2011 by murphyr
     * @param value String that needs to be trimmed, can be null or empty
     * @return right trimmed version of input String, return null if the incoming one is null
     */
    public static String trimRight(String value) {
        if (value == null || value.isEmpty())
            return value;
        char[] val = value.toCharArray();
        int end = val.length;
        while ((end > 0) && (val[end - 1] <= ' '))
            end--;

        return value.substring(0, end);
    }

    /**
     * Format the passed number, added commas for the decimal parts.
     * <p/>
     * Created on Dec 3, 2008 by depryf
     * @param num number to format
     * @return formatted number
     */
    public static String formatNumber(int num) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);
        return format.format(num);
    }

    /**
     * Formats a time given in millisecond. The output will be "X hours Y min Z sec", unless X, Y or Z is 0 in which
     * case that part of the string will be omitted.
     * <p/>
     * Created on May 3, 2004 by Fabian Depry
     * @param timeInMilli time in milli-seconds
     * @return a <code>String</code> representing the formatted time...
     */
    public static String formatTime(long timeInMilli) {
        long hourBasis = 60;

        StringBuilder formattedTime = new StringBuilder();

        long secTmp = timeInMilli / 1000;
        long sec = secTmp % hourBasis;
        long minTmp = secTmp / hourBasis;
        long min = minTmp % hourBasis;
        long hour = minTmp / hourBasis;

        if (hour > 0) {
            formattedTime.append(hour).append(" hour");
            if (hour > 1)
                formattedTime.append("s");
        }

        if (min > 0) {
            if (formattedTime.length() > 0)
                formattedTime.append(", ");
            formattedTime.append(min).append(" minute");
            if (min > 1)
                formattedTime.append("s");
        }

        if (sec > 0) {
            if (formattedTime.length() > 0)
                formattedTime.append(", ");
            formattedTime.append(sec).append(" second");
            if (sec > 1)
                formattedTime.append("s");
        }

        if (formattedTime.length() > 0)
            return formattedTime.toString();

        return "< 1 second";
    }

    /**
     * Takes a string with a byte count and converts it into a "nice" representation of size.
     * <p/>
     * 124 b <br>
     * 34 KB <br>
     * 12 MB <br>
     * 2 GB
     * <p/>
     * Created on May 281, 2004 by Chuck May
     * @param size size to format
     * @return <code>String</code> with the formatted size
     */
    public static String formatFileSize(long size) {
        if (size < 1024)
            return size + " B";
        else if (size < 1024 * 1024)
            return new DecimalFormat("#.# KB").format((double)size / 1024);
        else if (size < 1024 * 1024 * 1024)
            return new DecimalFormat("#.# MB").format((double)size / 1024 / 1024);

        return new DecimalFormat("#.# GB").format((double)size / 1024 / 1024 / 1024);
    }

    /**
     * Copies the given directory into the requested directory; any file and subfolders will be copied.
     * @param from source directory, must exist
     * @param to target directory, if it doesn't exist, it will be created
     */
    public static void copyDirectory(File from, File to) throws IOException {
        if (!from.exists())
            throw new IOException("Source directory does not exist.");
        if (!from.isDirectory())
            throw new IOException("Source is not a directory.");
        if (!to.exists())
            if (!to.mkdirs())
                throw new IOException("Unable to create '" + to.getPath() + "'");
        if (!to.isDirectory())
            throw new IOException("Target is not a directory.");

        File[] files = from.listFiles();
        if (files == null)
            throw new IOException("Unable to read from source directory");

        for (File f : files) {
            if (f.isFile())
                copyInputStreamToOutputStream(new FileInputStream(f), new FileOutputStream(new File(to, f.getName())));
            else
                copyDirectory(f, new File(to, from.getName()));
        }
    }

    /**
     * Deletes the requested directory and anything it contains.
     * @param dir directory to delete, must exist
     */
    public static void deleteDirectory(File dir) throws IOException {
        deleteDirectory(dir, true);
    }

    /**
     * Empties the requested directory; after the call the directory will still exists but will be empty.
     * @param dir directory to empty, must exist
     */
    public static void emptyDirectory(File dir) throws IOException {
        deleteDirectory(dir, false);
    }

    private static void deleteDirectory(File dir, boolean deleteRoot) throws IOException {
        if (!dir.exists())
            return;
        if (!dir.isDirectory())
            throw new IOException("File is not a directory.");

        File[] files = dir.listFiles();
        if (files == null)
            throw new IOException("Unable to read from source directory");

        for (File f : files) {
            if (f.isFile()) {
                if (!f.delete())
                    throw new IOException("Unable to delete '" + f.getPath() + "'");
            }
            else
                deleteDirectory(f, true);
        }

        if (deleteRoot)
            if (!dir.delete())
                throw new IOException("Unable to delete '" + dir.getPath() + "'");
    }

    /**
     * Zips the provided file to the requested zip file. If the file is a directory, the entire content will be zipped.
     * @param file file to zip (can be a directory or a file), must exist
     * @param to the zip file to create, it must end with the extension '.zip'
     */
    public static void zipFile(File file, File to) throws IOException {
        zipFiles(Collections.singletonList(file), to);
    }

    /**
     * Zips the provided files to the requested file. If any file is a directory, the entire content will be zipped.
     * @param files files to zip (can be a directories or a files), must exist
     * @param to the zip file to create, it must end with the extension '.zip'
     */
    public static void zipFiles(List<File> files, File to) throws IOException {
        if (!to.getName().toLowerCase().endsWith(".zip"))
            throw new IOException("Target file must end with 'zip'.");

        try (FileOutputStream fos = new FileOutputStream(to); ZipOutputStream zipOutput = new ZipOutputStream(fos)) {
            for (File file : files) {
                if (!file.exists())
                    throw new IOException("Source directory does not exist.");
                internalZip(file, zipOutput, file.getParentFile().getAbsolutePath().length());
            }
        }
    }

    private static void internalZip(File file, ZipOutputStream zipOutput, int topDirLength) throws IOException {
        String relative = file.getAbsolutePath().substring(topDirLength).replace('\\', '/').substring(1);
        if (file.isDirectory() && !relative.endsWith("/"))
            relative += "/";
        zipOutput.putNextEntry(new ZipEntry(relative));
        if (file.isFile())
            copyInputStreamToOutputStream(new FileInputStream(file), zipOutput, false);
        else {
            File[] files = file.listFiles();
            if (files != null)
                for (File f : files)
                    internalZip(f, zipOutput, topDirLength);
        }
    }

    /**
     * Unzips the provided zip file in the requested directory.
     * @param from zip file to unzip, must exist and be a valid zip file
     * @param to target folder where to unzip the file
     */
    public static void unzipFile(File from, File to) throws IOException {
        if (!from.exists())
            throw new IOException("Source file does not exist.");
        if (!from.isFile())
            throw new IOException("Source is not a file.");
        if (!to.exists())
            if (!to.mkdirs())
                throw new IOException("Unable to create '" + to.getPath() + "'");
        if (!to.isDirectory())
            throw new IOException("Target is not a directory.");

        try (ZipFile file = new ZipFile(from); FileInputStream fis = new FileInputStream(from); ZipInputStream zipInput = new ZipInputStream(fis)) {
            ZipEntry entry = zipInput.getNextEntry();
            while (entry != null) {
                File target = new File(to, entry.getName());
                if (!target.getParentFile().exists())
                    if (!target.getParentFile().mkdirs())
                        throw new IOException("Unable to create '" + target.getParentFile().getPath() + "'");

                if (entry.isDirectory()) {
                    if (!target.mkdirs())
                        throw new IOException("Unable to create '" + target.getPath() + "'");
                }
                else {
                    FileOutputStream fos = new FileOutputStream(target);
                    copyInputStreamToOutputStream(file.getInputStream(entry), fos);
                    fos.close();
                }

                entry = zipInput.getNextEntry();
            }
        }
    }

    public static String getWorkingDirectory() {
        return System.getProperty("user.dir").replace(".idea\\modules\\", "");
    }
}
