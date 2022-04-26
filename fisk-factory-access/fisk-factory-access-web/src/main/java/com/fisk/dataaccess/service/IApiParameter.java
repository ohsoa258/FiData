package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.api.ApiParameterDTO;
import com.fisk.dataaccess.entity.ApiParameterPO;

import java.util.List;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-04-26 11:07:14
 */
public interface IApiParameter extends IService<ApiParameterPO> {

    /**
     * 根据apiId查询请求参数集合
     *
     * @param apiId apiId
     * @return 查询结果
     */
    List<ApiParameterDTO> getListByApiId(long apiId);

    /**
     * 添加API请求参数集合
     *
     * @param dtoList dtoList
     * @return 执行结果
     */
    ResultEnum addData(List<ApiParameterDTO> dtoList);

    /**
     * 修改API请求参数集合
     *
     * @param dtoList dtoList
     * @return 执行结果
     */
    ResultEnum editData(List<ApiParameterDTO> dtoList);

    /**
     * 删除当前请求参数
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);
}

