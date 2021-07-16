package com.fisk.dataservice.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import java.util.List;

/**
 * @author wangyan
 */
public interface ApiConfigureFieldService {

    /**
     * 接口字段保存方法
     * @param dto
     * @return
     */
    ResultEnum saveConfigureField(ApiConfigureFieldPO dto);

    /**
     * 添加服务接口
     * @param apiName
     * @param apiInfo
     * @return
     */
    ResultEnum saveApiConfigure(String apiName, String apiInfo);

    /**
     * 根据主键id删除字段
     * @param id
     * @return
     */
    ResultEnum deleteDataById(Integer id);

    /**
     * 修改字段
     * @param dto
     * @return
     */
    ResultEnum updateField(ApiConfigureFieldPO dto);

    /**
     * 根据id查询字段
     * @param id
     * @return
     */
    ApiConfigureFieldPO getDataById(Integer id);

    /**
     * 分页查询
     * @param currentPage 当前页数
     * @param pageSize    页数大小
     * @return
     */
    List<ApiConfigureFieldPO> listData(Integer currentPage,Integer pageSize);
}
