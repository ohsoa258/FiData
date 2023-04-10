package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.user.UserHelper;
import com.fisk.mdm.dto.process.PendingApprovalDTO;
import com.fisk.mdm.entity.ProcessApplyPO;
import com.fisk.mdm.mapper.ProcessApplyMapper;
import com.fisk.mdm.service.IProcessApplyService;
import com.fisk.mdm.vo.process.PendingApprovalVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: wangjian
 * @Date: 2023-04-04
 */
@Slf4j
@Service
public class ProcessApplyServiceImpl extends ServiceImpl<ProcessApplyMapper, ProcessApplyPO> implements IProcessApplyService {
    @Resource
    UserHelper userHelper;
    @Override
    public Page<PendingApprovalVO> getPendingApproval(PendingApprovalDTO dto) {
        return baseMapper.getPendingApproval(dto.getPage(),userHelper.getLoginUserInfo().id,dto);
    }

    @Override
    public Page<PendingApprovalVO> getOverApproval(PendingApprovalDTO dto) {
        return baseMapper.getOverApproval(dto.getPage(),userHelper.getLoginUserInfo().id,dto);
    }
}
