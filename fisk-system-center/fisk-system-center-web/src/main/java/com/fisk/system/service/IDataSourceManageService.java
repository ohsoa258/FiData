package com.fisk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.dto.datasource.DataSourceDTO;
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
     * 获取系统数据源
     *
     * @return 查询结果
     */
    List<DataSourceDTO> getSystemDataSource();

    /**
     * 获取外部数据源
     *
     * @return 查询结果
     */
    List<DataSourceDTO> getExternalDataSource();

    /**
     * 获取全部数据源
     *
     * @return 查询结果
     */
    List<DataSourceDTO> getAllDataSource();

    /**
     * 更新数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum updateDataSource(DataSourceDTO dto);

    /**
     * 新增数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum insertDataSource(DataSourceDTO dto);


    /**
     * 测试数据库连接
     *
     * @param dto 连接信息
     * @return 是否连接成功
     */
    ResultEnum testConnection(DataSourceDTO dto);

    /**
     * 获取单条数据源
     *
     * @return 查询结果
     */
    ResultEntity<DataSourceDTO> getById(int datasourceId);
}
