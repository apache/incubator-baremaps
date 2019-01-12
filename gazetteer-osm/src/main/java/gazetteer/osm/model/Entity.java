package gazetteer.osm.model;

public interface Entity {

    int NO_VERSION = -1;
    int NO_UID = -1;
    String NO_USER = "";
    long NO_TIMESTAMP = -1;
    long NO_CHANGESET = -1;

    long getId();
    int getVersion();
    int getUid();
    String getUser();
    long getTimestamp();
    long getChangeset();

}
