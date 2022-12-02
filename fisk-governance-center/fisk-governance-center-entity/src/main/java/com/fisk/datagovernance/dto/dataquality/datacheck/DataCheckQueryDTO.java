package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.fisk.common.core.enums.fidatadatasource.LevelTypeEnum;
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
     * 数据源表主键id
     */
    @ApiModelProperty(value = "数据源表主键id")
    public int datasourceId;

    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public SourceTypeEnum sourceTypeEnum;

    /**
     * 选中的标识
     * 自定义：表名称
     * FiData：表ID
     * 树节点：NodeId
     */
    @ApiModelProperty(value = "选中的标识")
    public String uniqueId;

    /**
     * 节点类型
     */
    @ApiModelProperty(value = "节点类型")
    public LevelTypeEnum levelType;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     */
    @ApiModelProperty(value = "表业务类型 1：事实表、2：维度表、3、指标表  4、宽表")
    public int tableBusinessType;
}
