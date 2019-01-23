package io.gazetteer.osm.domain;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Member {

    public enum Type {node, way, relation}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return id == member.id &&
                type == member.type &&
                Objects.equal(role, member.role);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, role);
    }

}
