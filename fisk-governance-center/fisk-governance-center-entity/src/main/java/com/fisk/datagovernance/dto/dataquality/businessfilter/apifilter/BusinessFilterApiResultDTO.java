package com.fisk.datagovernance.dto.dataquality.businessfilter.apifilter;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗 API清洗结果配置DTO
 * @date 2022/10/8 16:21
 */
@Data
public class BusinessFilterApiResultDTO {

    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    public int id;

    /**
     * tb_bizfilter_rule表主键ID
     */
    @ApiModelProperty(value = "tb_bizfilter_rule表主键ID")
    public int ruleId;

    /**
     * tb_bizfilter_api_config表主键ID
     */
    @ApiModelProperty(value = "tb_bizfilter_api_config表主键ID")
    public int apiId;

    /**
     * 结果参数类型 1：授权result参数  2：正文result参数
     */
    @ApiModelProperty(value = "结果参数类型 1：授权result参数  2：正文result参数")
    public int resultParamType;

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
     * code
     */
    @ApiModelProperty(value = "code")
    public String code;

    /**
     * 父级参数code
     */
    @ApiModelProperty(value = "父级参数code")
    public String parentCode;

    /**
     * 授权字段 1:是  2:否
     */
    @ApiModelProperty(value = "授权字段 1:是  2:否")
    public int authField;

    /**
     * 更新标识字段 1:是  2:否
     */
    @ApiModelProperty(value = "更新标识字段 1:是  2:否")
    public int primaryKeyField;

    /**
     * 子级
     */
    @ApiModelProperty(value = "子级")
    public List<BusinessFilterApiResultDTO> children;
}
