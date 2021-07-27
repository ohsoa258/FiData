package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.BusinessNameDTO;
import com.fisk.datamodel.dto.DataAreaDTO;
import com.fisk.datamodel.dto.DataAreaQueryDTO;
import com.fisk.datamodel.entity.DataAreaPO;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
public interface IDataArea extends IService<DataAreaPO> {

    /**
     * 添加数据域时,显示所有业务域
     * @return 执行结果
     */
    List<BusinessNameDTO> getBusinessName();
    /**
     * 添加数据域
     * @param dataAreaDTO dto
     * @return 执行结果
     */
    ResultEnum addData(DataAreaDTO dataAreaDTO);

    /**
     * 回显数据: 根据id查询
     * @param id id
     * @return 查询结果
     */
    DataAreaDTO getData(long id);

    /**
     * 业务域修改
     *
     * @param dataAreaDTO dto
     * @return 执行结果
     */
    ResultEnum updateDataArea(DataAreaDTO dataAreaDTO);

    /**
     * 删除业务域
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteDataArea(long id);

    /**
     * 分页查询
     *
     * @param key  搜索条件
     * @param page 当前页码
     * @param rows 每页显示条数
     * @return 查询结果
     */
    Page<Map<String,Object>> queryByPage(String key, Integer page, Integer rows);

    /**
     * 筛选器
     *
     * @param query 查询条件
     * @return 查询结果
     */
    Page<DataAreaDTO> dataFilter(DataAreaQueryDTO query);

    /**
     * 筛选器获取所有表字段(多表)
     *
     * @return 多表字段
     */
    List<FilterFieldDTO> getBusinessAreaColumn();
}
