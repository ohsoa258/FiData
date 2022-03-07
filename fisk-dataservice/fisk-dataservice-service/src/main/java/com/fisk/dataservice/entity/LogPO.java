package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
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
}
