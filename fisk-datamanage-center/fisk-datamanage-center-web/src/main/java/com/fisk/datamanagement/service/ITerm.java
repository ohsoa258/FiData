package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.term.TermAssignedEntities;
import com.fisk.datamanagement.dto.term.TermDTO;

/**
 * @author JianWenYang
 */
public interface ITerm {

    /**
     * 添加术语
     * @param dto
     * @return
     */
    ResultEnum addTerm(TermDTO dto);

    /**
     * 获取术语详情
     * @param guid
     * @return
     */
    TermDTO getTerm(String guid);

    /**
     * 修改术语
     * @param dto
     * @return
     */
    ResultEnum updateTerm(TermDTO dto);

    /**
     * 删除术语
     * @param guid
     * @return
     */
    ResultEnum deleteTerm(String guid);

    /**
     * 术语关联entity
     * @param dto
     * @return
     */
    ResultEnum termAssignedEntities(TermAssignedEntities dto);

    /**
     * 术语删除关联entity
     * @param dto
     * @return
     */
    ResultEnum termDeleteAssignedEntities(TermAssignedEntities dto);

}
