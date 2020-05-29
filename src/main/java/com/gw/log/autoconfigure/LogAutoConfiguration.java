package com.gw.log.autoconfigure;

import com.gw.log.feign.RemoteLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author wjn
 * @date 2020-05-27
 */
@EnableAsync
@Configuration
@ComponentScan(basePackages = {"com.gw.log.autoconfigure","com.gw.log.feign"})
@ConditionalOnClass(value = {LogAspect.class,RemoteLogService.class})
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "enable",prefix = "com.log",havingValue = "true",matchIfMissing = true)
@EnableFeignClients(clients = com.gw.log.feign.RemoteLogService.class)
public class LogAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(LogAspect.class)
    @ConditionalOnProperty(value = "enabled", matchIfMissing = true)
    public LogAspect logAspect() {
        return new LogAspect();
    }
}
