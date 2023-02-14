package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.lineagemaprelation.LineageMapRelationDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ILineageMapRelation {

    /**
     * 新增process关联关系
     *
     * @param dtoList
     * @return
     */
    ResultEnum addLineageMapRelation(List<LineageMapRelationDTO> dtoList);

}
