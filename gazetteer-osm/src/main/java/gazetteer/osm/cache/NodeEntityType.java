package gazetteer.osm.cache;

import com.google.protobuf.InvalidProtocolBufferException;
import gazetteer.osm.model.Data;
import gazetteer.osm.model.Node;
import gazetteer.osm.model.User;
import gazetteer.osm.rocksdb.Leveldb;

public class NodeEntityType implements EntityType<Node> {

    @Override
    public byte[] serialize(Node entity) {
        return Leveldb.Node.newBuilder()
                .setId(entity.getData().getId())
                .setVersion(entity.getData().getVersion())
                .setUid(entity.getData().getUser().getId())
                .setUser(entity.getData().getUser().getName())
                .setTimestamp(entity.getData().getTimestamp())
                .setChangeset(entity.getData().getChangeset())
                .setLon(entity.getLon())
                .setLat(entity.getLat())
                .putAllTags(entity.getData().getTags())
                .build().toByteArray();
    }

    @Override
    public Node deserialize(byte[] bytes) throws InvalidProtocolBufferException {
        Leveldb.Node node = Leveldb.Node.parseFrom(bytes);
        User user = new User(node.getUid(), node.getUser());
        Data data = new Data(node.getId(), node.getVersion(), node.getTimestamp(), node.getChangeset(), user, node.getTagsMap());
        return new Node(data, node.getLon(), node.getLat());
    }

}
