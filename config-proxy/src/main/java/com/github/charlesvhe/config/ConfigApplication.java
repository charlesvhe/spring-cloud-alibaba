package com.github.charlesvhe.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigServer
public class ConfigApplication {

    @Bean
    public EnvironmentRepository nacosEnvironmentRepository(){
        return new EnvironmentRepository(){
            @Override
            public Environment findOne(String application, String profile, String label) {
                Environment environment = new Environment(application,new String[]{profile}, label, null, null);
                return environment;
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(ConfigApplication.class, args);
    }
}
