package gazetteer.osm.cache;

import com.google.protobuf.InvalidProtocolBufferException;
import gazetteer.osm.model.Node;
import gazetteer.osm.rocksdb.Leveldb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeEntityType implements EntityType<Node> {

    @Override
    public byte[] serialize(Node entity) throws IOException {
        List<String> keys = new ArrayList<>();
        List<String> vals = new ArrayList<>();
        return Leveldb.Node.newBuilder()
                .setId(entity.getId())
                .setVersion(entity.getVersion())
                .setUid(entity.getUid())
                .setUser(entity.getUser())
                .setTimestamp(entity.getTimestamp())
                .setChangeset(entity.getChangeset())
                .setLon(entity.getLon())
                .setLat(entity.getLat())
                .addAllKeys(entity.getKeys())
                .addAllVals(entity.getVals())
                .build().toByteArray();
    }

    @Override
    public Node deserialize(byte[] data) throws InvalidProtocolBufferException {
        Leveldb.Node node = Leveldb.Node.parseFrom(data);
        return new Node(node.getId(),
                node.getVersion(),
                node.getUid(),
                node.getUser(),
                node.getTimestamp(),
                node.getChangeset(),
                node.getLon(), node.getLat(),
                node.getKeysList(),
                node.getValsList());
    }

}
