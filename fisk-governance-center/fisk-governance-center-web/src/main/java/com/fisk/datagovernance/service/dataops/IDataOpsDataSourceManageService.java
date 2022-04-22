package com.fisk.datagovernance.service.dataops;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datagovernance.dto.dataops.ExecuteDataOpsSqlDTO;
import com.fisk.datagovernance.vo.dataops.ExecuteResultVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataExampleSourceVO;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维数据源接口
 * @date 2022/4/22 13:37
 */
public interface IDataOpsDataSourceManageService {

    /**
     * 获取数据运维全部数据源信息
     * @return 查询结果
     */
    ResultEntity< List<DataExampleSourceVO> > getDataOpsSourceAll();

    /**
     * executeDataOpsSql
     * @return 查询结果
     */
    ResultEntity<ExecuteResultVO> executeDataOpsSql(ExecuteDataOpsSqlDTO dto);
}
