package com.fisk.dataservice.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author wangjian
 * @date 2023-09-08 15:44:21
 */
@Data
@TableName("tb_table_api_service")
public class TableApiServicePO extends BasePO {

    @ApiModelProperty(value = "appId")
    private String appId;

    @ApiModelProperty(value = "API名称")
    private String apiName;

    @ApiModelProperty(value = "显示名称")
    private String displayName;

    @ApiModelProperty(value = "api描述")
    private String apiDes;

    @ApiModelProperty(value = "sql脚本")
    private String sqlScript;

    @ApiModelProperty(value = "来源库id")
    private Integer sourceDbId;

    @ApiModelProperty(value = "0: 未发布  1: 发布成功  2: 发布失败")
    private Integer publish;

    @ApiModelProperty(value = "1:启用 0:禁用")
    private Integer enable;

    @ApiModelProperty(value = "1:数组对象2:对象")
    private Integer jsonType;

    @ApiModelProperty(value = "api地址")
    private String apiAddress;

    @ApiModelProperty(value = "请求方式1:get2:post")
    private Integer methodType;

    @ApiModelProperty(value = "方法名称")
    private String methodName;

    @ApiModelProperty(value = "是否是重点接口 0否，1是")
    private Integer importantInterface;

    @ApiModelProperty(value = "特殊处理类型 0:无 1:ksf物料主数据 2:ksf通知单 3:ksf库存状态变更")
    public Integer specialType;

    @ApiModelProperty(value = "起始同步时间")
    public String syncTime;
}
