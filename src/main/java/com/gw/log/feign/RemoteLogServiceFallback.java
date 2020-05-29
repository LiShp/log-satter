package com.gw.log.feign;

import com.gw.log.entity.SysLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author lsp
 * @date 2020-03-27 17:40
 */
@Component
public class RemoteLogServiceFallback implements RemoteLogService {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteLogServiceFallback.class);
    private Throwable cause;

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public String saveLog(SysLog sysLog, String Authorization) {
        LOG.error("feign 插入日志失败", cause);
        return null;
    }

}
