package com.fisk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.system.dto.datasource.FiDataMateDataDTO;
import com.fisk.system.dto.datasource.FiDataMateDataQueryDTO;
import com.fisk.system.dto.datasource.TestConnectionDTO;
import com.fisk.system.entity.DataSourcePO;

import java.util.List;

/**
 * 数据源接口
 *
 * @author dick
 */
public interface IDataSourceManageService extends IService<DataSourcePO> {

    /**
     * 获取全部数据源
     *
     * @return 查询结果
     */
    List<DataSourceDTO> getAll();

    /**
     * 获取全部数据源
     *
     * @return 查询结果
     */
    List<DataSourceDTO> getAllDataSourec();

    /**
     * 更新数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum updateDataSource(DataSourceDTO dto);

    /**
     * 测试数据库连接
     *
     * @param dto 连接信息
     * @return 是否连接成功
     */
    ResultEnum testConnection(TestConnectionDTO dto);

    /**
     * 获取单条数据源
     *
     * @return 查询结果
     */
    ResultEntity<DataSourceDTO> getById(int datasourceId);

//    /**
//     * 获取所有数据源元数据信息
//     *
//     * @return 查询结果
//     */
//    ResultEntity<FiDataMateDataDTO> getAllMateData();
//
//    /**
//     * 获取单条数据源元数据信息
//     *
//     * @return 查询结果
//     */
//    ResultEntity<FiDataMateDataDTO> getMateData(FiDataMateDataQueryDTO dto);
}
