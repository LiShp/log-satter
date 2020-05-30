package com.gw.log.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gw.cloud.common.base.util.DateUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

/**
 * 表名：sys_log
*/
@Getter
@Setter
@ToString
public class SysLog  {
    /**
     * 用户名称
     */
    @ApiModelProperty("用户名称")
    private String userName;

    /**
     * 用户code
     */
    @ApiModelProperty("用户code")
    private String userCode;

    /**
     * 平台code
     */
    @ApiModelProperty("平台code")
    private String platformCode;

    /**
     * 用户IP
     */
    @ApiModelProperty("用户IP")
    private String ipAddress;

    /**
     * 系统名称
     */
    @ApiModelProperty("系统名称")
    private String systemName;

    /**
     * 请求方式
     */
    @ApiModelProperty("请求方式")
    private String requestMethod;

    /**
     * 请求参数
     */
    @ApiModelProperty("请求参数")
    private String requestParameters;

    /**
     * 请求接口路径
     */
    @ApiModelProperty("请求接口路径")
    private String requestAddress;

    /**
     * 返回参数
     */
    @ApiModelProperty("返回参数")
    private String returnParameter;

    /**
     * 操作方法
     */
    @ApiModelProperty("操作方法")
    private String operationMethod;

    /**
     * 操作模块
     */
    @ApiModelProperty("操作模块")
    private String operationModule;

    /**
     * 操作时间
     */
    @ApiModelProperty("操作时间")
    private String operationTime;

    /**
     * 操作状态
     */
    @ApiModelProperty("操作状态")
    private Boolean status;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String remark;
    /**
     * 运行错误时异常信息
     */
    @ApiModelProperty("运行错误时异常信息")
    private String exctptionMessage;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @DateTimeFormat(pattern = DateUtil.DEFAULT_FORMAT_PATTERN_DATETIME)
    @JsonFormat(pattern = DateUtil.DEFAULT_FORMAT_PATTERN_DATETIME, timezone = DateUtil.DEFAULT_TIME_ZONE_TYPE)
    private Date createTime;
}