package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.*;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimension.DimensionListDTO;
import com.fisk.datamodel.dto.dimension.DimensionQueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAssociationDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionMetaDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionSourceDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDimension {

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

}
