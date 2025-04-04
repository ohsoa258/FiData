package com.fisk.dataservice.dto.tableapi;

import com.fisk.dataservice.enums.JsonTypeEnum;
import com.fisk.dataservice.enums.RequestTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableApiServiceDTO {

    public long id;

    @ApiModelProperty(value = "api名", required = true)
    public String apiName;

    @ApiModelProperty(value = "api描述", required = true)
    public String apiDes;

    @ApiModelProperty(value = "显示名称", required = true)
    public String displayName;

    @ApiModelProperty(value = "sql脚本", required = true)
    public String sqlScript;

    @ApiModelProperty(value = "来源库id", required = true)
    public Integer sourceDbId;

    @ApiModelProperty(value = "应用ID", required = true)
    public Integer appId;

    @ApiModelProperty(value = "启用或禁用", required = true)
    public Integer enable;

    @ApiModelProperty(value = "api地址", required = true)
    public String apiAddress;

    @ApiModelProperty(value = "1:数组对象2:对象", required = true)
    public Integer jsonType;

    @ApiModelProperty(value = "请求方式1:get2:post")
    public Integer methodType;

    @ApiModelProperty(value = "方法名称")
    public String methodName;

    @ApiModelProperty(value = "是否是重点接口 0否，1是")
    public int importantInterface;

    @ApiModelProperty(value = "特殊处理类型 0:无 1:ksf物料主数据 2:ksf通知单 3:ksf库存状态变更")
    public Integer specialType;

    @ApiModelProperty(value = "起始同步时间")
    public String syncTime;

    @ApiModelProperty(value = "(康师傅用)工厂id")
    public Integer plantId;
}
