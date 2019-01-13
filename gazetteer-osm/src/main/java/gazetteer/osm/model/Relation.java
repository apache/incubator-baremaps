package gazetteer.osm.model;

import java.util.List;

public class Relation implements Entity {

    private final Data data;

    private final List<Member> members;

    public Relation(Data data, List<Member> members) {
        this.data = data;
        this.members = members;
    }

    @Override
    public Data getData() {
        return null;
    }

    public List<Member> getMembers() {
        return members;
    }

}
