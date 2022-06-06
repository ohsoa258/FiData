package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.entity.EntityQueryDTO;
import com.fisk.mdm.dto.viwGroup.*;
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
    List<ViwGroupVO> getDataByGroupId(Integer id);

    /**
     * 根据实体id查询视图组
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
     * 视图组根据属性id删除
     * @param dto
     * @return
     */
    ResultEnum deleteAttribute(ViwGroupDetailsDTO dto);

    /**
     * 自定义视图组新增属性
     * @param dtoList
     * @return
     */
    ResultEnum addAttribute(ViwGroupDetailsAddDTO dtoList);

    /**
     * 根据实体id查询实体关联关系
     * @param dto
     * @return
     */
    ViwGroupQueryRelationDTO getRelationByEntityId(ViwGroupQueryDTO dto);

    /**
     * 创建自定义视图
     * @param id
     * @return
     */
    ResultEnum createCustomView(Integer id);
}
