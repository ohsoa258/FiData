package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangjian
 * @date 2023-11-22 11:16:15
 */
@TableName("tb_table_source_relations")
@Data
public class TableSourceRelationsPO extends BasePO {

    @ApiModelProperty(value = "接入配置表id")
    private Integer accessId;

    @ApiModelProperty(value = "源表")
    private String sourceTable;

    @ApiModelProperty(value = "源字段")
    private String sourceColumn;

    @ApiModelProperty(value = "目标表")
    private String targetTable;

    @ApiModelProperty(value = "目标字段")
    private String targetColumn;

    @ApiModelProperty(value = "源表id")
    public Integer sourceEntityId;

    @ApiModelProperty(value = "目标表id")
    public Integer targetEntityId;
}
