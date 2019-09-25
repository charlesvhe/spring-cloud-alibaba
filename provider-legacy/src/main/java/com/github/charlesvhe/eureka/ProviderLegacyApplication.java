package com.github.charlesvhe.eureka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
public class ProviderLegacyApplication {

    @RestController
    @RefreshScope
    class EchoController {
        @Value("${key:default}")
        private String key;

        @RequestMapping(value = "/test", method = RequestMethod.GET)
        public String test() {
            return key;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ProviderLegacyApplication.class, args);
    }
}
