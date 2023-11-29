package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.tableapi.*;
import com.fisk.dataservice.dto.tableservice.TableServicePublishStatusDTO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.task.dto.task.BuildTableApiServiceDTO;

import java.util.List;


/**
 *
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2023-09-08 15:44:21
 */
public interface ITableApiService extends IService<TableApiServicePO> {

    /**
     * 获取分页数据分发服务api
     * @param dto
     * @return
     */
    Page<TableApiPageDataDTO> getTableApiListData(TableApiPageQueryDTO dto);

    /**
     * 根据apiId获取配置信息
     * @param apiId
     * @return
     */
    TableApiServiceSaveDTO getApiServiceById(long apiId);

    /**
     * 添加数据分发服务api
     * @param dto
     * @return
     */
    ResultEntity<Object> addTableApiService(TableApiServiceDTO dto);

    /**
     * 数据分发服务Api配置保存
     * @param dto
     * @return
     */
    ResultEnum TableApiServiceSave(TableApiServiceSaveDTO dto);

    /**
     * 删除数据分发服务Api
     *
     * @param id
     * @return
     */
    ResultEnum delTableApiById(long id);

    /**
     * 修改表服务发布状态
     *
     * @param dto
     * @return
     */
    void updateTableServiceStatus(TableServicePublishStatusDTO dto);


    /**
     * 根据管道id获取表服务集合
     *
     * @param pipelineId
     * @return
     */
    List<BuildTableApiServiceDTO> getTableApiListByPipelineId(Integer pipelineId);
    /**
     * 数据分发api同步服务-新增同步按钮,手动同步表服务
     *
     * @param tableServiceSyncDTO
     * @return
     */
    ResultEnum editTableApiServiceSync(TableApiServiceSyncDTO tableServiceSyncDTO);


    /**
     * 启用或禁用
     * @param id
     * @return
     */
    ResultEnum enableOrDisable(Integer id);

    /**
     * 重点或非重点接口
     * @param id
     * @return
     */
    ResultEnum importantOrUnimportant(Integer id);
}

