package com.github.charlesvhe.eureka;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaServer
@EnableScheduling
public class EurekaApplication {

    @NacosInjected
    private NamingService namingService;
    @Autowired
    private PeerAwareInstanceRegistry peerAwareInstanceRegistry;

    @Scheduled(fixedDelay = 10000)
    public void nacosSync() throws Exception {
        ListView<String> serviceList = namingService.getServicesOfServer(1, 1000);
        for (String service : serviceList.getData()) {
            List<Instance> instanceList = namingService.getAllInstances(service);
            for (Instance instance : instanceList) {
                String appName = instance.getServiceName().substring(instance.getServiceName().lastIndexOf('@') + 1);
                String discoveryClient = instance.getMetadata().get("DiscoveryClient");
                if (StringUtils.isEmpty(discoveryClient)) {   // 从nacos原始注册，同步到eureka
                    peerAwareInstanceRegistry.register(InstanceInfo.Builder.newBuilder()
                            .setAppName(appName)
                            .setIPAddr(instance.getIp())
                            .setPort(instance.getPort())
                            .setInstanceId(String.format("%s:%s:%s", appName, instance.getIp(), instance.getPort()))
                            .setDataCenterInfo(() -> DataCenterInfo.Name.MyOwn)
                            .setMetadata(instance.getMetadata())
                            .build(), true);
                }
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }
}
