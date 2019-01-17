package io.gazetteer.osm.domain;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Info {

    private final long id;
    private final int version;
    private final long timestamp;
    private final long changeset;
    private final User user;
    private final Map<String, String> tags;

    public Info(long id, int version, long timestamp, long changeset, User user, Map<String, String> tags) {
        checkNotNull(user);
        checkNotNull(tags);
        this.id = id;
        this.version = version;
        this.timestamp = timestamp;
        this.changeset = changeset;
        this.user = user;
        this.tags = tags;
    }

    public long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getChangeset() {
        return changeset;
    }

    public User getUser() {
        return user;
    }

    public Map<String, String> getTags() {
        return tags;
    }

}
