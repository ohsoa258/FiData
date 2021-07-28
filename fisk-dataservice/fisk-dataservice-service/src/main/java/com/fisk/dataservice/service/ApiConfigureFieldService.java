package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataservice.dto.ApiFieldDataDTO;
import com.fisk.dataservice.dto.ApiConfigureFieldEditDTO;
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
    ResultEnum saveConfigure(ApiFieldDataDTO dto);

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
    ResultEnum updateField(ApiConfigureFieldEditDTO dto);

    /**
     * 根据id查询字段
     * @param id
     * @return
     */
    ApiConfigureFieldPO getDataById(Integer id);

    /**
     * 分页查询
     * @param page
     * @return
     */
    List<ApiConfigureFieldPO> listData(Page<ApiConfigureFieldPO> page);
}
