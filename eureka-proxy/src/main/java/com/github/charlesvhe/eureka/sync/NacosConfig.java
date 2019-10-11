package com.github.charlesvhe.eureka.sync;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liju2
 * @date 2019/10/10
 */
@Configuration
public class NacosConfig {

    @Bean
    public NamingService nacosConfigService(@Value("${nacos.serverAddr}") String serverAddr) throws NacosException {
        return NacosFactory.createNamingService(serverAddr);
    }

    @Bean
    public NamingMaintainService nacosConfigService1(@Value("${nacos.serverAddr}") String serverAddr) throws NacosException {
        return NacosFactory.createMaintainService(serverAddr);
    }

}
