package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.entity.EntityDTO;
import com.fisk.mdm.dto.entity.EntityPageDTO;
import com.fisk.mdm.dto.entity.UpdateEntityDTO;
import com.fisk.mdm.vo.entity.EntityInfoVO;
import com.fisk.mdm.vo.entity.EntityVO;

import java.util.List;

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
    EntityVO getDataById(Integer id);

    /**
     * 分页查询实体
     * @param dto
     * @return
     */
    Page<EntityVO> listData(EntityPageDTO dto);

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

    /**
     * 根据实体id查询属性
     *
     * @param id
     * @return
     */
    EntityInfoVO getAttributeById(Integer id, String name);

    /**
     *根据实体id查询发布成功属性数据
     * @param id
     * @return
     */
    EntityInfoVO getFilterAttributeById(Integer id);

    /**
     * 获取可关联（同模型下 除本身外 创建后台表成功）的实体
     *
     * @return {@link List}<{@link EntityVO}>
     */
    ResultEntity<List<EntityVO>> getCreateSuccessEntity(Integer modelId, Integer entityId);
}
