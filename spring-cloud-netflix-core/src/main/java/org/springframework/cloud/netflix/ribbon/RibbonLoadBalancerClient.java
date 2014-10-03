package org.springframework.cloud.netflix.ribbon;

import com.netflix.client.ClientFactory;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

/**
 * @author Spencer Gibb
 */
public class RibbonLoadBalancerClient implements LoadBalancerClient {
    @Autowired
    private ServerListInitializer serverListInitializer;

    @Override
    public ServiceInstance choose(String serviceId) {
        serverListInitializer.initialize(serviceId);
        ILoadBalancer loadBalancer = ClientFactory.getNamedLoadBalancer(serviceId);
        Server server = loadBalancer.chooseServer(null);
        if (server == null) {
            throw new IllegalStateException("Unable to locate ILoadBalancer for service: "+ serviceId);
        }
        return new RibbonServer(serviceId, server);
    }

    private class RibbonServer implements ServiceInstance {
        private String serviceId;
        private Server server;

        private RibbonServer(String serviceId, Server server) {
            this.serviceId = serviceId;
            this.server = server;
        }

        @Override
        public String getServiceId() {
            return serviceId;
        }

        @Override
        public String getHost() {
            return server.getHost();
        }

        @Override
        public int getPort() {
            return server.getPort();
        }
    }
}
