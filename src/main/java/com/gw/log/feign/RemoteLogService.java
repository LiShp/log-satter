package com.gw.log.feign;

import com.gw.log.entity.SysLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author wjn
 * @date 2020-05-28
 */
@FeignClient(value = "log" , url = "http://logcenter.test.paas.gwm.cn/", fallback = RemoteLogServiceFallback.class)
public interface RemoteLogService {
    /**
     * 保存日志
     * @param sysLog log
     * @return boolean
     */
    @PostMapping("/syslog/createLog")
    void saveLog(@RequestBody SysLog sysLog, @RequestHeader("Authorization") String Authorization);

}
