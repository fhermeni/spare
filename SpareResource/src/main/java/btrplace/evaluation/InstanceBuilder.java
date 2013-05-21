package btrplace.evaluation;

import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/21/13
 * Time: 5:20 PM
 */
public class InstanceBuilder {

    private Instance instance;
    private static int id;


    public InstanceBuilder() {
        instance = new Instance();
        instance.setVmid(new UUID(0, id));

    }

    private int cpu;
    private int ecu;
    private int mem;
    private int hd;

    public InstanceBuilder cpu(int ncpu) {
        instance.setCpu(ncpu);
        return this;
    }

    public InstanceBuilder ecu(int necu) {
        instance.setEcu(necu);
        return this;
    }

    public InstanceBuilder mem(int nmem) {
        instance.setMem(nmem);
        return this;
    }

    public InstanceBuilder hd(int nhd) {
        instance.setHd(nhd);
        return this;
    }

    public Instance build() {
        id++;
        return instance;
    }

    public Instance micro() {
        return new InstanceBuilder().mem(630).ecu(1).build();
    }

    public Instance small() {
        return new InstanceBuilder().mem(1700).ecu(1).build();
    }

    public Instance medium() {
        return new InstanceBuilder().mem(3750).ecu(2).build();
    }

    public Instance large() {
        return new InstanceBuilder().mem(7500).ecu(4).build();
    }

    public Instance extraLarge() {
        return new InstanceBuilder().mem(15000).ecu(8).build();
    }
}
