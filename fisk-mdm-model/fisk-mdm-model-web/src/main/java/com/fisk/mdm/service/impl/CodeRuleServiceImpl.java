package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.codeRule.CodeRuleAddDTO;
import com.fisk.mdm.dto.codeRule.CodeRuleDTO;
import com.fisk.mdm.dto.codeRule.CodeRuleGroupDTO;
import com.fisk.mdm.dto.codeRule.CodeRuleGroupUpdateDTO;
import com.fisk.mdm.entity.CodeRuleGroupPO;
import com.fisk.mdm.entity.CodeRulePO;
import com.fisk.mdm.map.CodeRuleMap;
import com.fisk.mdm.mapper.CodeRuleGroupMapper;
import com.fisk.mdm.mapper.CodeRuleMapper;
import com.fisk.mdm.service.CodeRuleService;
import com.fisk.mdm.vo.codeRule.CodeRuleVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author WangYan
 * @Date 2022/6/23 11:20
 * @Version 1.0
 */
@Service
public class CodeRuleServiceImpl implements CodeRuleService {

    @Resource
    CodeRuleGroupMapper groupMapper;
    @Resource
    CodeRuleMapper codeRuleMapper;
    @Resource
    CodeRuleService codeRuleService;
    @Resource
    UserClient userClient;

    @Override
    public ResultEnum addRuleGroup(CodeRuleGroupDTO dto) {
        QueryWrapper<CodeRuleGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(CodeRuleGroupPO::getEntityId,dto.getEntityId())
                .eq(CodeRuleGroupPO::getName,dto.getName())
                .last("limit 1");
        CodeRuleGroupPO groupPo = groupMapper.selectOne(queryWrapper);
        if (groupPo != null){
            return ResultEnum.DATA_EXISTS;
        }

        CodeRuleGroupPO codeRuleGroupPo = CodeRuleMap.INSTANCES.groupDtoToPo(dto);
        int res = groupMapper.insert(codeRuleGroupPo);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum updateData(CodeRuleGroupUpdateDTO dto) {
        CodeRuleGroupPO codeRuleGroupPo = CodeRuleMap.INSTANCES.groupUpdateDtoToPo(dto);
        int res = groupMapper.updateById(codeRuleGroupPo);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum deleteGroupById(Integer id) {
        CodeRuleGroupPO codeRuleGroupPo = groupMapper.selectById(id);
        if (codeRuleGroupPo == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        int res = groupMapper.deleteById(id);
        if (res <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 删除组下的数据
        QueryWrapper<CodeRulePO> queryWrapper = new QueryWrapper();
        queryWrapper.lambda()
                .eq(CodeRulePO::getGroupId,id);
        codeRuleMapper.delete(queryWrapper);

        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum deleteCodeRuleById(CodeRuleDTO dto) {
        boolean existCodeRule = this.isExistCodeRule(dto.getGroupId());
        if (existCodeRule == false){
            return ResultEnum.DATA_NOTEXISTS;
        }

        QueryWrapper<CodeRulePO> queryWrapper = new QueryWrapper();
        queryWrapper.lambda()
                .eq(CodeRulePO::getGroupId,dto.getGroupId())
                .eq(CodeRulePO::getRuleType,dto.getRuleType());
        int res = codeRuleMapper.delete(queryWrapper);
        return res > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum addCodeRule(CodeRuleAddDTO dto) {

        // 删除编码规则组下的数据
        QueryWrapper<CodeRulePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(CodeRulePO::getGroupId,dto.getGroupId());
        codeRuleMapper.delete(queryWrapper);

        // 新增编码规则组数据
        dto.getCodeRuleDtoList().stream().filter(Objects::nonNull)
                .forEach(e -> {
                    CodeRulePO codeRulePo = CodeRuleMap.INSTANCES.dtoToPo(e);
                    int res = codeRuleMapper.insert(codeRulePo);
                    if (res <= 0){
                        throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                    }
                });

        return ResultEnum.SUCCESS;
    }

    @Override
    public List<CodeRuleVO> getDataByGroupId(Integer id) {
        CodeRuleGroupPO codeRuleGroupPo = groupMapper.selectById(id);
        if (codeRuleGroupPo == null){
            return null;
        }

        List<CodeRuleVO> list = new ArrayList<>();
        CodeRuleVO codeRuleVo = CodeRuleMap.INSTANCES.groupPoToVo(codeRuleGroupPo);

        // 查询编码规则组
        QueryWrapper<CodeRulePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(CodeRulePO::getGroupId,id);
        List<CodeRulePO> detailsPoList = codeRuleMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(detailsPoList)){
            List<CodeRuleDTO> collect = detailsPoList.stream().map(e -> {
                CodeRuleDTO dto = CodeRuleMap.INSTANCES.detailsPoToDto(e);
                return dto;
            }).collect(Collectors.toList());

            codeRuleVo.setGroupDetailsList(collect);
        }

        list.add(codeRuleVo);

        // 获取创建人、修改人
        ReplenishUserInfo.replenishUserName(list, userClient, UserFieldEnum.USER_ACCOUNT);
        return list;
    }

    @Override
    public List<CodeRuleVO> getDataByEntityId(Integer entityId) {
        if (entityId == null){
            return null;
        }

        QueryWrapper<CodeRuleGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(CodeRuleGroupPO::getEntityId,entityId);
        List<CodeRuleGroupPO> groupPoList = groupMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(groupPoList)){
            List<CodeRuleVO> collect = groupPoList.stream().filter(e -> e.getId() != 0).map(e -> {
                CodeRuleVO codeRuleVo = codeRuleService.getDataByGroupId((int) e.getId()).get(0);
                return codeRuleVo;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    /**
     * 判断自定义视图组是否存在
     * @param id
     * @return
     */
    public boolean isExistCodeRule(Integer id) {
        CodeRuleGroupPO codeRuleGroupPo = groupMapper.selectById(id);
        if (codeRuleGroupPo == null) {
            return false;
        }

        return true;
    }
}
