package com.fisk.common.service.dbMetaData.dto;

import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量数据源树形结构DTO
 * @date 2024/8/28 15:55
 */
@Data
public class DataQualityDataSourceTreeDTO {

    @ApiModelProperty(value = "标识id/节点id", required = true)
    public String id;

    @ApiModelProperty(value = "父级节点id，第一级默认-1", required = true)
    public String parentId;

    @ApiModelProperty(value = "节点名称，如果是表节点则带架构名", required = true)
    public String label;

    @ApiModelProperty(value = "节点别名，没有别名则和名称保持一致", required = true)
    public String labelAlias;

    @ApiModelProperty(value = "节点名称，如果是表节点也不带架构名")
    public String labelRelName;

    @ApiModelProperty(value = "架构名，只有表节点才有架构名")
    public String labelFramework;

    @ApiModelProperty(value = "业务类型，对应TableBusinessTypeEnum枚举")
    public int labelBusinessType;

    @ApiModelProperty(value = "平台数据源类型，1、FiData 2、自定义", required = true)
    public int sourceType;

    @ApiModelProperty(value = "平台数据源ID", required = true)
    public int sourceId;

    @ApiModelProperty(value = "节点层级类型", required = true)
    public LevelTypeEnum levelType;

    @ApiModelProperty(value = "发布状态 0：未发布 1：已发布，只有表节点才有发布状态")
    public String publishState;

    @ApiModelProperty(value = "字段长度，只有字段节点才有字段长度")
    public String labelLength;

    @ApiModelProperty(value = "字段类型，只有字段节点才有字段类型")
    public String labelType;

    @ApiModelProperty(value = "描述，只有表或者字段节点才有描述")
    public String labelDesc;

    @ApiModelProperty(value = "子集树，递归")
    public List<DataQualityDataSourceTreeDTO> children;
}
