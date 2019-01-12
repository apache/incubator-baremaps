package gazetteer.osm.model;

public class Info {

    private final int uid;

    private final String user;

    private final long timestamp;

    private final long changeset;

    public Info(int uid, String user, long timestamp, long changeset) {
        this.uid = uid;
        this.user = user;
        this.timestamp = timestamp;
        this.changeset = changeset;
    }

    public int getUid() {
        return uid;
    }

    public String getUser() {
        return user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getChangeset() {
        return changeset;
    }

}
