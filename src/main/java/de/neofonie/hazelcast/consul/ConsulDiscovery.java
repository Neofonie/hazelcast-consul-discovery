/*
The MIT License (MIT)

Copyright (c) 2015 Neofonie GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.neofonie.hazelcast.consul;

import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.CatalogService;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Discover the Consul-nodes.
 *
 * @author Jan De Cooman
 */
public class ConsulDiscovery implements DiscoveryStrategy {

    private static final Logger LOG = Logger.getLogger(ConsulDiscovery.class.getName());

    private CatalogClient catalogClient;

    final private String consulHost;

    final private int consulPort;

    final private String serviceName;

    ConsulDiscovery(Map<String, Comparable> properties) {
        this.consulHost = (String) properties.getOrDefault("host", "localhost");
        this.consulPort = (Integer) properties.getOrDefault("port", 8500);
        this.serviceName = (String) properties.get("name");
        if (this.serviceName == null) {
            throw new RuntimeException("Property 'name' is missing in the consul provider");
        }
    }

    /**
     * Open connection to consul.
     *
     */
    @Override
    public void start() {

        LOG.log(Level.INFO, "Starting Consul Discovery");

    }

    /**
     * Construct the client to connect to Consul.
     * 
     * @return CatalogClient
     * @throws MalformedURLException 
     */
    private synchronized CatalogClient getClient() throws MalformedURLException {
        if (this.catalogClient == null) {
            this.catalogClient = Consul.builder().
                    withUrl(new URL("http", consulHost, consulPort, "")).
                    build().
                    catalogClient();
        }
        return this.catalogClient;
    }

    /**
     * Look in consul for nodes with a given name. Exceptions are not rethrown
     * to prevent HazelCast of shutting down. This method returns an empty
     * list in case of error. When for example, consul returns to a normal state
     * a list of nodes is returned. HazelCast will try to connect to this
     * nodes.
     * 
     * @return A Collection of Nodes.
     */
    @Override
    public Collection<DiscoveryNode> discoverNodes() {

        Collection<DiscoveryNode> list = new LinkedList<>();
        try {

            ConsulResponse<List<CatalogService>> service
                    = this.getClient().getService(serviceName);

            for (CatalogService s : service.getResponse()) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "Found service at: {0}", s.getAddress());
                }
                Address address = new Address(s.getAddress(), s.getServicePort());
                list.add(new SimpleDiscoveryNode(address));
            }

            LOG.log(Level.INFO, "Hazelcast found ''{0}'' instance(s) of ''{1}'' ", new Object[]{list.size(), serviceName});

            return list;
            
        } catch (Exception e) {
            // The exception is no longer rethrown. When the exception
            // arrives in Hazelcast-core, it will stop working totally.
            // An empty list is delivered as default. This method will be
            // invoked again after a certain time. When consul is available,
            // it will answer, otherwise we just retry.
            LOG.severe(e.getMessage());
        }

        return list;
    }

    @Override
    public void destroy() {
    }

}
