package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.viwGroup.UpdateViwGroupDTO;
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
    ResultEnum updateData(UpdateViwGroupDTO dto);

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
}
