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

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.config.properties.PropertyTypeConverter;
import com.hazelcast.config.properties.SimplePropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Consul discovery factory.
 *
 * @author Jan De Cooman
 */
public class ConsulDiscoveryFactory implements DiscoveryStrategyFactory {

    private final Collection<PropertyDefinition> propertyDefinitions;

    public ConsulDiscoveryFactory() {
        List<PropertyDefinition> properties = new ArrayList<>();
        properties.add(new SimplePropertyDefinition("host", PropertyTypeConverter.STRING));
        properties.add(new SimplePropertyDefinition("port", PropertyTypeConverter.INTEGER));
        properties.add(new SimplePropertyDefinition("name", PropertyTypeConverter.STRING));
        this.propertyDefinitions = Collections.unmodifiableCollection(properties);
    }

    @Override
    public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
        return ConsulDiscovery.class;
    }

    @Override
    public DiscoveryStrategy newDiscoveryStrategy(
            DiscoveryNode discoveryNode, ILogger logger, Map<String, Comparable> properties) {
        return new ConsulDiscovery(properties);
    }

    @Override
    public Collection<PropertyDefinition> getConfigurationProperties() {
        return propertyDefinitions;
    }

}
