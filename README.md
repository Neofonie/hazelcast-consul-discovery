# Hazelcast - Consul Node Discovery

## **⚠️ THIS PROJECT IS NOW UNMAINTED AND IN READ-ONLY MODE ⚠️**

The plugin for Hazelcast fetches nodes belonging to a given service from the Consul server. 

**Currently works from Hazelcast 3.6-EA !**

In order to get this plugin running, you must place the JAR on the classpath along with this above build of Hazelcast. Then you must alter the cluster.xml like this:

 ```
 <join>
    <multicast enabled="false">
        .....................
    </multicast>

    <discovery-strategies>
	<discovery-strategy class="de.neofonie.hazelcast.consul.ConsulDiscovery" enabled="true">
	  <properties>
	      <property name ="host">localhost</property>
	      <property name ="port">8500</property>
	      <property name ="name">your-hazelcast-service</property>
	  </properties>
	</discovery-strategy>
    </discovery-strategies>
</join>
```
        
It is important to know that you must register you Hazelcast service in Consul along with the appropriate port (e.g. 5901). The ConsulDiscovery will read all IPs from this service together with their ports!

# Credits and Authors

Forked from the original repo: https://github.com/decoomanj

Authors:
- Jan De Cooman
- Guiquan Weng

Thanks to Christoph Engelbert from Hazelcast for the support.
