package com.fisk.datamanagement.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class EntityFilterDTO {
    @ApiModelProperty(value = "排除已删除实体,默认为true",required = true)
    public boolean excludeDeletedEntities;
    @ApiModelProperty(value = "包含子分类,默认为true",required = true)
    public boolean includeSubClassifications;
    @ApiModelProperty(value = "包含子类型,默认为true",required = true)
    public boolean includeSubTypes;
    @ApiModelProperty(value = "包含分类属性,默认为true",required = true)
    public boolean includeClassificationAttributes;
    @ApiModelProperty(value = "筛选字段,默认值为null")
    public String entityFilters;
    @ApiModelProperty(value = "筛选标签,默认值为null")
    public String tagFilters;
    public List<String> attributes;
    @ApiModelProperty(value = "每页条数",required = true)
    public int limit;
    @ApiModelProperty(value = "偏移量", required = true)
    public int offset;
    @ApiModelProperty(value = "筛选元数据对象下实体类型,默认值为null,typeName包括:rdbms_instance/rdbms_db/rdbms_table/rdbms_column")
    public String typeName;
    @ApiModelProperty(value = "筛选业务分类下实体数据,默认值为null")
    public String classification;
    @ApiModelProperty(value = "筛选术语下实体数据,默认值为null,值为获取qualifiedName参数的值")
    public String termName;
    @ApiModelProperty(value = "根据suggestions值,筛选")
    public String query;
    @ApiModelProperty(value = "根据属性标签查询")
    public String label;
}
