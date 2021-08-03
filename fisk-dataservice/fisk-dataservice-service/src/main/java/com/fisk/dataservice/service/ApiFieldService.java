package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.ApiConfigureDTO;
import com.fisk.dataservice.dto.ConfigureUserDTO;
import com.fisk.dataservice.entity.ApiConfigurePO;

import java.util.List;
import java.util.Map;

/**
 * @author wangyan
 */
public interface ApiFieldService {

    /**
     * 根据路径查询拼接sql
     * @param apiRoute
     * @param currentPage 当前页
     * @param pageSize  页数大小
     * @param user  用户信息
     * @return
     */
    List<Map> queryField(String apiRoute, Integer currentPage, Integer pageSize, ConfigureUserDTO user);

    /**
     * 查询所有Api服务
     * @param page
     * @return
     */
    List<ApiConfigurePO> queryAll(Page<ApiConfigurePO> page);

    /**
     * 修改Api接口
     * @param dto
     * @return
     */
    ResultEnum updateApiConfigure(ApiConfigureDTO dto);

    /**
     * 删除Api接口
     * @param id
     * @return
     */
    ResultEnum deleteApiById(Integer id);
}
