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
import com.hazelcast.util.ExceptionUtil;
import com.orbitz.consul.CatalogClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.catalog.CatalogService;
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

    private CatalogClient agentClient;

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

        this.agentClient = Consul.newClient(consulHost, consulPort).
                catalogClient();
    }

    @Override
    public Collection<DiscoveryNode> discoverNodes() {

        Collection<DiscoveryNode> list = new LinkedList<>();

        if (this.agentClient != null) {
            try {

                ConsulResponse<List<CatalogService>> service = this.agentClient.getService(serviceName);

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
                LOG.severe(e.getMessage());
                throw ExceptionUtil.rethrow(e);
            }
        }
        return list;
    }

    @Override
    public void destroy() {
    }

}
