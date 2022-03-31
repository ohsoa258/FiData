package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.dto.atomicindicator.IndicatorQueryDTO;
import com.fisk.datamodel.dto.businessarea.*;
import com.fisk.datamodel.dto.webindex.WebIndexDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
public interface IBusinessArea extends IService<BusinessAreaPO> {


    /**
     * 添加业务域
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(BusinessAreaDTO dto);

    /**
     * 回显数据: 根据id查询
     *
     * @param id id
     * @return 查询结果
     */
    BusinessAreaDTO getData(long id);

    /**
     * 修改业务域
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum updateBusinessArea(BusinessAreaDTO dto);

    /**
     * 根据id删除业务域
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteBusinessArea(long id);

    /**
     * 分页
     *
     * @param key key
     * @param page page
     * @param rows rows
     * @return 查询结果
     */
    Page<Map<String, Object>> queryByPage(String key, Integer page, Integer rows);

    /**
     * 获取tb_area_business表全部字段
     *
     * @return 查询结果
     */
    List<FilterFieldDTO> getBusinessAreaColumn();
    /**
     * 分页
     *

     * @param query query
     * @return 查询结果
     */
    Page<BusinessPageResultDTO> getDataList(BusinessQueryDTO query);

    /**
     * Doris发布
     * @param dto
     * @return
     */
    ResultEnum getBusinessAreaPublicData(IndicatorQueryDTO dto);

    /**
     * 获取业务域数量
     * @return
     */
    WebIndexDTO getBusinessArea();

}
