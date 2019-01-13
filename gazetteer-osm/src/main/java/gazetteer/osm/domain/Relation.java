package gazetteer.osm.domain;

import java.util.List;

public class Relation implements Entity {

    private final Info info;

    private final List<Member> members;

    public Relation(Info info, List<Member> members) {
        this.info = info;
        this.members = members;
    }

    @Override
    public Info getInfo() {
        return null;
    }

    public List<Member> getMembers() {
        return members;
    }

}
