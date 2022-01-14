package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEntity;
import com.fisk.dataservice.dto.datasource.DataSourceConDTO;
import com.fisk.dataservice.dto.datasource.DataSourceConEditDTO;
import com.fisk.dataservice.dto.datasource.DataSourceConQuery;
import com.fisk.dataservice.entity.DataSourceConPO;
import com.fisk.dataservice.dto.datasource.TestConnectionDTO;
import com.fisk.dataservice.vo.datasource.DataDomainVO;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.vo.datasource.DimensionVO;

import java.util.List;

/**
 * 数据源接口
 * @author dick
 */
public interface IDataSourceConManageService extends IService<DataSourceConPO> {

    /**
     * 获取权限内所有的数据源
     * @param page 分页对象
     * @param query 查询参数
     * @return 查询结果
     */
    Page<DataSourceConVO> listDataSourceCons(DataSourceConQuery query);

    /**
     * 保存数据
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum saveDataSourceCon(DataSourceConDTO dto);

    /**
     * 更新数据
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum updateDataSourceCon(DataSourceConEditDTO dto);

    /**
     * 删除数据
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteDataSourceCon(int id);

    /**
     * 测试数据库连接
     * @param dto 连接信息
     * @return 是否连接成功
     */
    ResultEnum testConnection(TestConnectionDTO dto);

    /**
     * 获取数据域连接下的所有数据源
     * @param id 数据源连接地址
     * @return 数据源下的数据域
     */
    ResultEntity<List<DataDomainVO>> listDataDomain(int id);

    /**
     *获取Tabular或Cube下的数据结构
     * @param id 数据源连接地址
     * @return Tabular或Cube下的数据结构
     */
    ResultEntity<List<DimensionVO>> SSASDataStructure(int id);
}
