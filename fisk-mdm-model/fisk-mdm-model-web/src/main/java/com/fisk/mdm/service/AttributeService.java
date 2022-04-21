package com.fisk.mdm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.dto.attribute.AttributeQueryDTO;
import com.fisk.mdm.dto.attribute.AttributeUpdateDTO;
import com.fisk.mdm.dto.model.ModelDTO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.dto.model.ModelUpdateDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.Entity;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.model.ModelVO;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/4/5 14:48
 */
public interface AttributeService extends IService<AttributePO> {
    /**
     * 根据id查询
     * @param id
     * @return
     */
    ResultEntity<AttributeVO> getById(Integer id);

    /**
     * 添加属性
     * @param attributeDTO
     * @return
     */
    ResultEnum addData(AttributeDTO attributeDTO);

    /**
     * 编辑
     * @param attributeUpdateDTO
     * @return
     */
    ResultEnum editData(AttributeUpdateDTO attributeUpdateDTO);

    /**
     * 删除
     * @param id
     * @return ResultEnum
     */
    ResultEnum deleteDataById(Integer id);

    /**
     * 分页查询所有属性
     * @return {@link Page}<{@link AttributeVO}>
     */
    Page<AttributeVO> getAll(AttributeQueryDTO query);


    /**
     * 提交待添加、待修改数据
     *
     * @return {@link List}<{@link AttributePO}>
     */
    ResultEnum getNotSubmittedData();


    /**
     * 获取实体、属性信息
     *
     * @return {@link List}<{@link Entity}>
     */
    List<Entity> getER();

}
