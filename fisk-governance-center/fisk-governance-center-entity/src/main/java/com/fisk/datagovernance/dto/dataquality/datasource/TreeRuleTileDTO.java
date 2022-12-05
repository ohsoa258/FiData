package com.fisk.datagovernance.dto.dataquality.datasource;

import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description Tree节点平铺成列表
 * @date 2022/12/2 10:09
 */
@Data
public class TreeRuleTileDTO {
    @ApiModelProperty(value = "标识ID")
    public String id;

    @ApiModelProperty(value = "父级标识ID")
    public String parentId;

    @ApiModelProperty(value = "标识名称")
    public String name;

    @ApiModelProperty(value = "业务类型，表和视图维度设置")
    public int labelBusinessType;

    @ApiModelProperty(value = "层级类型")
    public LevelTypeEnum levelTypeEnum;

    @ApiModelProperty(value = "校验规则数量")
    public int checkRuleCount;

    @ApiModelProperty(value = "清洗规则数据")
    public int filterRuleCount;

    @ApiModelProperty(value = "回收规则数量")
    public int recoveryRuleCount;
}
