package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.label.*;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ILabel {

    /**
     * 添加标签
     * @param dto
     * @return
     */
    ResultEnum addLabel(LabelDTO dto);

    /**
     * 删除标签
     * @param id
     * @return
     */
    ResultEnum delLabel(int id);

    /**
     * 更改标签
     * @param dto
     * @return
     */
    ResultEnum updateLabel(LabelDTO dto);

    /**
     * 根据id获取标签详情
     * @param id
     * @return
     */
    LabelDTO getLabelDetail(int id);

    /**
     * 分页获取
     * @param dto
     * @return
     */
    Page<LabelDataDTO> getLabelPageList(LabelQueryDTO dto);

    /**
     * atlas获取标签列表
     *
     * @return
     */
    List<LabelInfoDTO> getLabelList(String keyword);

    /**
     * 通过catearyid和id获取标签
     * @param dto
     * @return
     */
    List<LabelDataDTO> queryLabelListById(GlobalSearchDto dto);
}
