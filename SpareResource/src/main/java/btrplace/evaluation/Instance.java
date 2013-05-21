package btrplace.evaluation;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 5/21/13
 * Time: 4:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Instance {

    private int cpu;
    private int ecu;
    private int mem;
    private int hd;
    private UUID vmid;

    public void setVmid(UUID vmid) {
        this.vmid = vmid;
    }

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

    public UUID getVmid() {
        return vmid;
    }

    @Override
    public String toString() {
        return "VmID=" + vmid + "{" + "CPU=" + cpu + " ECU=" + ecu + " Mem=" + mem + " HD=" + hd + "}";
    }
}
