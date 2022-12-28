package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.datasource.DataSourceConfigInfoDTO;
import com.fisk.dataservice.dto.tableservice.*;
import com.fisk.task.dto.task.BuildTableServiceDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ITableService {

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    Page<TableServicePageDataDTO> getTableServiceListData(TableServicePageQueryDTO dto);

    /**
     * 新增表
     *
     * @param dto
     * @return
     */
    ResultEntity<Object> addTableServiceData(TableServiceDTO dto);

    /**
     * 获取数据源配置
     *
     * @return
     */
    List<DataSourceConfigInfoDTO> getDataSourceConfig();

    /**
     * 表服务保存
     *
     * @param dto
     * @return
     */
    ResultEnum TableServiceSave(TableServiceSaveDTO dto);

    /**
     * 获取表服务详情
     *
     * @param id
     * @return
     */
    TableServiceSaveDTO getTableServiceById(long id);

    /**
     * 删除表服务
     *
     * @param id
     * @return
     */
    ResultEnum delTableServiceById(long id);

    /**
     * 根据管道id获取表服务集合
     *
     * @param pipelineId
     * @return
     */
    List<BuildTableServiceDTO> getTableListByPipelineId(Integer pipelineId);

    /**
     * 修改表服务发布状态
     *
     * @param dto
     * @return
     */
    void updateTableServiceStatus(TableServicePublishStatusDTO dto);

    /**
     * 根据表服务id构建发布数据
     *
     * @param id
     * @return
     */
    BuildTableServiceDTO getBuildTableServiceById(long id);

}
