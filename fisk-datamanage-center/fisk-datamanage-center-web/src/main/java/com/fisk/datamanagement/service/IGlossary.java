package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.glossary.GlossaryAttributeDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IGlossary {

    /**
     * 获取术语库列表,包含术语库下术语、类别
     * @return
     */
    List<GlossaryAttributeDTO> getGlossaryList();

    /**
     * 添加术语库
     * @param dto
     * @return
     */
    ResultEnum addGlossary(GlossaryDTO dto);

    /**
     * 删除术语库
     * @param guid
     * @return
     */
    ResultEnum deleteGlossary(String guid);

    /**
     * 修改术语库
     * @param dto
     * @return
     */
    ResultEnum updateGlossary(GlossaryDTO dto);

}
