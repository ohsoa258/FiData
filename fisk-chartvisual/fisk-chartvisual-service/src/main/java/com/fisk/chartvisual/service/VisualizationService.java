package com.fisk.chartvisual.service;

import com.fisk.chartvisual.vo.ChartQueryObjectVO;
import com.fisk.chartvisual.vo.DataServiceResult;

/**
 * @author WangYan
 * @date 2022/1/12 14:29
 */
public interface VisualizationService {

    /**
     * 报表可视化生成Sql
     * @param objectVO
     * @return
     */
    DataServiceResult buildSql(ChartQueryObjectVO objectVO);
}
