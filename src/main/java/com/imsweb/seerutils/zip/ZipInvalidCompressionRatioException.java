/*
 * Copyright (C) 2022 Information Management Services, Inc.
 */
package com.imsweb.seerutils.zip;

import java.io.IOException;

public class ZipInvalidCompressionRatioException extends IOException {

    /**
     * Constructs an {@code ZipInvalidCompressionRatioException} with a given message
     * {@code String}.  No underlying cause is set;
     * {@code getCause} will return {@code null}.
     * @param message the error message.
     * @see #getMessage
     */
    public ZipInvalidCompressionRatioException(String message) {
        super(message);
    }
}
