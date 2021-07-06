package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.DataSourceAreaDTO;
import com.fisk.datamodel.entity.DataSourceAreaPO;

import java.util.List;

/**
 * @author Lock
 */
public interface IDataSourceArea extends IService<DataSourceAreaPO> {

    /**
     * 添加计算数据源
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(DataSourceAreaDTO dto);

    /**
     * 回显数据: 根据id查询
     *
     * @param id id
     * @return 查询结果
     */
    DataSourceAreaDTO getData(long id);

    /**
     * 计算数据源修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum updateDataSourceArea(DataSourceAreaDTO dto);

    /**
     * 计算数据源删除
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteDataSourceArea(long id);
    /**
     * 计算数据源首页展示
     * @return 查询结果
     */
    List<DataSourceAreaDTO> listDataSource();
}
