package com.fisk.dataservice.service;

import com.fisk.dataservice.dto.ConfigureUserDTO;

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
}
