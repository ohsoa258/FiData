package com.fisk.datagovern.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovern.dto.label.LabelDTO;
import com.fisk.datagovern.dto.label.LabelDataDTO;
import com.fisk.datagovern.dto.label.LabelQueryDTO;

import javax.annotation.Resource;
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

}
