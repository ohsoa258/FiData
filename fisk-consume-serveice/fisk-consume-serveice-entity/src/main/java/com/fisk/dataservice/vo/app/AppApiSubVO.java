package com.fisk.dataservice.vo.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author dick
 * @version v1.0
 * @description 应用订阅API VO
 * @date 2022/1/10 17:51
 */
@Data
public class AppApiSubVO {
    /**
     * Id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    public int appId;

    /**
     * 服务id
     */
    @ApiModelProperty(value = "API id")
    public int serviceId;

    /**
     * API 状态 1启用、0禁用
     */
    @ApiModelProperty(value = "apiState")
    public int apiState;

    /**
     * 服务名称名称
     */
    @ApiModelProperty(value = "服务名称名称")
    public String serviceName;

    /**
     * api标识
     */
    @ApiModelProperty(value = "api标识")
    public String apiCode;

    /**
     * 服务描述
     */
    @ApiModelProperty(value = "服务描述")
    public String serviceDesc;

    /**
     * 类型：1api服务 2表服务 3 文件服务
     */
    @ApiModelProperty(value = "类型：1api服务 2表服务 3 文件服务")
    public Integer type;

    /**
     * 数据源ID
     */
    @ApiModelProperty(value = "数据源ID")
    public Integer dataSourceId;

    /**
     * 创建api类型
     */
    @ApiModelProperty(value = "创建api类型：1 创建新api 2 使用现有api 3 代理API")
    public Integer createApiType;

    /**
     * api代理转发地址
     */
    @ApiModelProperty(value = "api代理转发地址")
    public String apiProxyUrl;

    /**
     * api代理调用地址
     */
    @ApiModelProperty(value = "api代理调用地址")
    public String apiProxyCallUrl;
}
