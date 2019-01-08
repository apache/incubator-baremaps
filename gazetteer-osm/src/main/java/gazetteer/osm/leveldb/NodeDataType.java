package gazetteer.osm.leveldb;

import com.google.protobuf.InvalidProtocolBufferException;
import gazetteer.osm.model.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NodeDataType implements DataType<Node> {

    @Override
    public byte[] serialize(Node entity) throws IOException {
        List<String> keys = new ArrayList<>();
        List<String> vals = new ArrayList<>();
        return Leveldb.Node.newBuilder()
                .setId(entity.getId())
                .setVersion(entity.getVersion())
                .setUserid(entity.getUserId())
                .setTimestamp(entity.getTimestamp())
                .setChangesetId(entity.getChangesetId())
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
                node.getUserid(),
                node.getTimestamp(),
                node.getChangesetId(),
                node.getLon(), node.getLat(),
                node.getKeysList(),
                node.getValsList());
    }

}
