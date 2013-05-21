package btrplace.evaluation;

import btrplace.test.PremadeElements;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User: TU HUYNH DANG
 * Date: 5/21/13
 * Time: 1:48 PM
 */
public class WebApplication implements PremadeElements {

    private Set<UUID> apache;
    private Set<UUID> tomcat;
    private Set<UUID> mysql;

    public WebApplication() {
        apache = new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3));
        tomcat = new HashSet<UUID>(Arrays.asList(vm4, vm5, vm6, vm7));
        mysql = new HashSet<UUID>(Arrays.asList(vm8, vm9, vm10));
    }

    public Set<Set<UUID>> getAllTiers() {
        Set<Set<UUID>> all_tiers = new HashSet<Set<UUID>>(3);
        all_tiers.add(apache);
        all_tiers.add(tomcat);
        all_tiers.add(mysql);
        return all_tiers;
    }

    public Set<UUID> getAllReplicas() {
        return new HashSet<UUID>(Arrays.asList(vm1, vm2, vm3, vm4, vm5,
                vm6, vm7, vm8, vm9, vm10));
    }

    public Set<UUID> getApache() {
        return apache;
    }

    public void setApache(Set<UUID> apache) {
        this.apache = apache;
    }

    public Set<UUID> getTomcat() {
        return tomcat;
    }

    public void setTomcat(Set<UUID> tomcat) {
        this.tomcat = tomcat;
    }

    public Set<UUID> getMysql() {
        return mysql;
    }

    public void setMysql(Set<UUID> mysql) {
        this.mysql = mysql;
    }
}
