/*
 * Copyright (C) 2022 Information Management Services, Inc.
 */
package com.imsweb.seerutils.zip;

import java.io.IOException;

public class ZipEntryTooLargeException extends IOException {

    /**
     * Constructs an {@code ZipEntryTooLargeException} with a given message
     * {@code String}.  No underlying cause is set;
     * {@code getCause} will return {@code null}.
     * @param message the error message.
     * @see #getMessage
     */
    public ZipEntryTooLargeException(String message) {
        super(message);
    }
}
