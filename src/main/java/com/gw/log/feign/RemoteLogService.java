package com.gw.log.feign;

import com.gw.log.entity.SysLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author wjn
 * @date 2020-05-28
 */
@FeignClient(value = "log" , url = "http://127.0.0.1:8888" , fallback = RemoteLogServiceFallback.class)
public interface RemoteLogService {
    /**
     * 保存日志
     * @param sysLog log
     * @return boolean
     */
    @PostMapping("/syslog/createLog")
    String saveLog(@RequestBody SysLog sysLog, @RequestHeader("Authorization") String Authorization);

}
