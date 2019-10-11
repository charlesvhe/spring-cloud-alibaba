package com.github.charlesvhe.eureka.sync;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author liju2
 * @date 2019/10/10
 */
@Component
@EnableScheduling
public class NacosHeartbeat {

    @Autowired
    private NamingService namingService;
    @Autowired
    private NacosEventListener listener;

    @Scheduled(fixedDelay = 10000)
    public void syncNacosAndEureka() throws Exception {
        ListView<String> serviceList = namingService.getServicesOfServer(1, 1000);
        for (String service : serviceList.getData()) {
            List<ServiceInfo> list = namingService.getSubscribeServices();
            Optional<ServiceInfo> optional = list.stream().filter(serviceInfo -> serviceInfo.getName().equals(service)).findFirst();
            if (!optional.isPresent()) {
                namingService.subscribe(service, listener);
            }
        }

    }
}
