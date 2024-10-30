package org.apache.baremaps.storage.csv;

import org.apache.baremaps.data.storage.DataSchema;
import org.apache.baremaps.data.storage.DataStore;
import org.apache.baremaps.data.storage.DataStoreException;
import org.apache.baremaps.data.storage.DataTable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * A DataStore implementation that manages a single CSV file.
 */
public class CsvDataStore implements DataStore {

    private final String tableName;
    private final DataSchema schema;
    private final CsvDataTable dataTable;

    /**
     * Constructs a CsvDataStore with the specified table name, schema, and CSV file.
     *
     * @param tableName the name of the table
     * @param schema    the data schema defining the structure
     * @param csvFile   the CSV file to read data from
     * @param hasHeader whether the CSV file includes a header row
     * @param separator the character used to separate columns in the CSV file
     * @throws IOException if an I/O error occurs
     */
    public CsvDataStore(String tableName, DataSchema schema, File csvFile, boolean hasHeader, char separator) throws IOException {
        this.tableName = tableName;
        this.schema = schema;
        this.dataTable = new CsvDataTable(schema, csvFile, hasHeader, separator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> list() throws DataStoreException {
        return Collections.singletonList(tableName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataTable get(String name) throws DataStoreException {
        if (this.tableName.equals(name)) {
            return dataTable;
        } else {
            throw new DataStoreException("Table '" + name + "' not found.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(DataTable table) throws DataStoreException {
        throw new UnsupportedOperationException("Adding tables is not supported in CsvDataStore.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(String name, DataTable table) throws DataStoreException {
        throw new UnsupportedOperationException("Adding tables is not supported in CsvDataStore.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(String name) throws DataStoreException {
        throw new UnsupportedOperationException("Removing tables is not supported in CsvDataStore.");
    }
}
