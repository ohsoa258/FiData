package com.fisk.mdm.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/25 17:37
 * @Version 1.0
 */
@Data
@NoArgsConstructor
public class EntityQueryDTO {

    @ApiModelProperty(value = "Id")
    private Integer id;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "类型")
    private String type;
    @ApiModelProperty(value = "数据类型")
    private String dataType;
    @ApiModelProperty(value = "展示名称")
    private String displayName;
    @ApiModelProperty(value = "别名")
    private String aliasName;
    @ApiModelProperty(value = "排序")
    private String desc;
    @ApiModelProperty(value = "数据类型长度")
    private Integer dataTypeLength;
    @ApiModelProperty(value = "数据类型小数长度")
    private Integer dataTypeDecimalLength;
    @ApiModelProperty(value = "域实体Id")
    private Integer domainEntityId;
    @ApiModelProperty(value = "域名称")
    private String domainName;
    @ApiModelProperty(value = "映射类型")
    private String mapType;
    /**
     * 实体id,实体名称
     */
    @ApiModelProperty(value = "实体id")
    private Integer entityId;
    @ApiModelProperty(value = "实体名称")
    private String entityName;
    @ApiModelProperty(value = "显示实体名称")
    private String entityDisplayName;

    /**
     * 是否选中 0:未选中 1:选中
     */
    @ApiModelProperty(value = "是否选中 0:未选中 1:选中")
    private Integer isCheck;
    /**
     * 是否选中 0:是 1:不是
     */
    @ApiModelProperty(value = "是否选中 0:是 1:不是")
    private Integer isMainEntity;
    @ApiModelProperty(value = "子类")
    private List<EntityQueryDTO> children;

    public EntityQueryDTO(Integer id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
}
