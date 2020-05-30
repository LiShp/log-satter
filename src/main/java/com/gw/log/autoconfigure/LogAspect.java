package com.gw.log.autoconfigure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gw.log.constants.LogAnnotation;
import com.gw.log.entity.SysLog;
import com.gw.log.feign.RemoteLogService;
import com.gw.oauthcm.entity.TokenEntity;
import com.gw.oauthcm.utils.AuthUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lsp
 * @date 2020-03-25 14:31
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);
    @Value("${spring.application.name}")
    private String serverName;
    @Resource
    RemoteLogService remoteLogService;
    @Around(value = "@annotation(com.gw.log.constants.LogAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前毫秒
        long beginTime = System.currentTimeMillis();
        SysLog sysLog = new SysLog();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authorization = request.getHeader("authorization");
        if(authorization!=null){
            TokenEntity tokenInfo = AuthUtil.getTokenInfo();
            if (tokenInfo != null) {
                sysLog.setUserName(tokenInfo.getUserName());
                sysLog.setUserCode(tokenInfo.getUserCode());
                sysLog.setPlatformCode(tokenInfo.getPlatformCode());
            }
        }
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        LogAnnotation logAnnotation = methodSignature.getMethod().getDeclaredAnnotation(LogAnnotation.class);
        sysLog.setOperationModule(logAnnotation.module());
        //描述信息
        sysLog.setRemark(logAnnotation.description());
        //方法路径
        String method_path = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        sysLog.setRequestAddress(method_path);
        //请求方式
        String requestMethod = request.getMethod();
        sysLog.setRequestMethod(requestMethod);
        //系统名称
        sysLog.setSystemName(StringUtils.isNotBlank(sysLog.getSystemName()) ? sysLog.getSystemName() : serverName);
        if (logAnnotation.recordParam() || true) {
            // 获取参数名称
            String[] paramNames = methodSignature.getParameterNames();
            // 获取参数值
            Object[] params = joinPoint.getArgs();
            // 把参数名称和参数值组装成json格式
            JSONObject paramsJson = new JSONObject(paramNames.length);
            for (int i = 0; i < paramNames.length; i++) {
                paramsJson.put(paramNames[i], params[i]);
            }
            try {
                // 以json的形式记录参数
                sysLog.setRequestParameters(JSONObject.toJSONString(paramsJson));
            } catch (Exception e) {
                log.error("记录参数失败：{}", e.getMessage());
            }
        }
        try {
            //添加时间
            sysLog.setCreateTime(new Date());
            //获取用户ip地址
            sysLog.setIpAddress(getIpAddr(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()));
            getMethod(joinPoint, sysLog);
            // 执行原方法
            Object obj = joinPoint.proceed();
            // 执行时长(毫秒)
            Long time = System.currentTimeMillis() - beginTime;
            sysLog.setOperationTime(time.toString());
            //返回值
            String result = JSON.toJSONString(obj);
            sysLog.setReturnParameter(result);
            sysLog.setStatus(Boolean.TRUE);
            return obj;
        } catch (Exception e) {
            // 方法执行失败
            sysLog.setStatus(Boolean.FALSE);
            // 备注记录失败原因
            sysLog.setExctptionMessage(e.getMessage());
            throw e;
        } finally {
            // 异步将Log对象发送到队
                try {
                    String token = request.getHeader("authorization")!=null?request.getHeader("authorization").split(" ")[1]:null;
                    remoteLogService.saveLog(sysLog,token);
                    log.info("通过feign发送到log-center服务：{}", log);
                } catch (Exception e2) {
                    e2.getMessage();
                }
        }
    }
    private void getMethod(ProceedingJoinPoint joinPoint, SysLog sysLog) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        sysLog.setOperationMethod(className + "." + methodName + "()");
        log.info("===========" + log);
    }

    /**
     * 获取target字符第x次出现的位置
     * @param string
     * @param target
     * @param x
     * @return
     */
    public static int getCharacterPosition(String string, String target, int x) {
        // 这里是获取target符号的位置
        Matcher matcher = Pattern.compile(target).matcher(string);
        int mIdx = 0;
        while (matcher.find()) {
            mIdx++;
            // 当target符号第x次出现的位置
            if (mIdx == x) {
                break;
            }
        }
        int start = matcher.start();
        return start;
    }

    /**
     * 获取当前网络ip
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request){
        String ipAddress = request.getHeader("x-forwarded-for");
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")){
                //根据网卡取本机配置的IP
                InetAddress inet=null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    log.error(e.getMessage());
                }
                if (null != inet){
                    ipAddress= inet.getHostAddress();
                }
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ipAddress!=null && ipAddress.length()>15){ //"***.***.***.***".length() = 15
            if(ipAddress.indexOf(",")>0){
                ipAddress = ipAddress.substring(0,ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }
}
