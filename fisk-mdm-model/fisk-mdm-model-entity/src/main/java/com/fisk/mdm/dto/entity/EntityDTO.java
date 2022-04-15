package com.fisk.mdm.dto.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.mdm.enums.WhetherTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

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
    private String status;

    @ApiModelProperty(value = "表名",required = true)
    private String tableName;

    @ApiModelProperty(value = "是否开启成员日志",required = true)
    private WhetherTypeEnum enableMemberLog;

    @ApiModelProperty(value = "审批规则表id",required = true)
    private Integer approvalRuleId;

    @ApiModelProperty(value = "生成code规则表id",required = true)
    private Integer buildCodeRuleId;

    @ApiModelProperty(value = "派生层级表id",required = true)
    private Integer hierarchyId;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间",required = true)
    public LocalDateTime createTime;

    @ApiModelProperty(value = "创建人",required = true)
    public String createUser;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间",required = true)
    public LocalDateTime updateTime;

    @ApiModelProperty(value = "更新人",required = true)
    public String updateUser;
}
