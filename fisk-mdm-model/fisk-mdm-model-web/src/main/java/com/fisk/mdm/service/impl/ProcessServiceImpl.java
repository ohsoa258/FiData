package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.masterdata.MasterDataDTO;
import com.fisk.mdm.dto.process.*;
import com.fisk.mdm.entity.*;
import com.fisk.mdm.enums.ApprovalApplyStateEnum;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.ProcessPersonTypeEnum;
import com.fisk.mdm.map.ProcessApplyMap;
import com.fisk.mdm.map.ProcessInfoMap;
import com.fisk.mdm.map.ProcessNodeMap;
import com.fisk.mdm.map.ProcessPersonMap;
import com.fisk.mdm.service.*;
import com.fisk.mdm.vo.process.*;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.system.vo.roleinfo.RoleInfoVo;
import com.fisk.system.vo.roleinfo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wangjian
 * @Date: 2023-03-30
 * @Description: 流程服务
 */
@Slf4j
@Service
public class ProcessServiceImpl implements ProcessService {

    @Resource
    private IProcessInfoService processInfoService;

    @Resource
    private IProcessNodeService processNodeService;

    @Resource
    private IProcessPersonService processPersonService;

    @Resource
    private IProcessApplyService processApplyService;

    @Resource
    private IProcessApplyNotesService processApplyNotesService;
    @Resource
    private UserClient userClient;
    @Resource
    private UserHelper userHelper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum saveProcess(ProcessInfoDTO dto) {

        ProcessInfoPO processInfo = processInfoService.getProcessInfo(dto.getEntityId());
        if (processInfo != null) {
            List<ProcessNodePO> processNodePos = processNodeService.getProcessNodes((int) processInfo.getId());
            //逻辑删除流程信息
            processInfoService.deleteProcessInfo(dto.getEntityId());
            List<Integer> processNodeIds = processNodePos.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
            //逻辑删除流程节点信息
            LambdaQueryWrapper<ProcessNodePO> processNodePoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            processNodePoLambdaQueryWrapper.eq(ProcessNodePO::getDelFlag, 1).eq(ProcessNodePO::getProcessId, processInfo.getId());
            processNodeService.getBaseMapper().delete(processNodePoLambdaQueryWrapper);
            //逻辑删除流程节点角色或用户信息
            LambdaQueryWrapper<ProcessPersonPO> personPoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            personPoLambdaQueryWrapper.eq(ProcessPersonPO::getDelFlag, 1).in(ProcessPersonPO::getRocessNodeId, processNodeIds);
            processPersonService.getBaseMapper().delete(personPoLambdaQueryWrapper);
        }
        //保存流程信息
        ProcessInfoPO processInfoPo = ProcessInfoMap.INSTANCES.dtoToPo(dto);
        boolean save = processInfoService.save(processInfoPo);
        if (!save) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        //保存流程节点信息
        List<ProcessNodeDTO> processNodes = dto.getProcessNodes();
        if (processNodes.size() < 2) {
            throw new FkException(ResultEnum.SAVE_PROCESS_NODE_ERROR);
        }
        List<ProcessNodePO> processNodePos = ProcessNodeMap.INSTANCES.dtoListToPoList(processNodes);
        for (ProcessNodePO processNodePo : processNodePos) {
            processNodePo.setProcessId((int) processInfoPo.getId());
        }
        boolean b = processNodeService.saveBatch(processNodePos);
        if (!b) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        //保存流程节点用户及角色信息
        List<ProcessPersonPO> personList = new ArrayList<>();
        //赋值人员流程节点ID
        for (int i = 0; i < processNodes.size(); i++) {
            ProcessNodePO processNodePo = processNodePos.get(i);
            ProcessNodeDTO processNodeDto = processNodes.get(i);
            List<ProcessPersonDTO> personlist = processNodeDto.getPersonList();
            if (i != 0 && personlist.size() < 1) {
                throw new FkException(ResultEnum.PROCESS_PERSON_NOT_NULL);
            }
            List<ProcessPersonPO> personPos = ProcessPersonMap.INSTANCES
                    .dtoListToPoList(personlist);
            for (ProcessPersonPO personPo : personPos) {
                personPo.setRocessNodeId((int) processNodePo.getId());
                personList.add(personPo);
            }
        }
        if (personList.size() > 0) {
            boolean batch = processPersonService.saveBatch(personList);
            if (!batch) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ProcessInfoVO getProcess(Integer entityId) {
        if (entityId == null) {
            return null;
        }
        //根据实体id获取流程对象
        ProcessInfoPO processInfo = processInfoService.getProcessInfo(entityId);
        if (processInfo == null) {
            return null;
        }
        ProcessInfoVO processInfoVo = ProcessInfoMap.INSTANCES.poToVo(processInfo);
        //根据流程id获取流程节点对象
        List<ProcessNodePO> processNodes = processNodeService.getProcessNodes((int) processInfo.getId());
        if (processNodes == null || processNodes.size() == 0) {
            return processInfoVo;
        }
        List<ProcessNodeVO> processNodeVos = ProcessNodeMap.INSTANCES.poListToVoList(processNodes);
        List<Integer> processNodeIds = processNodes.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
        List<ProcessPersonPO> processPersons = processPersonService.getProcessPersons(processNodeIds);
        if (processPersons == null || processPersons.size() == 0) {
            return processInfoVo;
        }
        List<ProcessPersonVO> processPersonVos = ProcessPersonMap.INSTANCES.poListToVoList(processPersons);
        //获取流程人员中角色id
        List<Integer> personRoleIds = processPersonVos.stream().filter(i -> {
            return ProcessPersonTypeEnum.PERSON_TYPE1_ENUM.getValue() == i.getType();
        }).map(ProcessPersonVO::getUrid).collect(Collectors.toList());
        //获取流程人员中用户id
        List<Long> personUserIds = processPersonVos.stream().filter(i -> {
            return ProcessPersonTypeEnum.PERSON_TYPE2_ENUM.getValue() == i.getType();
        }).map(i -> (long) i.getUrid()).collect(Collectors.toList());
        //远程调用接口获取角色及用户信息
        Map<Integer, String> roleMap = new HashMap<>(personRoleIds.size());
        Map<Integer, String> userMap = new HashMap<>(personUserIds.size());
        ResultEntity<List<RoleInfoDTO>> roles = userClient.getRoles(personRoleIds);
        ResultEntity<List<UserDTO>> users = userClient.getUserListByIds(personUserIds);
        if (roles.code == ResultEnum.SUCCESS.getCode() && roles.getData() != null) {
            roleMap = roles.getData().stream().collect(Collectors.toMap(i -> (int) i.getId(), RoleInfoDTO::getRoleName));
        } else {
            log.error("远程调用失败，错误code: " + roles.getCode() + ",错误信息: " + roles.getMsg());
        }
        if (users.code == ResultEnum.SUCCESS.getCode() && users.getData() != null) {
            userMap = users.getData().stream().collect(Collectors.toMap(i -> i.getId().intValue(), UserDTO::getUsername));
        } else {
            log.error("远程调用失败，错误code: " + users.getCode() + ",错误信息: " + users.getMsg());
        }
        //根据节点分组获取每个节点下面的人员信息
        Map<Integer, List<ProcessPersonVO>> collect = processPersonVos.stream()
                .collect(Collectors.groupingBy(ProcessPersonVO::getRocessNodeId));
        if (collect.size() > 0) {
            for (ProcessNodeVO processNode : processNodeVos) {
                //获取当前节点下的所有人员信息
                List<ProcessPersonVO> processPersonVOList = collect.get(processNode.getId());
                //给当前节点下的人员名称赋值
                if (processPersonVOList == null || processPersonVOList.size() == 0) {
                    continue;
                }
                for (ProcessPersonVO processPersonVO : processPersonVOList) {
                    if (ProcessPersonTypeEnum.PERSON_TYPE1_ENUM.getValue() == processPersonVO.getType()) {
                        processPersonVO.setUrName(roleMap.get(processPersonVO.getUrid()));
                    } else if (ProcessPersonTypeEnum.PERSON_TYPE2_ENUM.getValue() == processPersonVO.getType()) {
                        processPersonVO.setUrName(userMap.get(processPersonVO.getUrid()));
                    }
                }
                processNode.setPersonList(processPersonVOList);
            }
        }
        processInfoVo.setProcessNodes(processNodeVos);
        return processInfoVo;
    }

    @Override
    public boolean verifyProcessApply(Integer entityId) throws FkException {
        int userId = userHelper.getLoginUserInfo().id.intValue();
        ProcessInfoPO processInfo = processInfoService.getProcessInfo(entityId);
        List<ProcessNodePO> processNodes = processNodeService.getProcessNodes((int) processInfo.getId());
        if (processNodes != null && processNodes.size() > 0) {
            //获取申请人流程节点
            ProcessNodePO processNodePo = processNodes.get(0);
            //获取申请人流程节点人员
            List<ProcessPersonPO> personPoList = processPersonService.getProcessPersons((int) processNodePo.getId());
            if (personPoList != null && personPoList.size() > 0) {
                List<Integer> personIds = personPoList.stream()
                        .filter(i -> i.getType().getValue() == ProcessPersonTypeEnum.PERSON_TYPE2_ENUM.getValue())
                        .map(ProcessPersonPO::getUrid).collect(Collectors.toList());
                //校验该用户是否走流程
                boolean uContains = personIds.contains(userId);
                //校验该角色是否走流程
                boolean rContains = false;
                ResultEntity<List<RoleInfoDTO>> rolebyUserId = userClient.getRolebyUserId(userId);
                if (rolebyUserId.code == ResultEnum.SUCCESS.getCode() && rolebyUserId.getData() != null) {
                    List<Integer> roleIds = personPoList.stream()
                            .filter(i -> i.getType().getValue() == ProcessPersonTypeEnum.PERSON_TYPE1_ENUM.getValue())
                            .map(ProcessPersonPO::getUrid).collect(Collectors.toList());
                    List<Integer> roles = rolebyUserId.getData().stream().map(i -> (int) i.getId()).distinct().collect(Collectors.toList());
                    for (Integer role : roles) {
                        rContains = roleIds.contains(role);
                        if (rContains) {
                            break;
                        }
                    }
                } else {
                    log.error("远程调用失败，错误code: " + rolebyUserId.getCode() + ",错误信息: " + rolebyUserId.getMsg());
                    throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
                }
                //判断是否可以走流程
                return uContains || rContains;
            }else {
                return true;
            }
        }
        return false;
    }
    /**
     * 添加工单
     * @param dto
     * @param batchNumber
     * @param eventTypeEnum
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addProcessApply(MasterDataDTO dto, String batchNumber, EventTypeEnum eventTypeEnum) throws FkException {
        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        int userId = loginUserInfo.id.intValue();
        ProcessInfoPO processInfo = processInfoService.getProcessInfo(dto.getEntityId());
        List<ProcessNodePO> processNodes = processNodeService.getProcessNodes((int) processInfo.getId());
        ProcessNodePO processNodePo = processNodes.get(1);
        ProcessApplyPO processApplyPo = new ProcessApplyPO();
        processApplyPo.setApplicant(String.valueOf(userId));
        processApplyPo.setApproverNode((int) processNodePo.getId());
        processApplyPo.setDescription(dto.getDescription());
        processApplyPo.setProcessId((int) processInfo.getId());
        processApplyPo.setState(ApprovalApplyStateEnum.IN_PROGRESS);
        processApplyPo.setOperationType(eventTypeEnum);
        processApplyPo.setOpreationstate(1);
        processApplyPo.setFidataBatchCode(batchNumber);
        processApplyService.save(processApplyPo);
        List<ProcessPersonPO> processPersons = processPersonService.getProcessPersons((int) processNodePo.getId());
        //通知节点用户进行审批
        return sendEmailToProcessNode(loginUserInfo,getUserIds(processPersons));
    }
    /**
     * 获取我的待审核流程
     * @return
     */
    @Override
    public List<ProcessApplyVO> getMyProcessApply() {
        LambdaQueryWrapper<ProcessApplyPO> poLambdaQueryWrapper = new LambdaQueryWrapper<>();
        poLambdaQueryWrapper.eq(ProcessApplyPO::getApplicant, userHelper.getLoginUserInfo().id)
                .eq(ProcessApplyPO::getDelFlag, 1);
        List<ProcessApplyPO> processApplyPos = processApplyService.list(poLambdaQueryWrapper);
        return ProcessApplyMap.INSTANCES.poListToVoList(processApplyPos);
    }

    /**
     * 获取待处理审批列表
     * @param dto
     * @return
     */
    @Override
    public Page<PendingApprovalVO> getPendingApproval(PendingApprovalDTO dto) {
        return processApplyService.getPendingApproval(dto);
    }

    /**
     * 获取已处理审批列表
     * @param dto
     * @return
     */
    @Override
    public Page<PendingApprovalVO> getOverApproval(PendingApprovalDTO dto) {
        return processApplyService.getOverApproval(dto);
    }

    /**
     * 审批
     * @param dto
     * @return
     */
    @Override
    public ResultEnum approval(ApprovalDTO dto) {
        //数据校验
        if (dto.getProcessApplyId() == null) {
            return ResultEnum.PARAMTER_ERROR;
        }
        ProcessApplyPO processApplyPo = processApplyService.getById(dto.getProcessApplyId());
        if (processApplyPo == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        ProcessInfoPO processInfo = processInfoService.getById(processApplyPo.getProcessId());
        if (processInfo == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        List<ProcessNodePO> processNodes = processNodeService.getProcessNodes((int) processInfo.getId());
        if (processNodes == null || processNodes.size() == 0) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        List<Integer> nodeIds = processNodes.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
        List<ProcessPersonPO> processPersons = processPersonService.getProcessPersons(nodeIds);
        if (processPersons == null || processPersons.size() == 0) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        Map<Integer, List<ProcessPersonPO>> processPersonMap =
                processPersons.stream().collect(Collectors.groupingBy(ProcessPersonPO::getRocessNodeId));
        //判断自动审批规则
        switch (processInfo.getAutoapproal()) {
            case ONLY_ONE_RULE:
                //仅首个节点需审批，其余自动同意
                return saveNoteByOnlyOne(dto, processApplyPo, processNodes, processPersonMap);
            case CONTINUOUS_APPROVAL_RULE:
                //仅连续审批时自动同意
                return saveNoteByContinuous(dto, processApplyPo, processNodes, processPersonMap);
            case ALL_APPROVAL_RULE:
                //每个节点都需要审批
                return saveNoteByAll(dto, processApplyPo, processNodes, processPersonMap);
            default:
                return ResultEnum.ERROR;
        }
    }

    public void sendEmail(MailSenderDTO mailSenderDTO) throws FkException {
        //第一步：查询邮件服务器设置
        ResultEntity<EmailServerVO> emailServerById = userClient.getEmailServerById(27);
        if (emailServerById == null || emailServerById.getCode() != ResultEnum.SUCCESS.getCode() ||
                emailServerById.getData() == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        EmailServerVO emailServerVO = emailServerById.getData();
        MailServeiceDTO mailServeiceDTO = new MailServeiceDTO();
        mailServeiceDTO.setOpenAuth(true);
        mailServeiceDTO.setOpenDebug(true);
        mailServeiceDTO.setHost(emailServerVO.getEmailServer());
        mailServeiceDTO.setProtocol(emailServerVO.getEmailServerType().getName());
        mailServeiceDTO.setUser(emailServerVO.getEmailServerAccount());
        mailServeiceDTO.setPassword(emailServerVO.getEmailServerPwd());
        mailServeiceDTO.setPort(emailServerVO.getEmailServerPort());
        mailSenderDTO.setUser(emailServerVO.getEmailServerAccount());
        try {
            //第二步：调用邮件发送方法
            MailSenderUtils.send(mailServeiceDTO, mailSenderDTO);
        } catch (Exception ex) {
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveNoteByOnlyOne(ApprovalDTO dto,
                                        ProcessApplyPO processApplyPo,
                                        List<ProcessNodePO> processNodes,
                                        Map<Integer, List<ProcessPersonPO>> processPersonMap) {

        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        List<ProcessPersonPO> processPersonPos = processPersonMap.get((int) processApplyPo.getApproverNode());
        List<Long> userIds = getUserIds(processPersonPos);
        //校验当前用户是否可以审批
        if (userIds.contains((long)loginUserInfo.id)) {
            //当前用户第一次保存审批节点
            ProcessApplyNotesPO processApplyNotesPo = new ProcessApplyNotesPO();
            processApplyNotesPo.setState(dto.getFlag());
            processApplyNotesPo.setRemark(dto.getDescription());
            processApplyNotesPo.setProcessapplyId(processApplyPo.getProcessId());
            processApplyNotesPo.setProcessnodeId(processApplyPo.getApproverNode());
            processApplyNotesService.save(processApplyNotesPo);
            Map<Integer, ProcessNodePO> processNodeMap = processNodes.stream().collect(Collectors.toMap(i -> (int) i.getId(), i -> i));
            ProcessNodePO processNodePO = processNodes.get(processNodes.size() - 1);
            //审核通过
            if (dto.getFlag() == 1) {
                //判断是否是最后一个节点
                if (processApplyPo.getApproverNode() == processNodePO.getId()) {
                    processApplyPo.setState(ApprovalApplyStateEnum.APPROVE);
                    processApplyService.updateById(processApplyPo);
                    //发送通过邮箱
                    return sendEmailToResult((int) processApplyPo.getId(), ApprovalApplyStateEnum.APPROVE);
                } else {
                    ProcessNodePO processNodePo = processNodeMap.get(processApplyPo.getApproverNode());
                    int i = processNodePo.getLevels() + 1;
                    ProcessNodePO processNodePoNext = processNodes.get(i);
                    //递归处理仅需首次审批
                    return saveOnlyOneApply(processNodePoNext, processApplyPo, loginUserInfo, processNodes, processPersonMap);
                }
            //审核拒绝
            } else if (dto.getFlag() == 2) {
                processApplyPo.setState(ApprovalApplyStateEnum.REFUSED);
                processApplyService.updateById(processApplyPo);
                //发送拒绝邮件通知
                return sendEmailToResult((int) processApplyPo.getId(), ApprovalApplyStateEnum.REFUSED);
            } else {
                return ResultEnum.PARAMTER_ERROR;
            }
        } else {
            return ResultEnum.ACCOUNT_CANNOT_OPERATION_API;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveNoteByContinuous(ApprovalDTO dto,
                                           ProcessApplyPO processApplyPo,
                                           List<ProcessNodePO> processNodes,
                                           Map<Integer, List<ProcessPersonPO>> processPersonMap) {
        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        List<ProcessPersonPO> processPersonPos = processPersonMap.get((int) processApplyPo.getApproverNode());
        List<Long> userIds = getUserIds(processPersonPos);
        //校验当前用户是否支持审批
        if (userIds.contains((long)loginUserInfo.id)) {
            //第一次保存审批节点信息
            ProcessApplyNotesPO processApplyNotesPo = new ProcessApplyNotesPO();
            processApplyNotesPo.setState(dto.getFlag());
            processApplyNotesPo.setRemark(dto.getDescription());
            processApplyNotesPo.setProcessapplyId(processApplyPo.getProcessId());
            processApplyNotesPo.setProcessnodeId(processApplyPo.getApproverNode());
            processApplyNotesService.save(processApplyNotesPo);
            Map<Integer, ProcessNodePO> processNodeMap = processNodes.stream().collect(Collectors.toMap(i -> (int) i.getId(), i -> i));
            //获取最后一个节点
            ProcessNodePO processNodePO = processNodes.get(processNodes.size() - 1);
            //通过
            if (dto.getFlag() == 1) {
                if (processApplyPo.getApproverNode() == processNodePO.getId()) {
                    processApplyPo.setState(ApprovalApplyStateEnum.APPROVE);
                    processApplyService.updateById(processApplyPo);
                    //最后一个节点发送通过邮箱
                    return sendEmailToResult((int) processApplyPo.getId(), ApprovalApplyStateEnum.APPROVE);
                } else {
                    ProcessNodePO processNodePo = processNodeMap.get(processApplyPo.getApproverNode());
                    int i = processNodePo.getLevels() + 1;
                    ProcessNodePO processNodePoNext = processNodes.get(i);
                    try {
                        //递归处理自动同意
                        return saveApply(processNodePoNext, processApplyPo, loginUserInfo, processNodes, processPersonMap);
                    } catch (Exception e) {
                        return ResultEnum.ERROR;
                    }
                }
            //拒绝
            } else if (dto.getFlag() == 2) {
                processApplyPo.setState(ApprovalApplyStateEnum.REFUSED);
                processApplyService.updateById(processApplyPo);
                //发送拒绝邮箱
                return sendEmailToResult((int) processApplyPo.getId(), ApprovalApplyStateEnum.REFUSED);
            } else {
                return ResultEnum.PARAMTER_ERROR;
            }
        } else {
            return ResultEnum.ACCOUNT_CANNOT_OPERATION_API;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveNoteByAll(ApprovalDTO dto,
                                    ProcessApplyPO processApplyPo,
                                    List<ProcessNodePO> processNodes,
                                    Map<Integer, List<ProcessPersonPO>> processPersonMap) {
        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        List<ProcessPersonPO> processPersonPos = processPersonMap.get((int) processApplyPo.getApproverNode());
        List<Long> userIds = getUserIds(processPersonPos);
        //校验是否可以审批
        if (userIds.contains((long)loginUserInfo.id)) {
            //保存
            ProcessApplyNotesPO processApplyNotesPo = new ProcessApplyNotesPO();
            processApplyNotesPo.setState(dto.getFlag());
            processApplyNotesPo.setRemark(dto.getDescription());
            processApplyNotesPo.setProcessapplyId(processApplyPo.getProcessId());
            processApplyNotesPo.setProcessnodeId(processApplyPo.getApproverNode());
            processApplyNotesService.save(processApplyNotesPo);
            Map<Integer, ProcessNodePO> processNodeMap = processNodes.stream().collect(Collectors.toMap(i -> (int) i.getId(), i -> i));
            ProcessNodePO processNodePo = processNodes.get(processNodes.size() - 1);
            if (dto.getFlag() == 1) {
                if (processApplyPo.getApproverNode() == processNodePo.getId()) {
                    processApplyPo.setState(ApprovalApplyStateEnum.APPROVE);
                    processApplyService.updateById(processApplyPo);
                    //所有节点都需要审批无需特殊处理
                    return sendEmailToResult((int) processApplyPo.getId(), ApprovalApplyStateEnum.APPROVE);
                } else {
                    ProcessNodePO processNodePO1 = processNodeMap.get(processApplyPo.getApproverNode());
                    int i = processNodePO1.getLevels() + 1;
                    ProcessNodePO processNodeP02 = processNodes.get(i);
                    processApplyPo.setState(ApprovalApplyStateEnum.IN_PROGRESS);
                    processApplyPo.setApproverNode((int) processNodeP02.getId());
                    processApplyService.updateById(processApplyPo);
                    //发送节点通知邮箱
                    return sendEmailToProcessNode(loginUserInfo,userIds);
                }
            } else if (dto.getFlag() == 2) {
                processApplyPo.setState(ApprovalApplyStateEnum.REFUSED);
                processApplyService.updateById(processApplyPo);
                //发送拒绝邮箱
                return sendEmailToResult((int) processApplyPo.getId(), ApprovalApplyStateEnum.REFUSED);
            } else {
                return ResultEnum.PARAMTER_ERROR;
            }
        } else {
            return ResultEnum.ACCOUNT_CANNOT_OPERATION_API;
        }
    }

    private ResultEnum sendEmailToResult(int processApplyId, ApprovalApplyStateEnum stateEnum) {
        try {
            String emailAddress = null;
            //获取需要通知的人员邮箱
            ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(Arrays.asList((long) userHelper.getLoginUserInfo().id));
            if (userListByIds.code == ResultEnum.SUCCESS.getCode()) {
                List<UserDTO> data = userListByIds.getData();
                emailAddress = data.get(0).getEmail();
            } else {
                log.error("远程调用失败，错误code: " + userListByIds.getCode() + ",错误信息: " + userListByIds.getMsg());
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
            String message = null;
            switch (stateEnum) {
                case APPROVE:
                    message = "你申请的流程" + processApplyId + "已通过";
                    break;
                case REFUSED:
                    message = "你申请的流程" + processApplyId + "被拒绝";
                    break;
                default:
                    return ResultEnum.ERROR;
            }
            MailSenderDTO mailSenderDTO = new MailSenderDTO();
            mailSenderDTO.setSubject("审批流程");
            mailSenderDTO.setBody("<div><span style=\"font-family: &quot;Microsoft Yahei&quot;;\">" +
                    "</span></div><div><span style=\"font-family: &quot;Microsoft Yahei&quot;;\">" +
                    "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;" + message + "</span></div>");
            mailSenderDTO.setToAddress(emailAddress);
            mailSenderDTO.setSendAttachment(false);
            sendEmail(mailSenderDTO);
            return ResultEnum.SUCCESS;
        } catch (FkException e) {
            return ResultEnum.ERROR;
        }
    }

    private ResultEnum sendEmailToProcessNode(UserInfo loginUserInfo,List<Long> userIds) {

        ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(userIds);
        if (userListByIds.code == ResultEnum.SUCCESS.getCode()) {
            List<UserDTO> users = userListByIds.getData();
            String address = users.stream().map(UserDTO::getEmail).collect(Collectors.joining(","));
            try {
                MailSenderDTO mailSenderDTO = new MailSenderDTO();
                mailSenderDTO.setSubject("审批流程");
                mailSenderDTO.setBody("<div><span style=\"font-family: &quot;Microsoft Yahei&quot;;\">" +
                        "</span></div><div><span style=\"font-family: &quot;Microsoft Yahei&quot;;\">" +
                        "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;" + loginUserInfo.getUsername() +
                        "请前往MDS系统中的我的审批页面查看并审批，系统地址：</span></div><div>&nbsp; &nbsp; &nbsp; " +
                        "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;&nbsp;<a href=\"http://192.168.11.130:82/#/masterDataModelingDMP\">" +
                        "http://192.168.11.130:82/#/masterDataModelingDMP</a>");
                mailSenderDTO.setToAddress(address);
                mailSenderDTO.setSendAttachment(false);
                sendEmail(mailSenderDTO);
            } catch (FkException e) {
                throw new FkException(ResultEnum.ERROR, e.getMessage());
            }
        } else {
            log.error("远程调用失败，错误code: " + userListByIds.getCode() + ",错误信息: " + userListByIds.getMsg());
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        return ResultEnum.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveApply(ProcessNodePO processNode,
                                ProcessApplyPO processApplyPo,
                                UserInfo loginUserInfo,
                                List<ProcessNodePO> processNodes,
                                Map<Integer, List<ProcessPersonPO>> processPersonMap) {
        List<ProcessPersonPO> processPersonPos = processPersonMap.get((int) processNode.getId());
        List<Long> userIds = getUserIds(processPersonPos);
        //节点改变重新校验
        if (userIds.contains((long)loginUserInfo.id)) {
            //保存
            ProcessApplyNotesPO processApplyNotesPo = new ProcessApplyNotesPO();
            processApplyNotesPo.setState(1);
            processApplyNotesPo.setRemark("auto");
            processApplyNotesPo.setProcessapplyId(processNode.getProcessId());
            processApplyNotesPo.setProcessnodeId((int) processNode.getId());
            processApplyNotesService.save(processApplyNotesPo);
            //判断是否最后一个节点
            if (processNode.getLevels() + 1 < processNodes.size()) {
                ProcessNodePO processNodePo = processNodes.get(processNode.getLevels() + 1);
                //当前节点支持审批继续递归处理下一个节点
                return saveApply(processNodePo, processApplyPo, loginUserInfo, processNodes, processPersonMap);
            } else {
                processApplyPo.setApproverNode((int) processNode.getId());
                processApplyPo.setState(ApprovalApplyStateEnum.APPROVE);
                processApplyService.updateById(processApplyPo);
                //最后一个节点发送同意通知
                return sendEmailToResult((int) processApplyPo.getId(), ApprovalApplyStateEnum.APPROVE);
            }
        } else {
            if (processNode.getLevels() + 1 < processNodes.size()) {
                ProcessNodePO processNodePo = processNodes.get(processNode.getLevels() + 1);
                processApplyPo.setState(ApprovalApplyStateEnum.IN_PROGRESS);
                processApplyPo.setApproverNode((int) processNodePo.getId());
                processApplyService.updateById(processApplyPo);
                //当前用户不能审批则发送通知节点用户邮箱
                return sendEmailToProcessNode(loginUserInfo, userIds);
            }else {
                processApplyPo.setState(ApprovalApplyStateEnum.APPROVE);
                processApplyPo.setApproverNode((int)processNode.getId());
                processApplyService.updateById(processApplyPo);
                return sendEmailToResult((int) processApplyPo.getId(), ApprovalApplyStateEnum.APPROVE);
            }
        }
    }

    /**
     * 只有首个节点需审批
     * @param processNode
     * @param processApplyPo
     * @param loginUserInfo
     * @param processNodes
     * @param processPersonMap
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveOnlyOneApply(ProcessNodePO processNode,
                                       ProcessApplyPO processApplyPo,
                                       UserInfo loginUserInfo,
                                       List<ProcessNodePO> processNodes,
                                       Map<Integer, List<ProcessPersonPO>> processPersonMap) {
        boolean flag = false;
        LambdaQueryWrapper<ProcessApplyNotesPO> poLambdaQueryWrapper = new LambdaQueryWrapper<>();
        poLambdaQueryWrapper.eq(ProcessApplyNotesPO::getProcessapplyId, processApplyPo.getId());
        List<ProcessApplyNotesPO> list = processApplyNotesService.list(poLambdaQueryWrapper);
        List<Integer> applyNoteUser = list.stream().map(i -> Integer.valueOf(i.getCreateUser())).collect(Collectors.toList());
        List<ProcessPersonPO> processPersonPos = processPersonMap.get(processNode.getLevels());
        //因为节点换了所以需要重新校验是否在审批人员内
        List<Long> userIds = getUserIds(processPersonPos);
        for (Long userId : userIds) {
            //判断当前节点内审核人员之前是否审核过
            flag = applyNoteUser.contains(userId.intValue());
            break;
        }
        if (flag) {
            //支持审批
            ProcessApplyNotesPO processApplyNotesPo = new ProcessApplyNotesPO();
            processApplyNotesPo.setState(1);
            processApplyNotesPo.setRemark("auto");
            processApplyNotesPo.setProcessapplyId(processNode.getProcessId());
            processApplyNotesPo.setProcessnodeId((int) processNode.getId());
            processApplyNotesService.save(processApplyNotesPo);
            //是否最后一个节点
            if (processNode.getLevels() + 1 < processNodes.size()) {
                ProcessNodePO processNodePo = processNodes.get(processNode.getLevels() + 1);
                //递归
                return saveOnlyOneApply(processNodePo, processApplyPo, loginUserInfo, processNodes, processPersonMap);
            } else {
                //最后一个节点发送通过通知
                processApplyPo.setApproverNode((int) processNode.getId());
                processApplyPo.setState(ApprovalApplyStateEnum.APPROVE);
                processApplyService.updateById(processApplyPo);
                return sendEmailToResult((int) processApplyPo.getId(), ApprovalApplyStateEnum.APPROVE);
            }
        } else {
            //当前节点无需自动审批通知当前节点人员进行审批
            processApplyPo.setApproverNode((int) processNode.getId());
            processApplyPo.setState(ApprovalApplyStateEnum.IN_PROGRESS);
            processApplyService.updateById(processApplyPo);
            return sendEmailToProcessNode(loginUserInfo, userIds);
        }
    }

    /**
     * 获取审批流程节点下所有成员
     * @param processPersons
     * @return
     * @throws FkException
     */
    public List<Long> getUserIds(List<ProcessPersonPO> processPersons) throws FkException{
        //获取节点下角色Id
        List<Integer> roleIds = processPersons.stream()
                .filter(i -> i.getType().getValue() == ProcessPersonTypeEnum.PERSON_TYPE1_ENUM.getValue())
                .map(ProcessPersonPO::getUrid).collect(Collectors.toList());
        //获取节点下用户Id
        List<Long> userIds = processPersons.stream()
                .filter(i -> i.getType().getValue() == ProcessPersonTypeEnum.PERSON_TYPE2_ENUM.getValue())
                .map(i -> (long) i.getUrid()).collect(Collectors.toList());
        //获取所有角色下成员
        ResultEntity<List<RoleInfoVo>> treeRols = userClient.getTreeRols();
        List<RoleInfoVo> data = treeRols.getData();
        if (treeRols.code == ResultEnum.SUCCESS.getCode() && data != null) {
            if (roleIds.size() != 0) {
                List<RoleInfoVo> rleInfos = data.stream().filter(i -> roleIds.contains(i.getId())).collect(Collectors.toList());
                for (RoleInfoVo rleInfo : rleInfos) {
                    List<UserInfoVo> userInfoVos = rleInfo.getUserInfoVos();
                    for (UserInfoVo userInfoVo : userInfoVos) {
                        userIds.add((long) userInfoVo.getId());
                    }
                }
                return userIds;
            }
            return userIds;
        } else {
            log.error("远程调用失败，错误code: " + treeRols.getCode() + ",错误信息: " + treeRols.getMsg());
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
    }
}
