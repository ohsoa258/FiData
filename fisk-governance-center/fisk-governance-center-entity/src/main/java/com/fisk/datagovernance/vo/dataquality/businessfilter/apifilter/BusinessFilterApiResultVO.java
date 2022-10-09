package com.fisk.datagovernance.vo.dataquality.businessfilter.apifilter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗结果配置VO
 * @date 2022/10/8 16:50
 */
@Data
public class BusinessFilterApiResultVO {
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    public int id;

    /**
     * tb_bizfilter_rule表主键ID
     */
    @ApiModelProperty(value = "tb_bizfilter_rule表主键ID")
    public String ruleId;

    /**
     * tb_bizfilter_api_config表主键ID
     */
    @ApiModelProperty(value = "tb_bizfilter_api_config表主键ID")
    public int apiId;

    /**
     * 结果参数类型 1：授权result参数  2：正文result参数
     */
    @ApiModelProperty(value = "结果参数类型 1：授权result参数  2：正文result参数")
    public int resultParmType;

    /**
     * 源字段
     */
    @ApiModelProperty(value = "源字段")
    public String sourceField;

    /**
     * 目标字段
     */
    @ApiModelProperty(value = "目标字段")
    public String targetField;

    /**
     * 目标字段标识
     */
    @ApiModelProperty(value = "目标字段标识")
    public String targetFieldUnique;

    /**
     * 父级参数id
     */
    @ApiModelProperty(value = "父级参数id")
    public int parentId;

    /**
     * 授权字段 1:是  2:否
     */
    @ApiModelProperty(value = "授权字段 1:是  2:否")
    public int authField;

    /**
     * 更新标识字段
     */
    @ApiModelProperty(value = "更新标识字段")
    public String primaryKeyField;
}
