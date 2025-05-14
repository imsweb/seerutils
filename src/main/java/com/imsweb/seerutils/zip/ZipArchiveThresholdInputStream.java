package com.imsweb.seerutils.zip;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.InputStreamStatistics;

@SuppressWarnings("unused")
public class ZipArchiveThresholdInputStream extends FilterInputStream {

    // don't alert for expanded sizes smaller than 100k
    private static final long _GRACE_ENTRY_SIZE = 100 * 1024L;

    private static final String _MAX_ENTRY_SIZE_MSG = "The file exceeded the maximum entry size allowed";

    private static final String _MIN_INFLATE_RATIO_MSG = "The file exceeded the maximum compression ratio allowed";

    private double _minInflateRatio;
    private long _maxEntrySize;

    /**
     * the reference to the current entry is only used for a more detailed log message in case of an error
     */
    private ZipArchiveEntry _entry;
    private boolean _guardState = true;

    public ZipArchiveThresholdInputStream(InputStream is) {
        super(is);
        _minInflateRatio = 0.01d;
        _maxEntrySize = 0xFFFFFFFFL;
    }

    /**
     * Sets the zip entry for a detailed logging
     * @param entry the entry
     */
    void setEntry(ZipArchiveEntry entry) {
        this._entry = entry;
    }

    void setMaxEntrySize(long maxEntrySize) {
        _maxEntrySize = maxEntrySize;
    }

    void setMinInflateRatio(double ratio) {
        _minInflateRatio = ratio;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b > -1)
            checkThreshold();
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int cnt = super.read(b, off, len);
        if (cnt > -1)
            checkThreshold();
        return cnt;
    }

    @Override
    public long skip(long n) throws IOException {
        long cnt = skipFully(super.in, n);
        if (cnt > 0)
            checkThreshold();
        return cnt;
    }

    /**
     * De-/activate threshold check.
     * A disabled guard might make sense, when POI is processing its own temporary data (see #59743)
     * @param guardState {@code true} (= default) enables the threshold check
     */
    public void setGuardState(boolean guardState) {
        this._guardState = guardState;
    }

    private void checkThreshold() throws IOException {
        if (!_guardState)
            return;

        if (!(in instanceof InputStreamStatistics))
            throw new IllegalArgumentException("InputStream of class " + in.getClass() + " is not implementing InputStreamStatistics.");

        final InputStreamStatistics stats = (InputStreamStatistics)in;
        final long payloadSize = stats.getUncompressedCount();

        long rawSize;
        try {
            rawSize = stats.getCompressedCount();
        }
        catch (NullPointerException e) {
            // this can happen with a very specially crafted file (see https://issues.apache.org/jira/browse/COMPRESS-598 for a related bug-report)
            // therefore we try to handle this gracefully for now this try/catch can be removed when COMPRESS-598 is fixed
            rawSize = 0;
        }

        // check the file size first, in case we are working on uncompressed streams; only check is max entry size is greater than 0
        if (_maxEntrySize > 0 && payloadSize > _maxEntrySize)
            throw new ZipEntryTooLargeException(_MAX_ENTRY_SIZE_MSG);

        // don't alert for small expanded size
        if (payloadSize <= _GRACE_ENTRY_SIZE)
            return;

        // check the inflate ratio if min inflate ratio is greater than zero
        double ratio = rawSize / (double)payloadSize;
        if (_minInflateRatio > 0.0d && ratio >= _minInflateRatio)
            return;

        // one of the limits was reached, report it
        throw new ZipInvalidCompressionRatioException(_MIN_INFLATE_RATIO_MSG);
    }

    ZipArchiveEntry getNextEntry() throws IOException {
        if (!(in instanceof ZipArchiveInputStream))
            throw new IllegalStateException("getNextEntry() is only allowed for stream based zip processing.");

        try {
            _entry = ((ZipArchiveInputStream)in).getNextEntry();
            return _entry;
        }
        catch (ZipException ze) {
            if (ze.getMessage().startsWith("Unexpected record signature"))
                throw new IllegalStateException("No valid entries or contents found, this is not a valid file", ze);

            throw ze;
        }
        catch (EOFException e) {
            return null;
        }
    }

    /**
     * Skips bytes from an input byte stream.
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up; this may not always be the case for
     * skip() implementations in subclasses of {@link InputStream}.
     * <p>
     * Note that the implementation uses {@link InputStream#read(byte[], int, int)} rather
     * than delegating to {@link InputStream#skip(long)}.
     * This means that the method may be considerably less efficient than using the actual skip implementation,
     * this is done to guarantee that the correct number of bytes are skipped.
     * <p>
     * This mimics POI's readFully(InputStream, byte[]).
     * <p>
     * If the end of file is reached before any bytes are read, returns {@code -1}. If
     * the end of the file is reached after some bytes are read, returns the
     * number of bytes read. If the end of the file isn't reached before {@code len}
     * bytes have been read, will return {@code len} bytes.
     *
     * <p>
     * Copied nearly verbatim from commons-io 41a3e9c
     * @param input byte stream to skip
     * @param toSkip number of bytes to skip.
     * @return number of bytes actually skipped.
     * @throws IOException if there is a problem reading the file
     * @throws IllegalArgumentException if toSkip is negative
     * @see InputStream#skip(long)
     */
    static long skipFully(final InputStream input, final long toSkip) throws IOException {
        final int skipBufferSize = 2048;

        if (toSkip < 0)
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        if (toSkip == 0)
            return 0L;

        /*
         * N.B. no need to synchronize this because: - we don't care if the buffer is created multiple times (the data
         * is ignored) - we always use the same size buffer, so if it it is recreated it will still be OK (if the buffer
         * size were variable, we would need to synch. to ensure some other thread did not create a smaller one)
         */
        byte[] skipByteBuffer = new byte[skipBufferSize];

        long remain = toSkip;
        while (remain > 0) {
            // See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
            final long n = input.read(skipByteBuffer, 0, (int)Math.min(remain, skipBufferSize));
            if (n < 0)  // EOF
                break;

            remain -= n;
        }

        if (toSkip == remain)
            return -1L;

        return toSkip - remain;
    }
}
