/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.internal.shapefile.jdbc;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;

/**
 * The Abstract Byte Reader.
 * @author  Marc Le Bihan
 * @version 0.5
 * @since   0.5
 * @module
 */
abstract class AbstractDbase3ByteReader extends CommonByteReader<SQLInvalidDbaseFileFormatException, SQLDbaseFileNotFoundException> implements Dbase3ByteReader {
    /** First data record position, in bytes. */
    protected short firstRecordPosition;

    /** Size of one record, in bytes. */
    protected short recordLength;

    /** Reserved (dBASE IV) Filled with 00h. */
    protected byte[] reservedFiller1 = new byte[2];

    /**
     * Reserved : Incomplete transaction (dBASE IV).
     * 00h : Transaction ended (or rolled back).
     * 01h : Transaction started.
     */
    protected byte reservedIncompleteTransaction;

    /**
     * Reserved : Encryption flag (dBASE IV).
     * 00h : Not encrypted.
     * 01h : Data encrypted.
     */
    protected byte reservedEncryptionFlag;

    /** Reserved : Free record thread (for LAN only). */
    protected byte[] reservedFreeRecordThread = new byte[4];

    /** Reserved : For multi-user (DBase 3+). */
    protected byte[] reservedMultiUser = new byte[8];

    /** Reserved : MDX flag (dBASE IV). */
    protected byte reservedMDXFlag;

    /** Binary code page value. */
    protected byte codePage;

    /** Reserved (dBASE IV) Filled with 00h. */
    protected byte[] reservedFiller2 = new byte[2];

    /** Marks the end of the descriptor : must be 0x0D. */
    protected byte descriptorTerminator;

    /** Valid dBASE III PLUS table file (03h without a memo .DBT file; 83h with a memo). */
    protected byte dbaseVersion;

    /** Number of records in the table. */
    protected int rowCount;

    /** Database charset. */
    protected Charset charset;

    /** Date of last update; in YYMMDD format. */
    protected byte[] dbaseLastUpdate = new byte[3];

    /**
     * Map a dbf file.
     * @param file Database file.
     * @throws SQLDbaseFileNotFoundException if the DBF file has not been found.
     * @throws SQLInvalidDbaseFileFormatException if the database has an invalid format.
     */
    public AbstractDbase3ByteReader(File file) throws SQLDbaseFileNotFoundException, SQLInvalidDbaseFileFormatException {
        super(file, SQLInvalidDbaseFileFormatException.class, SQLDbaseFileNotFoundException.class);
    }

    /**
     * Returns the charset.
     * @return Charset.
     */
    @Override public Charset getCharset() {
        return this.charset;
    }

    /**
     * Returns the database last update date.
     * @return Date of the last update.
     */
    @Override public Date getDateOfLastUpdate() {
        return toDate(this.dbaseLastUpdate);
    }

    /**
     * Returns the first record position, in bytes, in the DBase file.
     * @return First record position.
     */
    @Override public short getFirstRecordPosition() {
        return this.firstRecordPosition;
    }

    /**
     * Returns the length (in bytes) of one record in this DBase file, including the delete flag.
     * @return Record length.
     */
    @Override public short getRecordLength() {
        return this.recordLength;
    }

    /**
     * Returns the record count.
     * @return Record count.
     */
    @Override public int getRowCount() {
        return this.rowCount;
    }

    /**
     * Convert the binary code page value of the Dbase 3 file to a recent Charset.
     * @param codePageBinaryValue page code binary value.
     * @return Charset.
     * @throws UnsupportedCharsetException if the code page as no representation in recents Charset (legacy DOS or macintosh charsets).
     */
    protected Charset toCharset(byte codePageBinaryValue) throws UnsupportedCharsetException {
        // Attempt to find a known conversion.
        String dbfCodePage = toCodePage(codePageBinaryValue);

        // If no conversion has been found, decide if the cause is an unsupported value or an illegal value to choose the good exception to return.
        if (dbfCodePage == null) {
            switch(Byte.toUnsignedInt(codePageBinaryValue)) {
                case 0x04: dbfCodePage = "unsupported"; break;
                case 0x68: dbfCodePage = "unsupported"; break; // Kamenicky (Czech) MS-DOS
                case 0x69: dbfCodePage = "unsupported"; break; // Mazovia (Polish) MS-DOS
                case 0x96: dbfCodePage = "unsupported"; break; // russian mac
                case 0x97: dbfCodePage = "unsupported"; break; // eastern european macintosh
                case 0x98: dbfCodePage = "unsupported"; break; // greek macintosh
                case 0xC8: dbfCodePage = "unsupported"; break; // windows ee
                default: dbfCodePage = "unsupported"; break;
            }
        }

        assert dbfCodePage != null;

        // If the code page cannot find a match for a more recent Charset, we wont be able to handle this DBF.
        if (dbfCodePage.equals("unsupported")) {
            throw new UnsupportedCharsetException("Unsupported codepage");
        }

        try {
            return Charset.forName(dbfCodePage);
        }
        catch(IllegalArgumentException e) {
            // If this happens here, it means that we have selected a wrong charset. We have a bug.
            throw new RuntimeException("Wrong charset selection");
        }
    }

    /**
     * Return a Charset code page from a binary code page value.
     * @param pageCodeBinaryValue binary code page value.
     * @return Page code.
     */
    private String toCodePage(byte pageCodeBinaryValue) {
        // From http://trac.osgeo.org/gdal/ticket/2864
        HashMap<Integer, String> knownConversions = new HashMap<>();
        knownConversions.put(0x01, "cp437"); //  U.S. MS–DOS
        knownConversions.put(0x02, "cp850"); // International MS–DOS
        knownConversions.put(0x03, "cp1252"); // Windows ANSI
        knownConversions.put(0x08, "cp865"); //  Danish OEM
        knownConversions.put(0x09, "cp437"); //  Dutch OEM
        knownConversions.put(0x0a, "cp850"); //  Dutch OEM*
        knownConversions.put(0x0b, "cp437"); //  Finnish OEM
        knownConversions.put(0x0d, "cp437"); //  French OEM
        knownConversions.put(0x0e, "cp850"); //  French OEM*
        knownConversions.put(0x0f, "cp437"); //  German OEM
        knownConversions.put(0x10, "cp850"); //  German OEM*
        knownConversions.put(0x11, "cp437"); //  Italian OEM
        knownConversions.put(0x12, "cp850"); //  Italian OEM*
        knownConversions.put(0x13, "cp932"); //  Japanese Shift-JIS
        knownConversions.put(0x14, "cp850"); //  Spanish OEM*
        knownConversions.put(0x15, "cp437"); //  Swedish OEM
        knownConversions.put(0x16, "cp850"); //  Swedish OEM*
        knownConversions.put(0x17, "cp865"); //  Norwegian OEM
        knownConversions.put(0x18, "cp437"); //  Spanish OEM
        knownConversions.put(0x19, "cp437"); //  English OEM (Britain)
        knownConversions.put(0x1a, "cp850"); //  English OEM (Britain)*
        knownConversions.put(0x1b, "cp437"); //  English OEM (U.S.)
        knownConversions.put(0x1c, "cp863"); //  French OEM (Canada)
        knownConversions.put(0x1d, "cp850"); //  French OEM*
        knownConversions.put(0x1f, "cp852"); //  Czech OEM
        knownConversions.put(0x22, "cp852"); //  Hungarian OEM
        knownConversions.put(0x23, "cp852"); //  Polish OEM
        knownConversions.put(0x24, "cp860"); //  Portuguese OEM
        knownConversions.put(0x25, "cp850"); //  Portuguese OEM*
        knownConversions.put(0x26, "cp866"); //  Russian OEM
        knownConversions.put(0x37, "cp850"); //  English OEM (U.S.)*
        knownConversions.put(0x40, "cp852"); //  Romanian OEM
        knownConversions.put(0x4d, "cp936"); //  Chinese GBK (PRC)
        knownConversions.put(0x4e, "cp949"); //  Korean (ANSI/OEM)
        knownConversions.put(0x4f, "cp950"); //  Chinese Big5 (Taiwan)
        knownConversions.put(0x50, "cp874"); //  Thai (ANSI/OEM)
        knownConversions.put(0x57, "cp1252"); // ANSI
        knownConversions.put(0x58, "cp1252"); // Western European ANSI
        knownConversions.put(0x59, "cp1252"); // Spanish ANSI
        knownConversions.put(0x64, "cp852"); //  Eastern European MS–DOS
        knownConversions.put(0x65, "cp866"); //  Russian MS–DOS
        knownConversions.put(0x66, "cp865"); //  Nordic MS–DOS
        knownConversions.put(0x67, "cp861"); //  Icelandic MS–DOS
        knownConversions.put(0x6a, "cp737"); //  Greek MS–DOS (437G)
        knownConversions.put(0x6b, "cp857"); //  Turkish MS–DOS
        knownConversions.put(0x6c, "cp863"); //  French–Canadian MS–DOS
        knownConversions.put(0x78, "cp950"); //  Taiwan Big 5
        knownConversions.put(0x79, "cp949"); //  Hangul (Wansung)
        knownConversions.put(0x7a, "cp936"); //  PRC GBK
        knownConversions.put(0x7b, "cp932"); //  Japanese Shift-JIS
        knownConversions.put(0x7c, "cp874"); //  Thai Windows/MS–DOS
        knownConversions.put(0x86, "cp737"); //  Greek OEM
        knownConversions.put(0x87, "cp852"); //  Slovenian OEM
        knownConversions.put(0x88, "cp857"); //  Turkish OEM
        knownConversions.put(0xc8, "cp1250"); // Eastern European Windows
        knownConversions.put(0xc9, "cp1251"); // Russian Windows
        knownConversions.put(0xca, "cp1254"); // Turkish Windows
        knownConversions.put(0xcb, "cp1253"); // Greek Windows
        knownConversions.put(0xcc, "cp1257"); // Baltic Windows

        return(knownConversions.get(Byte.toUnsignedInt(pageCodeBinaryValue)));
    }

    /**
     * Set a charset.
     * @param cs Charset.
     */
    public void setCharset(Charset cs) {
        this.charset = cs;
    }

    /**
     * Return a date from a byte array.
     * @param yymmdd byte[3] with byte[0] = year (2 digits), [1] = month, [2] = day.
     * @return Date.
     */
    private Date toDate(byte[] yymmdd) {
        Objects.requireNonNull(yymmdd, "the yymmdd bytes cannot be null");

        if (yymmdd.length != 3)
            throw new IllegalArgumentException(MessageFormat.format("Database:toDate() works only on a 3 bytes YY MM DD date. this array has {0} length", yymmdd.length));

        Objects.requireNonNull(yymmdd[0], "the year byte cannot be null");
        Objects.requireNonNull(yymmdd[1], "the month byte cannot be null");
        Objects.requireNonNull(yymmdd[2], "the day byte cannot be null");

        int year = yymmdd[0] < 70 ? 100 + yymmdd[0] : yymmdd[0];
        int month = yymmdd[1];
        int day = yymmdd[2];

        @SuppressWarnings("deprecation") // But everything is deprecated in DBF files...
        Date date = new Date(year, month, day);
        return date;
    }
}
