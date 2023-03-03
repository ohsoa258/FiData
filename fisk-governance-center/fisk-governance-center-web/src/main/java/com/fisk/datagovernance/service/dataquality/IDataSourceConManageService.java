package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.*;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;


/**
 * 数据源接口
 *
 * @author dick
 */
public interface IDataSourceConManageService extends IService<DataSourceConPO> {

    /**
     * 获取所有的数据源
     *
     * @param query 查询参数
     * @return 查询结果
     */
    Page<DataSourceConVO> page(DataSourceConQuery query);

    /**
     * 保存数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum add(DataSourceConDTO dto);

    /**
     * 更新数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum edit(DataSourceConEditDTO dto);

    /**
     * 删除数据
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum delete(int id);

    /**
     * 测试数据库连接
     *
     * @param dto 连接信息
     * @return 是否连接成功
     */
    ResultEnum testConnection(TestConnectionDTO dto);

    /**
     * 获取FiData配置表元数据
     *
     * @return 查询结果
     */
    FiDataMetaDataTreeDTO getFiDataConfigMetaData(boolean isComputeRuleCount);

    /**
     * 获取自定义数据源元数据
     *
     * @return 查询结果
     */
    FiDataMetaDataTreeDTO getCustomizeMetaData(boolean isComputeRuleCount);

    /**
     * 数据库信息同步到redis
     *
     * @return 查询结果
     */
    Object reloadDataSource(int id);
}
