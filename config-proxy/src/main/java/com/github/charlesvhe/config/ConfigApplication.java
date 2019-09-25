package com.github.charlesvhe.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StringUtils;

@SpringBootApplication
@EnableConfigServer
public class ConfigApplication {
    @Autowired
    private ConfigService configService;

    @Bean
    public ConfigService nacosConfigService(@Value("${nacos.serverAddr}") String serverAddr) throws NacosException {
        return NacosFactory.createConfigService(serverAddr);
    }

    @Bean
    public EnvironmentRepository nacosEnvironmentRepository() {
        return new EnvironmentRepository() {
            @Override
            public Environment findOne(String application, String profile, String label) {
                try {
                    Environment environment = new Environment(application, new String[]{profile}, label, null, null);
                    addPropertySource("application", environment);
                    addPropertySource(application, environment);
                    return environment;
                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }
            }

            private void addPropertySource(String application, Environment environment) throws NacosException {
                String dataId = application + ".yml";
                String config = configService.getConfig(dataId, Constants.DEFAULT_GROUP, 3000);
                if (StringUtils.hasText(config)) {
                    YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
                    yamlPropertiesFactoryBean.setResources(new ByteArrayResource(config.getBytes()));
                    environment.add(new PropertySource(dataId, yamlPropertiesFactoryBean.getObject()));
                }
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(ConfigApplication.class, args);
    }
}
