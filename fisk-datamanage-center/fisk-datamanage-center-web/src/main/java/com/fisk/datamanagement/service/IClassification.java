package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.datamanagement.dto.businessclassification.BusinessClassificationTreeDTO;
import com.fisk.datamanagement.dto.classification.*;
import com.fisk.datamanagement.vo.AttributeTypeVO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IClassification {

    /**
     * 获取业务分类列表
     *
     * @return
     */
    ClassificationDefsDTO getClassificationList();

    /**
     * 获取业务分类属性结构
     *
     * @return
     */
    List<BusinessClassificationTreeDTO> getClassificationTree();

    /**
     * 更改业务分类
     *
     * @param dto
     * @return
     */
    ResultEnum updateClassification(ClassificationDefsDTO dto);

    /**
     * 删除业务分类
     * @param classificationName
     * @return
     */
    ResultEnum deleteClassification(String classificationName);

    /**
     * 添加业务分类
     * @param dto
     * @return
     */
    ResultEnum addClassification(ClassificationDefsDTO dto);

    /**
     * 业务分类添加关联实体
     * @param dto
     * @return
     */
    ResultEnum classificationAddAssociatedEntity(ClassificationAddEntityDTO dto);

    /**
     * 业务分类删除关联实体
     *
     * @param dto
     * @return
     */
    ResultEnum classificationDelAssociatedEntity(ClassificationDelAssociatedEntityDTO dto);

    /**
     * 同步业务分类
     *
     * @return
     */
    ResultEnum synchronousClassification();

    /**
     * 数据接入应用同步到业务分类
     *
     * @param dto
     * @return
     */
    ResultEnum appSynchronousClassification(ClassificationInfoDTO dto);

    /**
     * 删除业务分类关联实体
     *
     * @param classification
     */
    ResultEnum delClassificationEntity(String classification);

    /**
     * 新增业务分类属性
     * @param dto
     * @return
     */
    ResultEnum addClassificationAttribute(ClassificationAttributeDTO dto);

    List<AttributeTypeVO> getClassificationAttributeList(String guid);

    ResultEnum delClassificationAttribute(Integer id);
}
