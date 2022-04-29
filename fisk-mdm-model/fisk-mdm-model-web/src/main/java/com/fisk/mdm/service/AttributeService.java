package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attribute.*;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityMsgVO;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/4/5 14:48
 */
public interface AttributeService extends IService<AttributePO> {

    /**
     * 根据id查询
     *
     * @param id 属性id
     * @return {@link ResultEntity}<{@link AttributeVO}>
     */
    ResultEntity<AttributeVO> getById(Integer id);


    /**
     * 添加属性
     *
     * @param attributeDTO 属性dto
     * @return {@link ResultEnum}
     */
    ResultEnum addData(AttributeDTO attributeDTO);


    /**
     * 编辑属性
     *
     * @param attributeUpdateDTO 属性更新dto
     * @return ResultEnum
     */
    ResultEnum editData(AttributeUpdateDTO attributeUpdateDTO);

    /**
     * 分页查询所有属性
     * @param query 属性分页查询条件
     * @return {@link Page}<{@link AttributeVO}>
     */
    Page<AttributeVO> getAll(AttributeQueryDTO query);


    /**
     * 提交待添加、待修改数据
     * @param entityId 实体id
     * @return {@link List}<{@link AttributePO}>
     */
    ResultEntity<ResultEnum> getNotSubmittedData(Integer entityId);


    /**
     * 获取实体、属性信息
     * @return {@link List}<{@link EntityMsgVO}>
     */
    List<EntityMsgVO> getEntityMsg();

    /**
     * 根据ids 查询属性信息
     * @param ids
     * @return
     */
    ResultEntity<List<AttributeInfoDTO>> getByIds(List<Integer> ids);

    /**
     * 根据domainId查询数据
     * @param dto
     * @return
     */
    AttributeInfoDTO getByDomainId(AttributeDomainDTO dto);

    /**
     * 修改属性状态
     * @param statusDto
     * @return
     */
    ResultEnum updateStatus(AttributeStatusDTO statusDto);

    /**
     * 删除属性
     *
     * @param id 属性id
     * @return {@link ResultEnum}
     */
    ResultEnum deleteData(Integer id);

    /**
     * 删除属性(逻辑删除)
     *
     * @param id id
     * @return {@link ResultEnum}
     */
    ResultEnum deleteDataById(Integer id);

}
