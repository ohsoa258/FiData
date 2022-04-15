package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.entity.EntityDTO;
import com.fisk.mdm.dto.entity.EntityPageDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.entity.EntityPO;

/**
 * @author WangYan
 * @date 2022/4/2 17:49
 */
public interface EntityService {

    /**
     * 根据id获取实体
     * @param id
     * @return
     */
    EntityDTO getDataById(Integer id);

    /**
     * 分页查询实体
     * @param dto
     * @return
     */
    Page<EntityDTO> listData(EntityPageDTO dto);

    /**
     * 修改实体
     * @param dto
     * @return
     */
    ResultEnum updateData(UpdateEntityDTO dto);

    /**
     * 删除实体
     * @param id
     * @return
     */
    ResultEnum deleteData(Integer id);

    /**
     * 创建实体
     * @param dto
     * @return
     */
    ResultEnum saveEntity(EntityDTO dto);
}
