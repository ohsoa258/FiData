package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.entity.EntityQueryDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupQueryDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupUpdateDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupDetailsDTO;
import com.fisk.mdm.vo.viwGroup.ViwGroupVO;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:27
 * @Version 1.0
 */
public interface ViwGroupService {

    /**
     * 根据视图组id查询
     * @param id
     * @return
     */
    ViwGroupVO getDataByGroupId(Integer id);

    /**
     * 根据实体id查询
     * @param entityId
     * @return
     */
    List<ViwGroupVO> getDataByEntityId(Integer entityId);

    /**
     * 创建自定义视图组
     * @param dto
     * @return
     */
    ResultEnum addViwGroup(ViwGroupDTO dto);

    /**
     * 修改自定义视图组
     * @param dto
     * @return
     */
    ResultEnum updateData(ViwGroupUpdateDTO dto);

    /**
     * 根据id删除自定义视图组
     * @param id
     * @return
     */
    ResultEnum deleteGroupById(Integer id);

    /**
     * 自定义视图组新增属性
     * @param dtoList
     * @return
     */
    ResultEnum addAttribute(ViwGroupDetailsDTO dtoList);

    /**
     * 根据实体id查询实体关联关系
     * @param dto
     * @return
     */
    EntityQueryDTO getRelationByEntityId(ViwGroupQueryDTO dto);
}
