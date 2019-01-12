package gazetteer.osm.model;

import java.util.List;

public class Relation implements Entity {

    private final long id;

    private final int version;

    private final int uid;

    private final String user;

    private final long timestamp;

    private final long changeset;

    private final List<Member> members;

    private final List<String> keys;

    private final List<String> vals;

    public Relation(long id, int version, int uid, String user, long timestamp, long changeset, List<Member> members, List<String> keys, List<String> vals) {
        this.id = id;
        this.version = version;
        this.uid = uid;
        this.user = user;
        this.timestamp = timestamp;
        this.changeset = changeset;
        this.members = members;
        this.keys = keys;
        this.vals = vals;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public int getUid() {
        return uid;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public long getChangeset() {
        return changeset;
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<String> getKeys() {
        return keys;
    }

    public List<String> getVals() {
        return vals;
    }
}
