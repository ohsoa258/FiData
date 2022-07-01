package com.fisk.mdm.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.codeRule.CodeRuleAddDTO;
import com.fisk.mdm.dto.codeRule.CodeRuleDTO;
import com.fisk.mdm.dto.codeRule.CodeRuleGroupDTO;
import com.fisk.mdm.dto.codeRule.CodeRuleGroupUpdateDTO;
import com.fisk.mdm.vo.codeRule.CodeRuleVO;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/6/23 11:19
 * @Version 1.0
 */
public interface CodeRuleService {

    /**
     * 创建编码规则组
     * @param dto
     * @return
     */
    ResultEnum addRuleGroup(CodeRuleGroupDTO dto);

    /**
     * 修改编码规则组
     * @param dto
     * @return
     */
    ResultEnum updateData(CodeRuleGroupUpdateDTO dto);

    /**
     * 根据id删除编码规则组
     * @param id
     * @return
     */
    ResultEnum deleteGroupById(Integer id);

    /**
     * 编码规则组删除规则(根据规则id)
     * @param dto
     * @return
     */
    ResultEnum deleteCodeRuleById(CodeRuleDTO dto);

    /**
     * 编码规则组新增规则
     * @param dto
     * @return
     */
    ResultEnum addCodeRule(CodeRuleAddDTO dto);

    /**
     * 根据编码规则组id查询
     * @param id
     * @return
     */
    List<CodeRuleVO> getDataByGroupId(Integer id);

    /**
     * 根据实体id查询编码规则组
     * @param entityId
     * @return
     */
    List<CodeRuleVO> getDataByEntityId(Integer entityId);
}
