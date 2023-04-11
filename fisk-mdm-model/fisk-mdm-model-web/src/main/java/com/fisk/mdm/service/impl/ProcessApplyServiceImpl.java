package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.process.PendingApprovalDTO;
import com.fisk.mdm.entity.ProcessApplyPO;
import com.fisk.mdm.mapper.ProcessApplyMapper;
import com.fisk.mdm.service.IProcessApplyService;
import com.fisk.mdm.vo.process.PendingApprovalVO;
import com.fisk.mdm.vo.process.ProcessApplyVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: wangjian
 * @Date: 2023-04-04
 */
@Slf4j
@Service
public class ProcessApplyServiceImpl extends ServiceImpl<ProcessApplyMapper, ProcessApplyPO> implements IProcessApplyService {
    @Resource
    UserHelper userHelper;
    @Resource
    UserClient userClient;

    @Override
    public Page<ProcessApplyVO> getMyProcessApply(PendingApprovalDTO dto) {
        return baseMapper.getMyProcessApply(dto.getPage(),userHelper.getLoginUserInfo().id,dto);
    }

    @Override
    public Page<PendingApprovalVO> getPendingApproval(PendingApprovalDTO dto) {
        Long id = userHelper.getLoginUserInfo().id;
        ResultEntity<List<RoleInfoDTO>> res = userClient.getRolebyUserId(id.intValue());
        if (res.code == ResultEnum.SUCCESS.getCode()) {
            List<Long> roleIds = res.getData().stream().map(i -> i.getId()).collect(Collectors.toList());
            return baseMapper.getPendingApproval(dto.getPage(),id,roleIds,dto);
        } else {
            log.error("远程调用失败，错误code: " + res.getCode() + ",错误信息: " + res.getMsg());
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
    }

    @Override
    public Page<PendingApprovalVO> getOverApproval(PendingApprovalDTO dto) {
        return baseMapper.getOverApproval(dto.getPage(),userHelper.getLoginUserInfo().id,dto);
    }
}
