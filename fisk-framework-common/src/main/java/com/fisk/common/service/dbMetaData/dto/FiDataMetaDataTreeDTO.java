package com.fisk.common.service.dbMetaData.dto;

import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description FiData元数据树形结构DTO
 * @date 2022/6/15 11:52
 */
@Data
public class FiDataMetaDataTreeDTO {

    @ApiModelProperty(value = "标识id", required = true)
    public String id;

    @ApiModelProperty(value = "父级id，第一级默认-1", required = true)
    public String parentId;

    @ApiModelProperty(value = "父级name，应用到字段维度，取值为字段父级名称", required = true)
    public String parentName;

    @ApiModelProperty(value = "名称", required = true)
    public String label;

    @ApiModelProperty(value = "别名，没有别名则和名称保持一致", required = true)
    public String labelAlias;

    @ApiModelProperty(value = "业务类型，表和视图维度设置")
    public TableBusinessTypeEnum labelBusinessType;

    @ApiModelProperty(value = "源类型，1、FiData 2、自定义")
    public int sourceType;

    @ApiModelProperty(value = "源ID")
    public int sourceId;

    @ApiModelProperty(value = "层级类型", required = true)
    public LevelTypeEnum levelType;

    @ApiModelProperty(value = "发布状态 0：未发布 1：已发布")
    public String publishState;

    @ApiModelProperty(value = "字段长度")
    public String labelLength;

    @ApiModelProperty(value = "字段类型")
    public String labelType;

    @ApiModelProperty(value = "字段描述")
    public String labelDesc;

    @ApiModelProperty(value = "子集树，递归")
    public List<FiDataMetaDataTreeDTO> children;
}
