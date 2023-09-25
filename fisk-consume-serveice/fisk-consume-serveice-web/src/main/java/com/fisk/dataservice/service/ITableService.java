package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataReqDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.dataservice.dto.datasource.DataSourceConfigInfoDTO;
import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.dto.tableservice.*;
import com.fisk.dataservice.vo.tableservice.TableRecipientsVO;
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
     * 获取平台所有数据源配置
     *
     * @return
     */
    List<DataSourceConfigInfoDTO> getAllDataSourceConfig();

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

    /**
     * 新增表字段
     *
     * @param dto
     * @return
     */
    ResultEnum addTableServiceField(TableFieldDTO dto);

    /**
     * 修改表字段
     *
     * @param dto
     * @return
     */
    ResultEnum editTableServiceField(TableFieldDTO dto);

    /**
     * 删除表字段
     *
     * @param tableFieldId
     * @return
     */
    ResultEnum deleteTableServiceField(long tableFieldId);

    /**
     * 数据库同步服务-新增同步按钮,手动同步表服务
     *
     * @param tableServiceSyncDTO
     * @return
     */
    ResultEnum editTableServiceSync(TableServiceSyncDTO tableServiceSyncDTO);

    /**
     * 查询表服务应用告警通知配置
     *
     * @param tableAppId
     * @return
     */
    TableRecipientsVO getTableServiceAlarmNoticeByAppId(int tableAppId);

    /**
     * 保存表服务应用告警通知配置
     *
     * @param dto
     * @return
     */
    ResultEnum saveTableServiceAlarmNotice(TableRecipientsDTO dto);

    /**
     * 删除
     *
     * @param tableServiceEmail
     * @return
     */
    ResultEnum deleteTableServiceEmail(TableServiceEmailDTO tableServiceEmail);


    /**
     * 调用邮件服务器发邮件的方法
     *
     * @param tableServiceEmail
     * @return
     */
    ResultEnum tableServiceSendEmails(TableServiceEmailDTO tableServiceEmail);

    /**
     * 获取所有表名
     * @return
     */
    List<String> getTableName();

    /**
     * 启用或禁用
     * @param id
     * @return
     */
    ResultEnum enableOrDisable(Integer id);

    /**
     * 获取数据分发服务结构
     *
     * @param dto dto
     * @return list
     */
    List<FiDataMetaDataDTO> getDataServiceStructure(FiDataMetaDataReqDTO dto);

    /**
     * 获取数据分发服务表结构
     *
     * @param dto dto
     * @return list
     */
    List<FiDataMetaDataTreeDTO> getDataServiceTableStructure(FiDataMetaDataReqDTO dto);

    /**
     * 刷新数据分发服务结构
     *
     * @param dto dto
     * @return list
     */
    boolean setDataServiceStructure(FiDataMetaDataReqDTO dto);
}
