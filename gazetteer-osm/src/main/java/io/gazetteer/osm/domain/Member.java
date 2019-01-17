package io.gazetteer.osm.domain;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Member {

    public enum Type {Node, Way, Relation}

    private final long id;

    private final Type type;

    private final String role;

    public Member(long id, Type type, String role) {
        checkNotNull(type);
        checkNotNull(role);
        this.id = id;
        this.type = type;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getRole() {
        return role;
    }

}
