package gazetteer.osm.model;

public interface Entity {

    long getId();
    int getVersion();
    int getUid();
    String getUser();
    long getTimestamp();
    long getChangeset();

}
