package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.dbBEBuild.datamodel.dto.RelationDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.mdm.dto.access.*;
import com.fisk.mdm.dto.accessmodel.AccessPublishStatusDTO;
import com.fisk.mdm.entity.AccessDataPO;
import com.fisk.task.dto.accessmdm.AccessAttributeDTO;
import com.fisk.task.dto.mdmconfig.AccessMdmConfigDTO;

import java.util.List;


/**
 * @author wangjian
 * @date 2023-04-18
 * @Description: 数据接入
 */
public interface AccessDataService extends IService<AccessDataPO> {

    /**
     * 获取接入字段表列表
     * @param moudleId
     * @param entityId
     * @return
     */
    AccessAttributeListDTO getAccessAttributeList(Integer moudleId,Integer entityId);

    /**
     * 获取默认预览sql
     * @param moudleId
     * @param entityId
     * @return
     */
    Object getAccessDefaultSql(Integer moudleId,Integer entityId);

    /**
     * 更新接入脚本数据
     * @param dto
     * @return
     */
    ResultEnum updateAccessSql(AccessSqlDTO dto);

    /**
     * 添加修改接入属性字段
     * @param dto
     * @return
     */
    ResultEnum addOrUpdateAccessAttribute(AccessAttributeAddDTO dto);

    /**
     * 添加修改接入属性字段
     * @param dto
     * @return
     */
    void updateAccessPublishState(AccessPublishStatusDTO dto);

    /**
     * 获取接入字段映射关系
     *
     * @param accessId
     * @param entityId
     * @return
     */
    public List<AccessAttributeDTO> getAccessAttributeField(int accessId, int entityId);
    /**
     * mdmSQL预览接口
     *
     * @param dto
     * @return
     */
    Object mdmOverlayCodePreview(OverlayCodePreviewAccessDTO dto);

    /** 提供给nifi的数据
     *
     * @param entityId 实体表id
     * @param modelId 模型id
     * @return DataAccessConfigDTO
     */
    ResultEntity<AccessMdmConfigDTO> dataAccessConfig(long entityId, long modelId);

    /**
     * 获取实体表id
     * @return
     */
    List<ChannelDataDTO> getTableId();

    /**
     * 根据参数,获取发布列表
     * @param tableId
     * @return
     */
    List<TableHistoryDTO> getTableHistoryList(Integer tableId);

    List<EntityTableDTO> getEntityTable(long modelId);

    EntityTableDTO getEntityStgTable(long entityId, long modelId);

//    Object buildDomainUpdateScript(List<TableSourceRelationsDTO> dto);

    Object buildDomainUpdateScript(List<RelationDTO> list);
}

