package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验查询DTO
 * @date 2022/3/24 13:21
 */
@Data
public class DataCheckQueryDTO {
    /**
     * 数据源表主键id，查询表/视图时必填
     */
    @ApiModelProperty(value = "数据源表主键id，选中表/视图节点必填")
    public int datasourceId;

    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型，选中节点必填")
    public SourceTypeEnum sourceType;

    /**
     * 选中的标识
     * 自定义：表名称
     * FiData：表ID
     * 树节点：NodeId
     */
    @ApiModelProperty(value = "选中的标识，选中节点必填")
    public String uniqueId;

    /**
     * 节点类型，选中后节点类型必填
     */
    @ApiModelProperty(value = "节点类型，选中节点必填")
    public LevelTypeEnum levelType;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     */
    @ApiModelProperty(value = "表业务类型，选中表/视图节点必填")
    public TableBusinessTypeEnum tableBusinessType;

    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数")
    public int size;

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码")
    public int current;
}
