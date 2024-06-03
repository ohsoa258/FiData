package com.fisk.datamanagement.service;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.entity.*;
import com.fisk.datamanagement.dto.lineage.LineAgeDTO;
import com.fisk.datamanagement.dto.metadatalabelmap.MetadataLabelMapParameter;
import com.fisk.datamanagement.dto.metamap.MetaMapDTO;
import com.fisk.datamanagement.dto.metamap.MetaMapTblDTO;
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
     * 为即席查询获取元数据对象树形列表（ods dw mdm）
     * @return
     */
    List<EntityTreeDTO> getEntityListForAdHocQuery();

    /**
     * 为业务术语获取元数据对象树形列表
     * @return
     */
    List<EntityTreeDTO> getEntityListForBusinessTerm();

    /**
     *刷新元数据对象树形列表
     */

    void refreshEntityTreeList();

    /**
     *刷新即席查询元数据对象树形列表（ods dw mdm olap）
     */
    void refreshEntityTreeForAdHocQuery();

    /**
     * 刷新业务术语元数据对象树形列表
     */
    ResultEntity<Object> refreshEntityTreeForTerm();


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
     *
     * @param dto
     * @return
     */
    ResultEnum entityAssociatedLabel(MetadataLabelMapParameter dto);

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

    /**
     * 根据guid和应用名称获取entity详情
     * @param guid
     * @param appName
     * @return
     */
    JSONObject getEntityV2(String guid, String appName);

    /**
     * 根据类型获取获取元数据地图 0数据湖 1数仓
     *
     * @param type 0数据湖 1数仓
     * @return
     */
    List<MetaMapDTO> getMetaMapByType(Integer type);

    /**
     * 元数据地图根据应用id或业务过程id获取表 0数据湖 1数仓
     *
     * @param type 0数据湖 1数仓
     * @param appId 应用id/业务过程id
     * @return
     */
    List<MetaMapTblDTO> getMetaMapTableDetailByType(Integer type, Integer appId,Integer businessType);
}
