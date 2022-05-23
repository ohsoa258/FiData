package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attributeGroup.UpdateAttributeGroupDTO;
import com.fisk.mdm.vo.attributeGroup.AttributeGroupVO;

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
     * 修改属性组
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
}
