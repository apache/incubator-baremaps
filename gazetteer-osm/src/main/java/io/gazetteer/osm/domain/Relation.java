package io.gazetteer.osm.domain;

import com.google.common.base.Objects;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Relation implements Entity {

    private final Info info;

    private final List<Member> members;

    public Relation(Info info, List<Member> members) {
        checkNotNull(info);
        checkNotNull(members);
        this.info = info;
        this.members = members;
    }

    @Override
    public Info getInfo() {
        return info;
    }

    public List<Member> getMembers() {
        return members;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return Objects.equal(info, relation.info) &&
                Objects.equal(members, relation.members);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(info, members);
    }
}
