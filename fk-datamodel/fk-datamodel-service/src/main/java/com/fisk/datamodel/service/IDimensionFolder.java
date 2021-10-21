package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.dimension.DimensionListDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderDataDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderPublishDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IDimensionFolder {

    /**
     * 添加维度文件夹
     * @param dto
     * @return
     */
    ResultEnum addDimensionFolder(DimensionFolderDTO dto);

    /**
     * 删除维度文件夹
     * @param ids
     * @return
     */
    ResultEnum delDimensionFolder(List<Integer> ids);

    /**
     * 获取维度文件夹详情
     * @param id
     * @return
     */
    DimensionFolderDTO getDimensionFolder(int id);

    /**
     * 更改维度文件夹详情
     * @param dto
     * @return
     */
    ResultEnum updateDimensionFolder(DimensionFolderDTO dto);

    /**
     * 根据业务域id,获取维度文件夹下相关维度信息
     * @param businessAreaId
     * @return
     */
    List<DimensionFolderDataDTO> getDimensionFolderList(int businessAreaId);

    /**
     * 批量发布维度文件夹
     * @param dto
     * @return
     */
    ResultEnum batchPublishDimensionFolder(DimensionFolderPublishDTO dto);

}
