package com.fisk.datamanagement.service;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityDetailDTO;
import com.fisk.datamanagement.dto.entity.EntityFilterDTO;
import com.fisk.datamanagement.dto.entity.EntityTreeDTO;
import com.fisk.datamanagement.vo.JsonObjectDTO;

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
     * @param entityData
     * @return
     */
    ResultEnum updateEntity(JSONObject entityData);

    /**
     * 根据不同条件,筛选元数据对象列表
     * @param dto
     * @return
     */
    JSONObject searchBasicEntity(EntityFilterDTO dto);

    /**
     * 根据实体id,获取审计列表
     * @param guid
     * @return
     */
    JsonObjectDTO getAuditsList(String guid);


}
