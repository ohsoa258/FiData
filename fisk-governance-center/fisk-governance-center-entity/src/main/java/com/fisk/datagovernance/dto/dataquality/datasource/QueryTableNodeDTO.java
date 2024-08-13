package com.fisk.datagovernance.dto.dataquality.datasource;

import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 查询数据源tree表节点
 * @date 2022/12/2 16:32
 */
@Data
public class QueryTableNodeDTO {
    /**
     * 源id，如果sourceType为FiData则为平台数据源ID，如果为custom则为数据质量数据源表主键ID
     */
    @ApiModelProperty(value = "源id")
    public int sourceId;

    /**
     * 数据源类型
     */
    @ApiModelProperty(value = "数据源类型")
    public SourceTypeEnum sourceType;
}
