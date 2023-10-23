package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 日志
 * @date 2022/3/7 12:00
 */
@Data
@TableName("tb_logs")
public class LogPO extends BasePO {
    /**
     * APP Id
     */
    public int appId;

    /**
     * API Id
     */
    public int apiId;

    /**
     * 日志类型：100：数据服务API
     */
    public int logType;

    /**
     * 日志等级：DEBUG、INFO、WARNING、ERROR
     */
    public String logLevel;

    /**
     * 请求参数
     */
    public String logRequest;

    /**
     * 返回参数涉及到敏感信息，可自定义内容
     */
    public String logResponseInfo;

    /**
     * 日志信息
     */
    public String logInfo;

    /**
     * 业务状态：成功、失败
     */
    public String businessState;

    /**
     * 请求开始时间
     */
    public String requestStartDate;

    /**
     * 参数校验时间
     */
    public String paramCheckDate;

    /**
     * 请求结束时间
     */
    public String requestEndDate;

    /**
     * 响应的状态
     */
    public String responseStatus;

    /**
     * 应用id，逗号分隔
     */
    public String appIds;

    /**
     * 消费数量
     */
    private Integer number;

    /**
     * 是否是重点接口 0否，1是
     */
    private Integer importantInterface;
}
