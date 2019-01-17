package io.gazetteer.osm.domain;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class Relation implements Entity {

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

}
