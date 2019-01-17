package io.gazetteer.osm.domain;

import static com.google.common.base.Preconditions.checkNotNull;

public class User {

    private final int id;
    private final String name;

    public User(int id, String name) {
        checkNotNull(name);
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
