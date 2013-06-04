Introduction
================================

Data centers are widely used today to provide specific requirements for computing and hosting services, such as performance, reliability and availability. As the data centers involve in size and performance, the need for saving their energy and power consumption is important not only to reduce the operation cost but also to increase the efficiency of the infrastructures. The average utilization of a 5000-node data center is only 10-50% of its	maximum capacity. This leads to a waste of resources, energy and money. 

I have modeled and developed a set of placement constraints to control the amount of spare resources of a data center. The minSpareResource and maxSpareResource constraints limit the minimum and maximum of spare resources. The maxOnline constraint restricts the number online nodes below a given threshold. These constraints have been integrated into BtrPlace, a flexible consolidation manager, to configure data centers for energy efficiency.


Toward energy efficiency placement constraints
==============================================
The practical interest of the constraints and their models in terms of RP variables. The placement constraints to manipulate the spare resources are used by data center administrators to manage the number of idle resources and the number of nodes to be online. The constraints may provide two kinds of restriction:
* **Discrete** imposes a restriction on the destination configuration only. This restriction ensures the viability of the destination configuration when the constraints are not satisfied in the current configuration.
* **Continuous** imposes a restriction over the reconfiguration process, such as the action schedule. This  restriction is important, especially for limiting the number of online nodes, for example to ensure the license agreement at any moment.

Spare Resource Management Constraints
=====================================
1. **MinSpareResources**(N:Collection<node>, rc: String, C: number)

 The minSpareResources reserves at least C number of free resources directly available for VMs on the set of nodes N. The offline nodes are not considered.

2. **MaxSpareResources**(N:Collection<node>, rc: String, C: number)

 The maxSpareResources restricts at most C number of free resources directly available for VMs on the set of nodes N. The offline nodes are not considered.

3. **MinSpareNode**(N:Collection<node>, C: number)
The minSpareNode reserves at least C number of nodes to be idle.

4. **MaxSpareNode**(N:Collection<node>, C: number)
The maxSpareNode restricts at most C number of nodes to be idle.

5. **MaxOnline**(N:Collection<node>, C: number)
The MaxOnline constraint restrict the number of online nodes in set N to at most C nodes.
