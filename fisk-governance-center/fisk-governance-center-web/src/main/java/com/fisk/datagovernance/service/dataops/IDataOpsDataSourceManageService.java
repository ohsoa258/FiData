package com.fisk.datagovernance.service.dataops;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataops.ExecuteDataOpsSqlDTO;
import com.fisk.datagovernance.dto.dataops.GetDataOpsFieldSourceDTO;
import com.fisk.datagovernance.dto.dataops.TableDataSyncDTO;
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
     * 获取数据运维数据源中的 实例、库、表信息
     *
     * @return 查询结果
     */
    ResultEntity<List<DataOpsSourceVO>> getDataOpsTableSource();

    /**
     * 获取数据运维数据源中的 字段信息
     *
     * @return 查询结果
     */
    ResultEntity<List<DataOpsTableFieldVO>> getDataOpsFieldSource(GetDataOpsFieldSourceDTO dto);

    /**
     * 执行sql
     *
     * @return 查询结果
     */
    ResultEntity<ExecuteResultVO> executeDataOpsSql(ExecuteDataOpsSqlDTO dto);

    /**
     * 同步表数据
     *
     * @return 查询结果
     */
    ResultEnum tableDataSync(TableDataSyncDTO dto);

    /**
     * 数仓建模-表数据同步
     *
     * @param dto
     * @return
     */
    ResultEnum tableDataSyncForModel(TableDataSyncDTO dto);

    /**
     * 数据库信息同步到redis
     *
     * @return 查询结果
     */
    Object reloadDataOpsDataSource();
}
