package io.gazetteer.osm.domain;

import static com.google.common.base.Preconditions.checkNotNull;

public class Member {

    private final long id;

    private final MemberType type;

    private final String role;

    public Member(long id, MemberType type, String role) {
        checkNotNull(type);
        checkNotNull(role);
        this.id = id;
        this.type = type;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public MemberType getType() {
        return type;
    }

    public String getRole() {
        return role;
    }

}
