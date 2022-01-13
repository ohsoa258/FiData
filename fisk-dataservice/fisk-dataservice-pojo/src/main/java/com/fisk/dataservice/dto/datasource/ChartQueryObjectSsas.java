package com.fisk.dataservice.dto.datasource;

import com.fisk.dataservice.enums.GraphicTypeEnum;
import com.fisk.common.enums.dataservice.InteractiveTypeEnum;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description SSAS查询数据条件
 * @date 2022/1/6 14:51
 */
public class ChartQueryObjectSsas {
    /**
     * 数据源连接id
     */
    @NotNull
    public Integer id;
    public List<ColumnDetailsSsas> columnDetails;
    public List<ChartQueryFilter> queryFilters;
    public ChartDrillDown chartDrillDown;
    public InteractiveTypeEnum interactiveType;
    public GraphicTypeEnum graphicType;
}
