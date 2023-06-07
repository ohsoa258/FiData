package com.fisk.dataservice.vo.logs;

import com.fisk.dataservice.enums.LogTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version 1.0
 * @description 日志
 * @date 2022/3/7 12:09
 */
@Data
public class ApiLogVO {
    /**
     * Id
     */
    @ApiModelProperty(value = "主键")
    public int id;

    /**
     * appId
     */
    @ApiModelProperty(value = "appId")
    public int appId;

    /**
     * apiId
     */
    @ApiModelProperty(value = "apiId")
    public int apiId;

    /**
     * 日志类型：100：数据服务API
     */
    @ApiModelProperty(value = "日志类型")
    public LogTypeEnum logType;

    /**
     * 日志等级：DEBUG、INFO、WARNING、ERROR
     */
    @ApiModelProperty(value = "日志等级")
    public String logLevel;

    /**
     * 请求参数
     */
    @ApiModelProperty(value = "请求参数")
    public String logRequest;

    /**
     * 返回参数涉及到敏感信息，可自定义内容
     */
    @ApiModelProperty(value = "返回参数，可自定义")
    public String logResponseInfo;

    /**
     * 日志信息
     */
    @ApiModelProperty(value = "日志信息")
    public String logInfo;

    /**
     * 业务状态：成功、失败
     */
    @ApiModelProperty(value = "业务状态")
    public String businessState;

    /**
     * 请求开始时间
     */
    @ApiModelProperty(value = "请求开始时间")
    public String requestStartDate;

    /**
     * 参数校验时间
     */
    @ApiModelProperty(value = "参数校验时间")
    public String paramCheckDate;

    /**
     * 请求结束时间
     */
    @ApiModelProperty(value = "请求结束时间")
    public String requestEndDate;

    /**
     * 响应的状态
     */
    @ApiModelProperty(value = "响应的状态")
    public String responseStatus;

    /**
     * 应用id，逗号分隔
     */
    @ApiModelProperty(value = "应用id，逗号分隔")
    public String appIds;

    /**
     * apiCode
     */
    @ApiModelProperty(value = "apiCode")
    public String apiCode;

    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;

    /**
     * 应用账号
     */
    @ApiModelProperty(value = "应用账号")
    public String appAccount;

    /**
     * 应用名称
     */
    @ApiModelProperty(value = "应用名称")
    public String appName;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;
}
