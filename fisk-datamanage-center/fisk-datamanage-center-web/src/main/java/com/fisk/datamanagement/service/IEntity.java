package com.fisk.datamanagement.service;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.entity.*;
import com.fisk.datamanagement.dto.lineage.LineAgeDTO;
import com.fisk.datamanagement.dto.search.SearchBusinessGlossaryEntityDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IEntity {

    /**
     * 获取元数据对象树形列表
     * @return
     */
    List<EntityTreeDTO> getEntityTreeList();

    /**
     * 添加元数据对象：实例、数据库、表、字段
     * @param dto
     * @return
     */
    ResultEnum addEntity(EntityDTO dto);

    /**
     * 根据guid删除entity
     * @param guid
     * @return
     */
    ResultEnum deleteEntity(String guid);

    /**
     * 根据guid获取entity详情
     * @param guid
     * @return
     */
    JSONObject getEntity(String guid);

    /**
     *更改实体数据
     * @param dto
     * @return
     */
    ResultEnum updateEntity(JSONObject dto);

    /**
     * 根据不同条件,筛选元数据对象列表
     * @param dto
     * @return
     */
    SearchBusinessGlossaryEntityDTO searchBasicEntity(EntityFilterDTO dto);

    /**
     * 根据实体id,获取审计列表
     * @param guid
     * @return
     */
    List<EntityAuditsDTO> getAuditsList(String guid);

    /**
     * 实体批量关联标签
     * @param dto
     * @return
     */
    ResultEnum entityAssociatedLabel(EntityAssociatedLabelDTO dto);

    /**
     * 实体批量关联业务元数据
     * @param dto
     * @return
     */
    ResultEnum entityAssociatedMetaData(EntityAssociatedMetaDataDTO dto);

    /**
     * 获取元数据对象血缘关系
     * @param guid
     * @return
     */
    LineAgeDTO getMetaDataKinship(String guid);

    /**
     * 获取实例详情
     * @param guid
     * @return
     */
    EntityInstanceDTO getInstanceDetail(String guid);

}
