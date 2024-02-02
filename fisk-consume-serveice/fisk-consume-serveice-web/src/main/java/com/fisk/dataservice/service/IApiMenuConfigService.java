package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.api.ApiMenuDTO;
import com.fisk.dataservice.dto.api.ApiSortDTO;
import com.fisk.dataservice.dto.api.ApiTreeDTO;
import com.fisk.dataservice.entity.ApiMenuConfigPO;

import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2024-01-26 14:46:30
 */
public interface IApiMenuConfigService extends IService<ApiMenuConfigPO> {

    List<ApiTreeDTO> getApiTree(Integer serverType);

    ResultEnum addorUpdateApiMenu(ApiMenuDTO dto);

    /**
     * 删除Api标签
     * @param ids
     * @return
     */
    ResultEnum delApiMenu(List<Integer> ids);

    ResultEnum apiSort(ApiSortDTO dto);

}

