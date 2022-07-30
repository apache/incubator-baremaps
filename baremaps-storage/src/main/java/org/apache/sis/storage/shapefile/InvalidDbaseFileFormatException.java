package org.apache.sis.storage.shapefile;

import org.apache.sis.storage.DataStoreException;

/**
 * Thrown when the DBF file format seems to be invalid.
 *
 * <div class="warning">This is an experimental class,
 * not yet target for any Apache SIS release at this time.</div>
 *
 * @author Marc LE BIHAN
 * @version 0.6
 * @since   0.6
 * @module
 */
public class InvalidDbaseFileFormatException extends DataStoreException {
    /** Serial ID. */
    private static final long serialVersionUID = 7152705402305259568L;

    /**
     * Construct an exception.
     * @param message Message of the exception.
     */
    public InvalidDbaseFileFormatException(String message) {
        super(message);
    }

    /**
     * Construct an exception.
     * @param message Message of the exception.
     * @param cause Root cause of the exception.
     */
    public InvalidDbaseFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
