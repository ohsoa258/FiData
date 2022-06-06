package com.fisk.datagovernance.service.dataops;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datagovernance.dto.dataops.ExecuteDataOpsSqlDTO;
import com.fisk.datagovernance.vo.dataops.DataOpsSourceVO;
import com.fisk.datagovernance.vo.dataops.DataOpsTableFieldVO;
import com.fisk.datagovernance.vo.dataops.ExecuteResultVO;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据运维数据源接口
 * @date 2022/4/22 13:37
 */
public interface IDataOpsDataSourceManageService {

    /**
     * 获取数据运维数据源中的 实例、库、表、字段信息
     *
     * @return 查询结果
     */
    ResultEntity<List<DataOpsSourceVO>> getDataOpsDataSource();

    /**
     * 执行sql
     *
     * @return 查询结果
     */
    ResultEntity<ExecuteResultVO> executeDataOpsSql(ExecuteDataOpsSqlDTO dto);

    /**
     * pg数据库信息同步到redis
     *
     * @return 查询结果
     */
    Object reloadDataOpsDataSource();
}
