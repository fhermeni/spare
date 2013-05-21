package btrplace.evaluation;

import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/21/13
 * Time: 4:48 PM
 */
public class Node {
    private int cpu;
    private int ecu;
    private int mem;
    private int hd;
    private UUID nid;


    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public void setEcu(int ecu) {
        this.ecu = ecu;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public void setHd(int hd) {
        this.hd = hd;
    }

    public void setNid(UUID nid) {
        this.nid = nid;
    }

    public int getCpu() {
        return cpu;
    }

    public int getEcu() {
        return ecu;
    }

    public int getMem() {
        return mem;
    }

    public int getHd() {
        return hd;
    }

    public UUID getNid() {
        return nid;
    }

    @Override
    public String toString() {
        return "NodeID=" + nid + "{" + "CPU=" + cpu + " ECU=" + ecu + " Mem=" + mem + " HD=" + hd + "}";
    }
}
