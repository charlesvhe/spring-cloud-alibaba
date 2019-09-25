package com.github.charlesvhe.eureka;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableEurekaServer
@EnableScheduling
public class EurekaApplication {
    public static final String METADATA_DISCOVERY_CLIENT = "DiscoveryClient";

    @Autowired
    private NamingService namingService;
    @Autowired
    private PeerAwareInstanceRegistry peerAwareInstanceRegistry;

    @Bean
    public NamingService nacosConfigService(@Value("${nacos.serverAddr}") String serverAddr) throws NacosException {
        return NacosFactory.createNamingService(serverAddr);
    }

    @Scheduled(fixedDelay = 10000)
    public void syncNacosAndEureka() throws Exception {
        ListView<String> serviceList = namingService.getServicesOfServer(1, 1000);
        for (String service : serviceList.getData()) {
            for (Instance instance : namingService.getAllInstances(service)) {
                String appName = instance.getServiceName().substring(instance.getServiceName().lastIndexOf('@') + 1);
                String discoveryClient = instance.getMetadata().get(METADATA_DISCOVERY_CLIENT);
                if (StringUtils.isEmpty(discoveryClient)) {   // 从nacos原始注册，同步到eureka
                    Map<String, String> metadata = new HashMap<>(instance.getMetadata());
                    metadata.put(METADATA_DISCOVERY_CLIENT, "nacos");

                    peerAwareInstanceRegistry.register(InstanceInfo.Builder.newBuilder()
                            .setAppName(appName.toLowerCase())
                            .setIPAddr(instance.getIp())
                            .setPort(instance.getPort())
                            .setInstanceId(String.format("%s:%s:%s", appName, instance.getIp(), instance.getPort()))
                            .setDataCenterInfo(() -> DataCenterInfo.Name.MyOwn)
                            .setMetadata(metadata)
                            .build(), true);
                }
            }
        }

        List<Application> applicationList = peerAwareInstanceRegistry.getSortedApplications();
        for (Application application : applicationList) {
            for (InstanceInfo instance : application.getInstances()) {
                String discoveryClient = instance.getMetadata().get(METADATA_DISCOVERY_CLIENT);
                if (StringUtils.isEmpty(discoveryClient)) {   // 从eureka原始注册，同步到nacos
                    Map<String, String> metadata = new HashMap<>(instance.getMetadata());
                    metadata.put(METADATA_DISCOVERY_CLIENT, "eureka");

                    Instance nacosInstance = new Instance();
                    nacosInstance.setIp(instance.getIPAddr());
                    nacosInstance.setPort(instance.getPort());
                    nacosInstance.setClusterName(Constants.DEFAULT_CLUSTER_NAME);
                    nacosInstance.setMetadata(metadata);

                    namingService.registerInstance(instance.getAppName().toLowerCase(), Constants.DEFAULT_GROUP, nacosInstance);
                }
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }
}
