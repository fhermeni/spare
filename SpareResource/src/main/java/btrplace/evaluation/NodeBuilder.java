package btrplace.evaluation;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 5/21/13
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeBuilder {
    private Node node;
    private static int id;

    public NodeBuilder() {
        node = new Node();
        node.setNid(new UUID(1, id));
    }

    public NodeBuilder cpu(int ncpu) {
        node.setCpu(ncpu);
        node.setEcu(ncpu);
        return this;
    }

    public NodeBuilder ecu(int necu) {
        node.setEcu(necu);
        return this;
    }

    public NodeBuilder mem(int nmem) {
        node.setMem(nmem);
        return this;
    }

    public NodeBuilder hd(int nhd) {
        node.setHd(nhd);
        return this;
    }

    public Node build() {
        id++;
        return node;
    }

    public Node small() {
        return new NodeBuilder().cpu(6).mem(12000).build();
    }

    public Node medium() {
        return new NodeBuilder().cpu(8).mem(16000).build();
    }

    public Node large() {
        return new NodeBuilder().cpu(10).mem(24000).build();
    }

    public Node extra() {
        return new NodeBuilder().cpu(20).mem(32000).build();
    }
}
