package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.DataQualityDataSourceTreeDTO;
import com.fisk.datagovernance.dto.dataops.DataObsSqlDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.*;
import com.fisk.datagovernance.entity.dataquality.DataSourceConPO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataSourceVO;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datagovernance.vo.datasource.ExportResultVO;

import javax.servlet.http.HttpServletResponse;
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
     * 获取FiData数据源元数据信息
     *
     * @return 查询结果
     */
    DataQualityDataSourceTreeDTO getFiDataConfigMetaData();

    /**
     * 获取自定义数据源元数据信息
     *
     * @return 查询结果
     */
    DataQualityDataSourceTreeDTO getCustomizeMetaData();

    /**
     * 根据表ID获取表字段详情
     *
     * @return 查询结果
     */
    ResultEntity<List<DataQualityDataSourceTreeDTO>> getTableFieldByTableId(QueryTableFieldDTO dto);

    /**
     * 数据库信息同步到redis
     *
     * @return 查询结果
     */
    ResultEnum reloadDataSource(int id);


    /**
     * 导出查询结果
     */
    void exportData(ExportResultVO dto, HttpServletResponse response);

    List<DataObsSqlDTO> getObsSqlByUser();

    ResultEnum saveOrUpdateObsSql(List<DataObsSqlDTO> list);

    List<DataSourceVO> getAllDataSetSource();
}
