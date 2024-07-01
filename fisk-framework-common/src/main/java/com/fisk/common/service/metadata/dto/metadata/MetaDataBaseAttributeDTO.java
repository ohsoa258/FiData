package com.fisk.common.service.metadata.dto.metadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-07-01 14:26
 */
@Data
public class MetaDataBaseAttributeDTO {
    @ApiModelProperty(value = "限定名")
    public String qualifiedName;
    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "联系人信息")
    public String contact_info;
    @ApiModelProperty(value = "描述")
    public String description;
    @ApiModelProperty(value = "说明，数据接入中的应用ID")
    public String comment;
    @ApiModelProperty(value = "所属人")
    public String owner;
    @ApiModelProperty(value = "显示名称")
    public String displayName;
    @ApiModelProperty(value = "当前操作用户")
    public String currUserName;

    /**
     * 数据分类：DataClassificationEnum
     * PUBLIC_DATA(1, "公开数据", "green"),
     * INTERNAL_DATA(2, "内部数据", "blue"),
     * MAX(3, "敏感数据", "orange"),
     * MIN(4, "高度敏感数据", "red"),
     */
    @ApiModelProperty(value = "数据分类：DataClassificationEnum")
    public Integer dataClassification;

    /**
     * 数据分级：DataLevelEnum
     * LEVEL1(1, "一级（一般数据）", "green"),
     * LEVEL2(2, "二级（重要数据）", "blue"),
     * LEVEL3(3, "三级（敏感数据）", "orange"),
     * LEVEL4(4, "四级（核心数据）", "red"),
     */
    @ApiModelProperty(value = "数据分级：DataLevelEnum")
    public Integer dataLevel;

}
