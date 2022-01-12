package com.fisk.chartvisual.service.impl;

import com.fisk.chartvisual.map.VisualizationMap;
import com.fisk.chartvisual.mapper.DataSourceConMapper;
import com.fisk.chartvisual.service.BuildSqlService;
import com.fisk.chartvisual.service.IDataService;
import com.fisk.chartvisual.service.VisualizationService;
import com.fisk.chartvisual.vo.ChartQueryObjectVO;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author WangYan
 * @date 2022/1/12 14:30
 */
@Service
public class VisualizationServiceImpl implements VisualizationService {

    @Resource
    BuildSqlService BuildSqlService;
    @Resource
    IDataService db;

    @Override
    public DataServiceResult buildSql(ChartQueryObjectVO objectVO) {
        DataServiceResult dataServiceResult = new DataServiceResult();

        switch (objectVO.type) {
            case DMP:
                dataServiceResult.setData(BuildSqlService.query(VisualizationMap.INSTANCES.dataDoFields(objectVO.columnDetails), objectVO.id));
                return dataServiceResult;
            case VIEW:
                return db.query(VisualizationMap.INSTANCES.dataDoObject(objectVO));
            case MDX:
                //db.querySsas();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }
}
