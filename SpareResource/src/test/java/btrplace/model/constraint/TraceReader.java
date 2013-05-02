package btrplace.model.constraint;

import btrplace.model.DefaultMapping;
import btrplace.model.Mapping;
import btrplace.model.view.ShareableResource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: TU HUYNH DANG
 * Date: 5/2/13
 * Time: 9:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class TraceReader {

    private String model_file;
    private String assigment_file;
    private int number_resources, number_of_nodes, number_of_services, number_of_vm, number_of_balance_cost;
    private Map<Integer, ArrayList<UUID>> neighbors;
    private Map<Integer, ArrayList<UUID>> locations;
    private ArrayList<Integer> spreadMin;
    private ArrayList<Integer> pmcs;
    private Map<Integer, ArrayList<Integer>> map_service_depend;
    private Map<Integer, ArrayList<Integer>> balance_triple;
    private ArrayList<Integer> costs;
    private Map<Integer, ArrayList<Integer>> resource_properties;

    // For BtrPlace
    private ArrayList<UUID> nodes;
    private ArrayList<UUID> vms;
    private ArrayList<ShareableResource> shareableResources;
    private Map<Integer, ArrayList<UUID>> services_vms;
    private Map<Integer, Integer> assigment;
    private Mapping mapping;

    public TraceReader(String model, String assignmentPath) throws IOException {
        model_file = model;
        assigment_file = assignmentPath;
        number_resources = 0;
        number_of_nodes = 0;
        number_of_services = 0;
        number_of_vm = 0;
        number_of_balance_cost = 0;
        shareableResources = new ArrayList<ShareableResource>();
        resource_properties = new HashMap<Integer, ArrayList<Integer>>();
        nodes = new ArrayList<UUID>();
        vms = new ArrayList<UUID>();
        neighbors = new HashMap<Integer, ArrayList<UUID>>();
        locations = new HashMap<Integer, ArrayList<UUID>>();
        spreadMin = new ArrayList<Integer>();
        pmcs = new ArrayList<Integer>();
        map_service_depend = new HashMap<Integer, ArrayList<Integer>>();
        services_vms = new HashMap<Integer, ArrayList<UUID>>();
        balance_triple = new HashMap<Integer, ArrayList<Integer>>();
        costs = new ArrayList<Integer>(4);
        assigment = new HashMap<Integer, Integer>();
        mapping = new DefaultMapping();
    }

    public String getAssigment_file() {
        return assigment_file;
    }

    public Map<Integer, ArrayList<Integer>> getResource_properties() {
        return resource_properties;
    }

    public ArrayList<UUID> getVms() {
        return vms;
    }

    public Map<Integer, Integer> getAssigment() {
        return assigment;
    }

    public Mapping getMapping() {
        return mapping;
    }

    public void readModel() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(model_file));
        String line;

        //--------------- resources ----------
        if ((line = br.readLine()) != null) {
            number_resources = Integer.parseInt(line);
            for (int i = 0; i < number_resources; i++) {
                StringTokenizer st = new StringTokenizer(br.readLine());
                ArrayList<Integer> rp = new ArrayList<Integer>();
                while (st.hasMoreTokens()) {
                    rp.add(Integer.parseInt(st.nextToken()));
                }
                resource_properties.put(i, rp);
                shareableResources.add(new ShareableResource("r" + i, 1));
            }
        }

        //--------------- machines ----------
        if ((line = br.readLine()) != null) {
            number_of_nodes = Integer.parseInt(line);
            for (int i = 0; i < number_of_nodes; i++) {
                UUID nodej = new UUID(1, i);
                mapping.addOnlineNode(nodej);
                nodes.add(i, nodej);
                StringTokenizer st = new StringTokenizer(br.readLine());

                if (st.hasMoreTokens()) {
                    int nid = Integer.parseInt(st.nextToken());
                    if (neighbors.containsKey(nid)) {
                        neighbors.get(nid).add(nodej);
                    } else {
                        ArrayList<UUID> uuids = new ArrayList<UUID>();
                        uuids.add(nodej);
                        neighbors.put(nid, uuids);
                    }
                }

                if (st.hasMoreTokens()) {
                    int lid = Integer.parseInt(st.nextToken());
                    if (locations.containsKey(lid)) {
                        locations.get(lid).add(nodej);
                    } else {
                        ArrayList<UUID> uuids = new ArrayList<UUID>();
                        uuids.add(nodej);
                        locations.put(lid, uuids);
                    }
                }

                for (int j = 0; j < number_resources; j++) {
                    if (st.hasMoreTokens()) {
                        int resource_capacity = Integer.parseInt(st.nextToken());
                        shareableResources.get(j).set(nodej, resource_capacity);
                    }
                }
            }
        }
        //--------------- services ----------
        if ((line = br.readLine()) != null) {
            number_of_services = Integer.parseInt(line);
        }
        for (int s = 0; s < number_of_services; s++) {
            StringTokenizer service = new StringTokenizer(br.readLine());
            spreadMin.add(Integer.parseInt(service.nextToken()));

            int dependOn = Integer.parseInt(service.nextToken());
            if (dependOn > 0) {
                ArrayList depend_list = new ArrayList<Integer>();
                while (service.hasMoreTokens()) {
                    depend_list.add(Integer.parseInt(service.nextToken()));
                }
                map_service_depend.put(s, depend_list);
            }

        }

        //--------------- processes -----------
        if ((line = br.readLine()) != null) {
            number_of_vm = Integer.parseInt(line);
        }

        for (int k = 0; k < number_of_vm; k++) {
            UUID vm = new UUID(0, k);
            vms.add(vm);
            if ((line = br.readLine()) != null) {
                StringTokenizer vm_spec = new StringTokenizer(line);

                int service_id = Integer.parseInt(vm_spec.nextToken());
                if (services_vms.containsKey(service_id)) {
                    ArrayList<UUID> contain_vms = services_vms.get(service_id);
                    contain_vms.add(vm);
                } else {
                    ArrayList<UUID> contain_vms = new ArrayList<UUID>();
                    contain_vms.add(vm);
                    services_vms.put(service_id, contain_vms);
                }

                for (int j = 0; j < number_resources; j++) {
                    if (vm_spec.hasMoreTokens()) {
                        int resource_demand = Integer.parseInt(vm_spec.nextToken());
                        shareableResources.get(j).set(vm, resource_demand);
                    }
                }

                pmcs.add(Integer.parseInt(vm_spec.nextToken()));
            }
        }

        //--------------- balance triples -----------
        if ((line = br.readLine()) != null) {
            number_of_balance_cost = Integer.parseInt(line);
            if (number_of_balance_cost > 0) {
                for (int y = 0; y < number_of_balance_cost; y++) {
                    StringTokenizer bst = new StringTokenizer(br.readLine());
                    ArrayList<Integer> triple = new ArrayList<Integer>();
                    while (bst.hasMoreTokens()) {
                        triple.add(Integer.parseInt(bst.nextToken()));
                    }
                    balance_triple.put(y, triple);
                }
            } else {
                costs.add(0);
            }
        }

        //--------------- costs -----------
        while ((line = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line);
            while (st.hasMoreTokens())
                costs.add(Integer.parseInt(st.nextToken()));
        }

        br.close();
    }

    public void readAssigment() throws IOException {
//        Map<Integer, ArrayList<Integer>> node_assigment = new HashMap<Integer, ArrayList<Integer>>();
        BufferedReader br = new BufferedReader(new FileReader(assigment_file));
        String line;
        while ((line = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line);
            int i = 0;
            while (st.hasMoreTokens()) {
                int host = Integer.parseInt(st.nextToken());
                assigment.put(i, host);
                mapping.addRunningVM(vms.get(i), nodes.get(host));

                /*if(!node_assigment.containsKey(host)) {
                    ArrayList<Integer> listvm = new ArrayList<Integer>();
                    listvm.add(i);
                    node_assigment.put(host, listvm);
                }
                else {
                    node_assigment.get(host).add(i);
                }*/
                i++;
            }
        }
       /* for (Integer key : node_assigment.keySet()) {
            System.out.println("node " + key + " contain VMs:" + node_assigment.get(key));
        }*/
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("nodes:\n");
        for (UUID n : nodes) {
            sb.append(n + "\n");
        }

        sb.append("neighborhoods:\n");
        for (Integer key : neighbors.keySet()) {
            sb.append(key + ":" + neighbors.get(key) + "\n");
        }
        sb.append("locations:\n");
        for (Integer key : locations.keySet()) {
            sb.append(key + ":" + locations.get(key) + "\n");
        }

        sb.append("Sharable Resources:\n");
        for (ShareableResource sr : shareableResources) {
            sb.append(sr + "\n");
        }

        sb.append("SpreadMin: " + spreadMin + "\n");
        sb.append("PMC: " + pmcs + "\n");

        sb.append("Dependencies:\n");
        for (Integer key : map_service_depend.keySet()) {
            sb.append(key + " depends on: " + map_service_depend.get(key) + "\n");
        }

        sb.append("Services:\n");
        for (Integer key : services_vms.keySet()) {
            sb.append(key + " contains: " + services_vms.get(key) + "\n");
        }

        for (Integer key : balance_triple.keySet()) {
            sb.append("balance tripe: " + balance_triple.get(key) + "\n");
        }

        sb.append("costs:" + costs + "\n");

        sb.append("Assignment:\n");

        for (Integer key : assigment.keySet()) {
            sb.append(key + "=" + assigment.get(key) + "\n");
        }

        return sb.toString();
    }


    public String getModel_file() {
        return model_file;
    }

    public int getNumber_resources() {
        return number_resources;
    }

    public int getNumber_of_nodes() {
        return number_of_nodes;
    }

    public int getNumber_of_services() {
        return number_of_services;
    }

    public int getNumber_of_vm() {
        return number_of_vm;
    }

    public int getNumber_of_balance_cost() {
        return number_of_balance_cost;
    }

    public ArrayList<ShareableResource> getShareableResources() {
        return shareableResources;
    }

    public ArrayList<UUID> getNodes() {
        return nodes;
    }

    public Map<Integer, ArrayList<UUID>> getNeighbors() {
        return neighbors;
    }

    public Map<Integer, ArrayList<UUID>> getLocations() {
        return locations;
    }

    public ArrayList<Integer> getSpreadMin() {
        return spreadMin;
    }

    public ArrayList<Integer> getPmcs() {
        return pmcs;
    }

    public Map<Integer, ArrayList<Integer>> getMap_service_depend() {
        return map_service_depend;
    }

    public Map<Integer, ArrayList<UUID>> getServices_vms() {
        return services_vms;
    }

    public Map<Integer, ArrayList<Integer>> getBalance_triple() {
        return balance_triple;
    }

    public ArrayList<Integer> getCosts() {
        return costs;
    }
}
