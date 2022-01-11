package com.fisk.chartvisual.service;

import com.fisk.chartvisual.vo.ChartQueryObjectVO;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.chartvisual.dto.DataDoFieldDTO;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/12/1 19:50
 */
public interface BuildSqlService {

    /**
     * 白泽数据源生成执行sql
     * @param apiConfigureFieldList
     * @return
     */
    Object query(List<DataDoFieldDTO> apiConfigureFieldList,Integer id);

    /**
     * 报表可视化生成Sql
     * @param objectVO
     * @return
     */
    DataServiceResult buildSql(ChartQueryObjectVO objectVO);
}
