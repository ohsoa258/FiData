package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.response.ResultEnum;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowQueryDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.dataservice.dto.ApiConfigureDTO;
import com.fisk.dataservice.dto.ConfigureUserDTO;
import com.fisk.dataservice.dto.DownSystemQueryDTO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.vo.DownSystemQueryVO;

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
     * 根据路径查询拼接sql
     * @param apiRoute
     * @param currentPage 当前页
     * @param pageSize  页数大小
     * @return
     */
    List<Map> queryField(String apiRoute, Integer currentPage, Integer pageSize);

    /**
     * 查询所有Api服务
     * @param page
     * @param apiName
     * @return
     */
    Page<ApiConfigureDTO> queryAll(Page<ApiConfigurePO> page,String apiName);

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

    /**
     * 根据id查询Api服务
     * @param id
     * @return
     */
    ApiConfigurePO getById(Integer id);

    /**
     * 获取过滤器表字段
     * @return 字段
     */
    List<FilterFieldDTO> getColumn();

    /**
     * 筛选器
     * @param query 查询条件
     * @return 筛选结果
     */
    Page<DownSystemQueryVO> whereListData(DownSystemQueryDTO query);
}
