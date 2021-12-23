package com.fisk.datamanagement.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamanagement.dto.entity.EntityDTO;
import com.fisk.datamanagement.dto.entity.EntityDetailDTO;
import com.fisk.datamanagement.dto.entity.EntityTreeDTO;

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
    EntityDetailDTO getEntity(String guid);



}
