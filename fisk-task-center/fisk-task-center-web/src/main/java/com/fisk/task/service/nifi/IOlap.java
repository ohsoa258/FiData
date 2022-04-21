package com.fisk.task.service.nifi;

import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datamodel.dto.businessarea.BusinessAreaGetDataDTO;
import com.fisk.datamodel.dto.dimension.ModelMetaDataDTO;
import com.fisk.task.entity.OlapPO;

import java.util.List;

/**
 * 建模
 *
 * @author JinXingWang
 */
public interface IOlap {
    /**
     * 生成创建模型sql
     *
     * @param businessAreaId 业务域Id
     * @param dto            业务域维度建模
     * @return
     */
    List<OlapPO> build(int businessAreaId, BusinessAreaGetDataDTO dto);

    /**
     * 通过表名查指标表id(表名唯一)
     *
     * @param name name
     * @return
     */
    OlapPO selectByName(String name);

    /**
     * 查找该业务域下所有的表
     *
     * @param BusinessAreaId BusinessAreaId
     * @return
     */
    List<OlapPO> selectOlapByBusinessAreaId(String BusinessAreaId);

    /**
     * 查找该业务域下所有的表
     *
     * @param id   id
     * @return
     */
    OlapPO selectOlapPO(int id);

    /**
     * getNifiGetPortHierarchy
     *
     * @param pipelineName   pipelineName
     * @param type   type
     * @param tableName   tableName
     * @param tableAccessId   tableAccessId
     * @return
     */
    NifiGetPortHierarchyDTO getNifiGetPortHierarchy(String pipelineName,int type,String tableName,int tableAccessId);


}
