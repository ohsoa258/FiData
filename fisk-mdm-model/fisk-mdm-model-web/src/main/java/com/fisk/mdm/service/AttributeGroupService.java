package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDTO;
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDetailsDTO;
import com.fisk.mdm.dto.attributeGroup.UpdateAttributeGroupDTO;
import com.fisk.mdm.vo.attributeGroup.AttributeGroupVO;
import com.fisk.mdm.vo.attributeGroup.QueryAttributeGroupVO;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/23 8:34
 * @Version 1.0
 */
public interface AttributeGroupService {

    /**
     * 根据组id查询属性组
     * @param id
     * @return
     */
    AttributeGroupVO getDataByGroupId(Integer id);

    /**
     * 根据模型id查询属性组
     * @param modelId
     * @return
     */
    List<AttributeGroupVO> getDataByModelId(Integer modelId);

    /**
     * 修改属性组信息
     * @param dto
     * @return
     */
    ResultEnum updateData(UpdateAttributeGroupDTO dto);

    /**
     * 根据id删除属性组
     * @param id
     * @return
     */
    ResultEnum deleteGroupById(Integer id);

    /**
     * 属性组新增属性
     * @param dto
     * @return
     */
    ResultEnum addAttribute(AttributeGroupDetailsDTO dto);

    /**
     * 属性组根据属性id删除
     * @param dto
     * @return
     */
    ResultEnum deleteAttribute(AttributeGroupDetailsDTO dto);

    /**
     * 创建属性组
     * @param dto
     * @return
     */
    ResultEnum addAttributeGroup(AttributeGroupDTO dto);

    /**
     * 根据属性id查询属性组
     * @param attributeId
     * @return
     */
    List<AttributeGroupDTO> getDataByAttributeId(Integer attributeId);

    /**
     * 根据组id查询属性组(根据实体进行分组)
     * @param id
     * @return
     */
    List<QueryAttributeGroupVO> getDataGroupById(Integer id);
}
