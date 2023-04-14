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
import com.fisk.mdm.vo.process.EndingApprovalVO;
import com.fisk.mdm.vo.process.PendingApprovalVO;
import com.fisk.mdm.vo.process.ProcessApplyVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        List<Integer> queryUserId = new ArrayList<>();
        if (!StringUtils.isEmpty(dto.getKeyword())){
            ResultEntity<List<Integer>> userIdByUserName = userClient.getUserIdByUserName(dto.getKeyword());
            if (userIdByUserName.code == ResultEnum.SUCCESS.getCode()) {
                queryUserId = userIdByUserName.getData();
            }else {
                log.error("远程调用失败，错误code: " + userIdByUserName.getCode() + ",错误信息: " + userIdByUserName.getMsg());
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
        }
        if (res.code == ResultEnum.SUCCESS.getCode()) {
            List<Long> roleIds = res.getData().stream().map(RoleInfoDTO::getId).collect(Collectors.toList());
            Page<PendingApprovalVO> pendingApproval = baseMapper.getPendingApproval(dto.getPage(),id,queryUserId,roleIds,dto);
            List<PendingApprovalVO> pendingApprovalVOList = pendingApproval.getRecords();
            if (CollectionUtils.isEmpty(pendingApprovalVOList)){
                return pendingApproval;
            }
            List<Long> userId = pendingApprovalVOList.stream().map(i->Long.parseLong(i.getApplicant())).collect(Collectors.toList());
            ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userId);
            if (userListByIds.code == ResultEnum.SUCCESS.getCode()) {
                List<UserDTO> data = userListByIds.getData();
                Map<Long, UserDTO> userMap = data.stream().collect(Collectors.toMap(UserDTO::getId, i -> i));
                List<PendingApprovalVO> list = pendingApprovalVOList.stream().map(i -> {
                    UserDTO userDTO = userMap.get(Long.parseLong(i.getApplicant()));
                    if (userDTO != null) {
                        i.setApplicantName(userDTO.getUsername());
                    }
                    return i;
                }).collect(Collectors.toList());
                pendingApproval.setRecords(list);
                return pendingApproval;
            } else {
                log.error("远程调用失败，错误code: " + res.getCode() + ",错误信息: " + res.getMsg());
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
        } else {
            log.error("远程调用失败，错误code: " + res.getCode() + ",错误信息: " + res.getMsg());
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
    }

    @Override
    public Page<EndingApprovalVO> getOverApproval(PendingApprovalDTO dto) {
        Long id = userHelper.getLoginUserInfo().id;
        List<Integer> queryUserId = new ArrayList<>();
        if (!StringUtils.isEmpty(dto.getKeyword())) {
            ResultEntity<List<Integer>> userIdByUserName = userClient.getUserIdByUserName(dto.getKeyword());
            if (userIdByUserName.code == ResultEnum.SUCCESS.getCode()) {
                queryUserId = userIdByUserName.getData();
            } else {
                log.error("远程调用失败，错误code: " + userIdByUserName.getCode() + ",错误信息: " + userIdByUserName.getMsg());
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
        }
        Page<EndingApprovalVO> overApproval = baseMapper.getOverApproval(dto.getPage(), id, queryUserId, dto);
        List<EndingApprovalVO> endingApprovalVOList = overApproval.getRecords();
        if (CollectionUtils.isEmpty(endingApprovalVOList)) {
            return overApproval;
        }
        List<Long> userId = endingApprovalVOList.stream().map(i -> Long.parseLong(i.getApplicant())).collect(Collectors.toList());
        ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userId);
        if (userListByIds.code == ResultEnum.SUCCESS.getCode()) {
            List<UserDTO> data = userListByIds.getData();
            Map<Long, UserDTO> userMap = data.stream().collect(Collectors.toMap(UserDTO::getId, i -> i));
            List<EndingApprovalVO> list = endingApprovalVOList.stream().map(i -> {
                UserDTO userDTO = userMap.get(Long.parseLong(i.getApplicant()));
                if (userDTO != null) {
                    i.setApplicantName(userDTO.getUsername());
                }
                return i;
            }).collect(Collectors.toList());
            overApproval.setRecords(list);
            return overApproval;
        } else {
            log.error("远程调用失败，错误code: " + userListByIds.getCode() + ",错误信息: " + userListByIds.getMsg());
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
    }
}
