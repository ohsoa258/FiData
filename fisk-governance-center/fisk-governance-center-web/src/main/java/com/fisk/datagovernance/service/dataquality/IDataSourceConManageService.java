package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.*;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataExampleSourceVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceVO;

import java.util.List;

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
    PageDTO<DataSourceConVO> page(DataSourceConQuery query);

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
    List<FiDataMetaDataDTO> getFiDataConfigMetaData();

    /**
     * 获取数据源下的表
     *
     * @return 查询结果
     */
    List<DataExampleSourceVO> getTableAll();

    /**
     * 获取表字段信息
     *
     * @return 查询结果
     */
    DataSourceVO getTableFieldAll(TableFieldQueryDTO dto);
}
