package com.fisk.mdm.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/4/2 18:17
 */
@Data
public class EntityDTO {

    @ApiModelProperty(value = "主键id",required = true)
    private Integer id;

    @ApiModelProperty(value = "模型id",required = true)
    private Integer modelId;

    @ApiModelProperty(value = "实体名称",required = true)
    private String name;

    @ApiModelProperty(value = "展示名称",required = true)
    private String displayName;

    @ApiModelProperty(value = "描述",required = true)
    private String desc;

    @ApiModelProperty(value = "状态",required = true)
    private Integer status;

    @ApiModelProperty(value = "表名",required = true)
    private String tableName;

    @ApiModelProperty(value = "是否开启成员日志",required = true)
    private Boolean enableMemberLog;

    @ApiModelProperty(value = "审批规则表id",required = true)
    private Integer approvalRuleId;

    @ApiModelProperty(value = "生成code规则表id",required = true)
    private Integer buildCodeRuleId;

    @ApiModelProperty(value = "派生层级表id",required = true)
    private Integer hierarchyId;
}
