package btrplace.evaluation;

import btrplace.test.PremadeElements;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/21/13
 * Time: 2:26 PM
 */
public class Datacenter implements PremadeElements {

    private Set<UUID> servers;
    private Set<Set<UUID>> racks;
    private Set<UUID> rack1;
    private Set<UUID> rack2;
    private Set<UUID> rack3;

    public UUID n6;
    public UUID n7;
    public UUID n8;
    public UUID n9;
    public UUID n10;
    public UUID n11;
    public UUID n12;

    public Datacenter() {

        n6 = new UUID(1, 6);
        n7 = new UUID(1, 7);
        n8 = new UUID(1, 8);
        n9 = new UUID(1, 9);
        n10 = new UUID(1, 10);
        n11 = new UUID(1, 11);
        n12 = new UUID(1, 12);
        racks = new HashSet<Set<UUID>>(3);
        servers = new HashSet<UUID>();

        rack1 = new HashSet<UUID>(Arrays.asList(n1, n2, n3, n4));
        rack2 = new HashSet<UUID>(Arrays.asList(n5, n6, n7, n8));
        rack3 = new HashSet<UUID>(Arrays.asList(n9, n10, n11, n12));

        racks.add(rack1);
        racks.add(rack2);
        racks.add(rack3);
        servers.addAll(Arrays.asList(n1, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12));
    }

    public Set<UUID> getServers() {
        return servers;
    }

    public void setServers(Set<UUID> servers) {
        this.servers = servers;
    }

    public Set<Set<UUID>> getRacks() {
        return racks;
    }

    public void setRacks(Set<Set<UUID>> racks) {
        this.racks = racks;
    }

    public Set<UUID> getRack1() {
        return rack1;
    }

    public void setRack1(Set<UUID> rack1) {
        this.rack1 = rack1;
    }

    public Set<UUID> getRack2() {
        return rack2;
    }

    public void setRack2(Set<UUID> rack2) {
        this.rack2 = rack2;
    }

    public Set<UUID> getRack3() {
        return rack3;
    }

    public void setRack3(Set<UUID> rack3) {
        this.rack3 = rack3;
    }

}
