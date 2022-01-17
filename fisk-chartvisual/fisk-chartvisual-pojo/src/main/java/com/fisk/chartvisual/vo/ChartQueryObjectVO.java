package com.fisk.chartvisual.vo;

import com.fisk.chartvisual.dto.*;
import com.fisk.chartvisual.enums.GraphicTypeEnum;
import com.fisk.chartvisual.enums.StorageEngineTypeEnum;
import com.fisk.common.enums.chartvisual.InteractiveTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author WangYan
 * @date 2022/1/6 15:54
 */
@Data
public class ChartQueryObjectVO {

    /**
      * 数据源连接id
     */
    @NotNull
    public Integer id;

    /**
     * 生成sql方式
     */
    @NotNull
    public StorageEngineTypeEnum type;
    public String tableName;

    /**
     * 列和值
     */
    public List<FieldDataDTO> columnDetails;
    public List<ChartQueryFilterDTO> queryFilters;
    /**
     * 分页
     */
    public PaginationQueryDTO pagination;

    /**
     * 图表和图形(白泽数据源接口目前没有用到)
     */
    public ChartDrillDown chartDrillDown;
    public GraphicTypeEnum graphicType;
    public InteractiveTypeEnum interactiveType;
}
