package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimension.DimensionDateAttributeDTO;
import com.fisk.datamodel.dto.dimension.DimensionQueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionMetaDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.entity.DimensionPO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDimension extends IService<DimensionPO> {

    /**
     * 添加维度表
     * @param dto
     * @return
     */
    ResultEnum addDimension(DimensionDTO dto);

    /**
     * 获取维度表详情
     * @param id
     * @return
     */
    DimensionDTO getDimension(int id);

    /**
     * 修改维度表
     * @param dto
     * @return
     */
    ResultEnum updateDimension(DimensionDTO dto);

    /**
     * 删除维度表
     * @param id
     * @return
     */
    ResultEnum deleteDimension(int id);

    /**
     * 更新维度脚本数据
     * @param dto
     * @return
     */
    ResultEnum updateDimensionSql(DimensionSqlDTO dto);

    /**
     * 根据筛选条件获取维度名称列表
     * @param dto
     * @return
     */
    List<DimensionMetaDTO>getDimensionNameList(DimensionQueryDTO dto);

    /**
     * 设置维度日期属性
     * @param dto
     * @return
     */
    ResultEnum updateDimensionDateAttribute(DimensionDateAttributeDTO dto);

    /**
     * 根据业务域id,获取时间维度表以及字段
     * @param businessId
     * @return
     */
    DimensionDateAttributeDTO getDimensionDateAttribute(int businessId);

    /**
     * 根据维度id,更改发布状态
     * @param dto
     */
    void updateDimensionPublishStatus(ModelPublishStatusDTO dto);

}
