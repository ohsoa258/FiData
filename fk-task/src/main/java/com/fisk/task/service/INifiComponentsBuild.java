package com.fisk.task.service;

import com.davis.client.model.ConnectionEntity;
import com.davis.client.model.ControllerServiceEntity;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessorEntity;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.task.entity.dto.nifi.*;

/**
 * Nifi组件创建
 *
 * @author gy
 */
public interface INifiComponentsBuild {

    /* ===========组=========== */

    /**
     * 创建组
     *
     * @param dto dto
     * @return 创建的实体
     */
    BusinessResult<ProcessGroupEntity> buildProcessGroup(BuildProcessGroupDTO dto);

    /**
     * 根据组id获取组
     *
     * @param id 组id
     * @return 组
     */
    BusinessResult<ProcessGroupEntity> getProcessGroupById(String id);

    /* ===========连接池=========== */

    /**
     * 数据库连接对象创建
     *
     * @param dto dto
     * @return 创建的连接对象
     */
    BusinessResult<ControllerServiceEntity> buildDbControllerService(BuildDbControllerServiceDTO dto);

    /**
     * 获取控制器服务的实体
     *
     * @param id controller-service的id
     * @return 控制器服务实体
     */
    ControllerServiceEntity getDbControllerService(String id);

    /**
     * 修改控制器服务运行状态
     *
     * @param id controller-service的id
     * @return 控制器服务实体
     */
    BusinessResult<ControllerServiceEntity> updateDbControllerServiceState(String id);

    /**
     * 修改控制器服务运行状态
     *
     * @param entity 修改的controller-service实体
     * @return 控制器服务实体
     */
    BusinessResult<ControllerServiceEntity> updateDbControllerServiceState(ControllerServiceEntity entity);

    /* 组件创建 */

    /**
     * 创建ExecuteSQL组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildExecuteSqlProcess(BuildExecuteSqlProcessorDTO data);

    /**
     * 创建ConvertToJson组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildConvertToJsonProcess(BuildConvertToJsonProcessorDTO data);

    /**
     * 创建ConvertJsonToSql组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildConvertJsonToSqlProcess(BuildConvertJsonToSqlProcessorDTO data);

    /**
     * 创建ConvertJsonToSql组件
     *
     * @param data dto
     * @return Processor组件
     */
    BusinessResult<ProcessorEntity> buildPutSqlProcess(BuildPutSqlProcessorDTO data);

    /**
     * 连接两个Processor组件
     *
     * @param groupId  组id
     * @param sourceId 源组件的id
     * @param targetId 目标组件的id
     * @return 连接entity
     */
    BusinessResult<ConnectionEntity> buildConnectProcessors(String groupId, String sourceId, String targetId, AutoEndBranchTypeEnum type);

}
