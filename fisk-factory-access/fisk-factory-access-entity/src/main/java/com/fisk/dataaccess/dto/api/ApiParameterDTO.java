package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * api请求参数
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-04-26 11:07:14
 */
@Data
public class ApiParameterDTO {

    @ApiModelProperty(value = "主键", required = true)
    public long id;

    @ApiModelProperty(value = "左边非实时api的id", required = true)
    public long apiId;

    @ApiModelProperty(value = "Headers or Body", required = true)
    @NotNull
    public String requestType;

    @ApiModelProperty(value = "form-data or raw")
    public String requestMethod;

    @ApiModelProperty(value = "请求参数key or Headers的key", required = true)
    public String parameterKey;

    @ApiModelProperty(value = "请求参数value or Headers的value", required = true)
    public String parameterValue;

    @ApiModelProperty(value = "参数类型：1常量 2表达式 3脚本")
    public Integer parameterType;

    @ApiModelProperty(value = "参数类型为表达式,表达式的类型为聚合函数,聚合字段所属的表id")
    public Integer tableAccessId;

    @ApiModelProperty(value = "当选择body时: 1字段，2表")
    public Integer attributeType;

    @ApiModelProperty(value = "当选择body时: 字段名称")
    public String attributeFieldName;

    @ApiModelProperty(value = "当选择body时: 值类型，值数组，字符串，对象组")
    public String attributeFieldType;

    @ApiModelProperty(value = "当选择body时: 推送规则")
    public String attributeFieldRule;

    @ApiModelProperty(value = "当选择body时: 描述")
    public String attributeFieldDesc;

    @ApiModelProperty(value = "当选择body时: 字段的样例数据")
    public String attributeFieldSample;

    @ApiModelProperty(value = "当选择body时: 字段的上一级字段")
    public String attributeFieldParent;

}
