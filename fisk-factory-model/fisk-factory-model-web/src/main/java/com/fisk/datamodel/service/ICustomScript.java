package com.fisk.datamodel.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamodel.dto.customscript.CustomScriptDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptInfoDTO;
import com.fisk.datamodel.dto.customscript.CustomScriptQueryDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ICustomScript {

    /**
     * 新增
     *
     * @param dto
     * @return
     */
    ResultEnum addCustomScript(CustomScriptDTO dto);

    /**
     * 编辑
     *
     * @param dto
     * @return
     */
    ResultEnum updateCustomScript(CustomScriptDTO dto);

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    ResultEnum batchDelCustomScript(List<Integer> ids);

    /**
     * 详情
     *
     * @param id
     * @return
     */
    CustomScriptDTO getCustomScript(Integer id);

    /**
     * 根据表id获取数据
     *
     * @param dto
     * @return
     */
    List<CustomScriptInfoDTO> listCustomScript(CustomScriptQueryDTO dto);

}
