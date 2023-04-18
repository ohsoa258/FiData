package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.email.dto.MailSenderDTO;
import com.fisk.common.core.utils.email.dto.MailServeiceDTO;
import com.fisk.common.core.utils.email.method.MailSenderUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.BuildFactoryHelper;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.mdm.dto.process.*;
import com.fisk.mdm.entity.*;
import com.fisk.mdm.enums.ApprovalApplyStateEnum;
import com.fisk.mdm.enums.ApprovalNodeStateEnum;
import com.fisk.mdm.enums.EventTypeEnum;
import com.fisk.mdm.enums.ProcessPersonTypeEnum;
import com.fisk.mdm.map.ProcessInfoMap;
import com.fisk.mdm.map.ProcessNodeMap;
import com.fisk.mdm.map.ProcessPersonMap;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.service.*;
import com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils;
import com.fisk.mdm.utlis.DataSynchronizationUtils;
import com.fisk.mdm.utlis.TypeConversionUtils;
import com.fisk.mdm.vo.process.*;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import com.fisk.system.vo.emailserver.EmailServerVO;
import com.fisk.system.vo.roleinfo.RoleInfoVo;
import com.fisk.system.vo.roleinfo.UserInfoVo;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.BuildBatchApprovalDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
    private PublishTaskClient publishTaskClient;
    @Resource
    private UserClient userClient;
    @Resource
    private UserHelper userHelper;
    @Resource
    private TypeConversionUtils typeConversionUtils;
    @Resource
    private DataSynchronizationUtils dataSynchronizationUtils;
    @Resource
    private IModelService modelService;
    @Resource
    EntityMapper entityMapper;
    @Value("${pgsql-mdm.type}")
    private DataSourceTypeEnum type;
    @Value("${pgsql-mdm.url}")
    private String url;
    @Value("${pgsql-mdm.username}")
    private String username;
    @Value("${pgsql-mdm.password}")
    private String password;
    @Value("${approval}")
    private String approvalUrl;

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
        Map<Integer, String> userMap = getUserMap(personUserIds);
        Map<Integer, String> roleMap = getRoleMap(personRoleIds);
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
    public ResultEnum verifyProcessApply(Integer entityId) throws FkException {
        int userId = userHelper.getLoginUserInfo().id.intValue();
        ProcessInfoPO processInfo = processInfoService.getProcessInfo(entityId);
        if (processInfo != null) {
            List<ProcessNodePO> processNodes = processNodeService.getProcessNodes((int) processInfo.getId());
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
                if (uContains || rContains) {
                    return ResultEnum.VERIFY_APPROVAL;
                } else {
                    return ResultEnum.VERIFY_NOT_APPROVAL;
                }
            } else {
                return ResultEnum.VERIFY_APPROVAL;
            }
        }
        return ResultEnum.VERIFY_NOT_APPROVAL;
    }

    /**
     * 添加工单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addProcessApply(Integer entityId,String description, String batchNumber, EventTypeEnum eventTypeEnum) throws FkException {
        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        int userId = loginUserInfo.id.intValue();
        ProcessInfoPO processInfo = processInfoService.getProcessInfo(entityId);
        List<ProcessNodePO> processNodes = processNodeService.getProcessNodes((int) processInfo.getId());
        ProcessNodePO processNodePo = processNodes.get(1);
        List<ProcessPersonPO> processPersons = processPersonService.getProcessPersons((int) processNodePo.getId());
        ProcessApplyPO processApplyPo = new ProcessApplyPO();
        processApplyPo.setApplicant(String.valueOf(userId));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSSS");
        String number = sdf.format(System.currentTimeMillis());
        processApplyPo.setApprovalCode(number);
        processApplyPo.setApplicationTime(LocalDateTime.now());
        processApplyPo.setApproverNode((int) processNodePo.getId());
        processApplyPo.setDescription(description);
        processApplyPo.setProcessId((int) processInfo.getId());
        processApplyPo.setState(ApprovalNodeStateEnum.IN_PROGRESS);
        processApplyPo.setOperationType(eventTypeEnum);
        processApplyPo.setOpreationstate(ApprovalApplyStateEnum.IN_PROGRESS);
        processApplyPo.setFidataBatchCode(batchNumber);
        processApplyService.save(processApplyPo);
        //通知节点用户进行审批
        try {
            sendEmailToProcessNode(processApplyPo,loginUserInfo, getUserIds(processPersons));
        }catch (FkException e){
            return ResultEnum.EMAIL_NOT_SEND;
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 获取我的待审核流程
     */
    @Override
    public Page<ProcessApplyVO> getMyProcessApply(ProcessApplyDTO dto) {
        return processApplyService.getMyProcessApply(dto);
    }

    /**
     * 获取待处理审批列表
     */
    @Override
    public Page<PendingApprovalVO> getPendingApproval(PendingApprovalDTO dto) {
        return processApplyService.getPendingApproval(dto);
    }

    /**
     * 获取所有审批列表
     */
    @Override
    public Page<AllApprovalVO> getAllApproval(AllApprovalDTO dto) {
        return processApplyService.getAllApproval(dto);
    }
    /**
     * 获取已处理审批列表
     */
    @Override
    public Page<EndingApprovalVO> getOverApproval(EndingApprovalDTO dto) {
        return processApplyService.getOverApproval(dto);
    }

    @Override
    public ApprovalDetailVO getApprovalDetail(Integer applyId) {
        ProcessApplyPO processApplyPo = processApplyService.getById(applyId);
        List<PersonVO> persons = new ArrayList<>();
        LambdaQueryWrapper<ProcessApplyNotesPO> notesWrapper = new LambdaQueryWrapper<>();
        notesWrapper.eq(ProcessApplyNotesPO::getProcessapplyId, applyId)
                .orderByAsc(ProcessApplyNotesPO::getCreateTime);
        List<ProcessApplyNotesPO> list = processApplyNotesService.list(notesWrapper);
        List<ProcessNodePO> processNodes = processNodeService.getProcessNodes(processApplyPo.getProcessId())
                .stream().sorted(Comparator.comparing(ProcessNodePO::getLevels)).collect(Collectors.toList());
        List<ProcessNodePO> posNode = processNodes.stream().filter(i -> i.getLevels() > 0).collect(Collectors.toList());
        List<Long> userIds = new ArrayList<>();
        userIds.add(Long.parseLong(processApplyPo.getApplicant()));
        String applicant = null;
        if (list.size() > 0) {
            userIds = list.stream().map(i -> Long.parseLong(i.getCreateUser())).collect(Collectors.toList());
            if (posNode.size() == 0) {
               throw new FkException(ResultEnum.SAVE_PROCESS_NODE_ERROR);
            } else {
                Map<Integer, ProcessNodePO> posNodeMap = posNode.stream().collect(Collectors.toMap(i -> (int)i.getId(), i -> i));
                List<Integer> nodeIds = posNode.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
                List<ProcessPersonPO> processPersons = processPersonService.getProcessPersons(nodeIds);
                Map<Integer, List<ProcessPersonPO>> personMap = processPersons.stream()
                        .collect(Collectors.groupingBy(ProcessPersonPO::getRocessNodeId));
                //获取节点下角色Id
                List<Integer> roleIds = processPersons.stream()
                        .filter(i -> i.getType().getValue() == ProcessPersonTypeEnum.PERSON_TYPE1_ENUM.getValue())
                        .map(ProcessPersonPO::getUrid).collect(Collectors.toList());
                //获取节点下用户Id
                List<Long> users = processPersons.stream()
                        .filter(i -> i.getType().getValue() == ProcessPersonTypeEnum.PERSON_TYPE2_ENUM.getValue())
                        .map(i -> (long) i.getUrid()).collect(Collectors.toList());
                userIds.addAll(users);
                //远程调用接口获取角色及用户信息
                Map<Integer, String> userMap = getUserMap(userIds);
                Map<Integer, String> roleMap = getRoleMap(roleIds);
                for (ProcessApplyNotesPO processApplyNotesPO : list) {
                    PersonVO personVO = new PersonVO();
                    personVO.setApproval(userMap.get(Integer.valueOf(processApplyNotesPO.getCreateUser())));
                    personVO.setDescription(processApplyNotesPO.getRemark());
                    personVO.setState(processApplyNotesPO.getState().getName());
                    ProcessNodePO processNodePO = posNodeMap.get(processApplyNotesPO.getProcessnodeId());
                    personVO.setLevels(processNodePO.getLevels());
                    personVO.setProcessNodeName(processNodePO.getName());
                    persons.add(personVO);
                    posNodeMap.remove(processApplyNotesPO.getProcessnodeId());
                }
                posNode = new ArrayList<>(posNodeMap.values());
                applicant = getApprovalName(processApplyPo, persons, posNode, personMap, userMap, roleMap);
            }
        } else {
            List<Integer> nodeIds = posNode.stream().map(i -> (int) i.getId()).collect(Collectors.toList());
            List<ProcessPersonPO> processPersons = processPersonService.getProcessPersons(nodeIds);
            Map<Integer, List<ProcessPersonPO>> personMap = processPersons.stream()
                    .collect(Collectors.groupingBy(ProcessPersonPO::getRocessNodeId));
            //获取节点下角色Id
            List<Integer> roleIds = processPersons.stream()
                    .filter(i -> i.getType().getValue() == ProcessPersonTypeEnum.PERSON_TYPE1_ENUM.getValue())
                    .map(ProcessPersonPO::getUrid).collect(Collectors.toList());
            //获取节点下用户Id
            List<Long> users = processPersons.stream()
                    .filter(i -> i.getType().getValue() == ProcessPersonTypeEnum.PERSON_TYPE2_ENUM.getValue())
                    .map(i -> (long) i.getUrid()).collect(Collectors.toList());
            userIds.addAll(users);
            //远程调用接口获取角色及用户信息
            Map<Integer, String> userMap = getUserMap(userIds);
            Map<Integer, String> roleMap = getRoleMap(roleIds);
            applicant = getApprovalName(processApplyPo, persons, posNode, personMap, userMap, roleMap);
        }
        ApprovalDetailVO approvalDetailVO = new ApprovalDetailVO();
        approvalDetailVO.setApplyId((int)processApplyPo.getId());
        approvalDetailVO.setApprovalCode(processApplyPo.getApprovalCode());
        approvalDetailVO.setApplicant(applicant);
        approvalDetailVO.setDescription(processApplyPo.getDescription());
        approvalDetailVO.setOperationType(processApplyPo.getOperationType().getName());
        approvalDetailVO.setApplicationTime(processApplyPo.getApplicationTime());
        approvalDetailVO.setPersons(persons);

        return approvalDetailVO;
    }

    private String getApprovalName(ProcessApplyPO processApplyPo, List<PersonVO> persons, List<ProcessNodePO> processNodes, Map<Integer, List<ProcessPersonPO>> personMap, Map<Integer, String> userMap, Map<Integer, String> roleMap) {
        String applicant;
        for (ProcessNodePO processNode : processNodes) {
            PersonVO personVO = new PersonVO();
            List<ProcessPersonPO> processPersonPOS = personMap.get((int) processNode.getId());
            String name = processPersonPOS.stream().map(i -> {
                if (i.getType().getValue() == ProcessPersonTypeEnum.PERSON_TYPE1_ENUM.getValue()) {
                    return roleMap.get(i.getUrid());
                }
                if (i.getType().getValue() == ProcessPersonTypeEnum.PERSON_TYPE2_ENUM.getValue()) {
                    return userMap.get(i.getUrid());
                }
                return null;
            }).collect(Collectors.joining(","));
            personVO.setApproval(name);
            personVO.setState(ApprovalNodeStateEnum.IN_PROGRESS.getName());
            personVO.setLevels(processNode.getLevels());
            personVO.setProcessNodeName(processNode.getName());
            persons.add(personVO);
        }
        applicant = userMap.get(Integer.valueOf(processApplyPo.getApplicant()));
        return applicant;
    }

    /**
     * 审批
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
        if (dto.getAdminMark()){
            return saveNoteByAll(dto, processInfo, processApplyPo, processNodes, processPersonMap);
        }
        //判断自动审批规则
        switch (processInfo.getAutoapproal()) {
            case ONLY_ONE_RULE:
                //仅首个节点需审批，其余自动同意
                return saveNoteByOnlyOne(dto, processInfo, processApplyPo, processNodes, processPersonMap);
            case CONTINUOUS_APPROVAL_RULE:
                //仅连续审批时自动同意
                return saveNoteByContinuous(dto, processInfo, processApplyPo, processNodes, processPersonMap);
            case ALL_APPROVAL_RULE:
                //每个节点都需要审批
                return saveNoteByAll(dto, processInfo, processApplyPo, processNodes, processPersonMap);
            default:
                return ResultEnum.ERROR;
        }
    }

    @Override
    public ResultEnum batchApproval(BatchApprovalDTO dto) {
        if(CollectionUtils.isEmpty(dto.getProcessApplyIds()) || dto.getFlag() == null){
            return ResultEnum.DATA_NOTEXISTS;
        }
        List<com.fisk.task.dto.model.ApprovalDTO> data = new ArrayList<>(dto.getProcessApplyIds().size());
        for (Integer processApplyId : dto.getProcessApplyIds()) {
            com.fisk.task.dto.model.ApprovalDTO approvalDTO = new com.fisk.task.dto.model.ApprovalDTO();
            approvalDTO.setDescription(dto.getDescription());
            approvalDTO.setFlag(dto.getFlag());
            approvalDTO.setProcessApplyId(processApplyId);
            data.add(approvalDTO);
        }
        BuildBatchApprovalDTO batchApprovalDTO = new BuildBatchApprovalDTO();
        batchApprovalDTO.setData(data);
        batchApprovalDTO.setUserId(userHelper.getLoginUserInfo().getId());
        if (publishTaskClient.createBatchApproval(batchApprovalDTO).getCode() != ResultEnum.SUCCESS.getCode()) {
            return ResultEnum.DATA_SUBMIT_ERROR;
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum rollbackApproval(Integer applyId) {
        LambdaQueryWrapper<ProcessApplyNotesPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProcessApplyNotesPO::getProcessapplyId,applyId);
        List<ProcessApplyNotesPO> processApplyNotesPos = processApplyNotesService.list(queryWrapper);
        if(processApplyNotesPos.size()>0){
            return ResultEnum.PROCESS_NOT_ROLLBACK;
        }else {
            LambdaQueryWrapper<ProcessApplyPO> delQueryWrapper = new LambdaQueryWrapper<>();
            delQueryWrapper.eq(ProcessApplyPO::getId,applyId);
            int delete = processApplyService.getBaseMapper().delete(delQueryWrapper);
            if (delete>0){
                return ResultEnum.SUCCESS;
            }
            return ResultEnum.ERROR;
        }
    }

    @Override
    public void downloadApprovalApply(Integer applyId, HttpServletResponse response) {
        ProcessApplyPO applyPo = processApplyService.getById(applyId);
        if (applyPo == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        ProcessInfoPO processInfo = processInfoService.getProcessInfo(applyPo.getProcessId());
        if (processInfo == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        dataSynchronizationUtils.downloadStgData(processInfo.getEntityId(),applyPo.getFidataBatchCode(),response);
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
                                        ProcessInfoPO processInfo,
                                        ProcessApplyPO processApplyPo,
                                        List<ProcessNodePO> processNodes,
                                        Map<Integer, List<ProcessPersonPO>> processPersonMap) {

        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        List<ProcessPersonPO> processPersonPos = processPersonMap.get((int) processApplyPo.getApproverNode());
        List<Long> userIds = getUserIds(processPersonPos);
        //校验当前用户是否可以审批
        if (userIds.contains((long) loginUserInfo.id)) {
            //当前用户第一次保存审批节点
            ProcessApplyNotesPO processApplyNotesPo = new ProcessApplyNotesPO();
            ;
            processApplyNotesPo.setState(typeConversionUtils.intToApprovalNodeStateEnum(dto.getFlag()));
            processApplyNotesPo.setRemark(dto.getDescription());
            processApplyNotesPo.setProcessapplyId((int)processApplyPo.getId());
            processApplyNotesPo.setProcessnodeId(processApplyPo.getApproverNode());
            processApplyNotesService.save(processApplyNotesPo);
            Map<Integer, ProcessNodePO> processNodeMap = processNodes.stream().collect(Collectors.toMap(i -> (int) i.getId(), i -> i));
            ProcessNodePO processNodePO = processNodes.get(processNodes.size() - 1);
            //审核通过
            if (dto.getFlag() == 1) {
                //判断是否是最后一个节点
                if (processApplyPo.getApproverNode() == processNodePO.getId()) {
                    try {
                        dataSynchronization(processInfo, processApplyPo);
                    } catch (FkException e) {
                        return e.getResultEnum();
                    }
                    processApplyPo.setState(ApprovalNodeStateEnum.APPROVE);
                    processApplyPo.setOpreationstate(ApprovalApplyStateEnum.APPROVE);
                    processApplyService.updateById(processApplyPo);
                    //发送通过邮箱
                    try {
                        return sendEmailToResult(processApplyPo.getApprovalCode(), ApprovalApplyStateEnum.APPROVE);
                    }catch (FkException e){
                        return ResultEnum.EMAIL_NOT_SEND;
                    }
                } else {
                    ProcessNodePO processNodePo = processNodeMap.get(processApplyPo.getApproverNode());
                    int i = processNodePo.getLevels() + 1;
                    ProcessNodePO processNodePoNext = processNodes.get(i);
                    //递归处理仅需首次审批
                    ProcessServiceImpl processService = (ProcessServiceImpl) AopContext.currentProxy();
                    return processService.saveOnlyOneApply(processInfo,processNodePoNext, processApplyPo, loginUserInfo, processNodes, processPersonMap);
                }
                //审核拒绝
            } else if (dto.getFlag() == 2) {
                processApplyPo.setState(ApprovalNodeStateEnum.REFUSED);
                processApplyPo.setOpreationstate(ApprovalApplyStateEnum.REFUSED);
                processApplyService.updateById(processApplyPo);
                //发送拒绝邮件通知
                try {
                    return sendEmailToResult(processApplyPo.getApprovalCode(), ApprovalApplyStateEnum.REFUSED);
                }catch (FkException e){
                    return ResultEnum.EMAIL_NOT_SEND;
                }
            } else {
                return ResultEnum.PARAMTER_ERROR;
            }
        } else {
            return ResultEnum.ACCOUNT_CANNOT_OPERATION_API;
        }
    }

    public Connection getConnection() {
        AbstractDbHelper dbHelper = new AbstractDbHelper();
        return dbHelper.connection(url, username,
                password, type);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveNoteByContinuous(ApprovalDTO dto,
                                           ProcessInfoPO processInfo,
                                           ProcessApplyPO processApplyPo,
                                           List<ProcessNodePO> processNodes,
                                           Map<Integer, List<ProcessPersonPO>> processPersonMap) {
        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        List<ProcessPersonPO> processPersonPos = processPersonMap.get((int) processApplyPo.getApproverNode());
        List<Long> userIds = getUserIds(processPersonPos);
        //校验当前用户是否支持审批
        if (userIds.contains((long) loginUserInfo.id)) {
            //第一次保存审批节点信息
            ProcessApplyNotesPO processApplyNotesPo = new ProcessApplyNotesPO();
            processApplyNotesPo.setState(typeConversionUtils.intToApprovalNodeStateEnum(dto.getFlag()));
            processApplyNotesPo.setRemark(dto.getDescription());
            processApplyNotesPo.setProcessapplyId((int)processApplyPo.getId());
            processApplyNotesPo.setProcessnodeId(processApplyPo.getApproverNode());
            processApplyNotesService.save(processApplyNotesPo);
            Map<Integer, ProcessNodePO> processNodeMap = processNodes.stream().collect(Collectors.toMap(i -> (int) i.getId(), i -> i));
            //获取最后一个节点
            ProcessNodePO processNodePO = processNodes.get(processNodes.size() - 1);
            //通过
            if (dto.getFlag() == 1) {
                if (processApplyPo.getApproverNode() == processNodePO.getId()) {
                    try {
                        dataSynchronization(processInfo, processApplyPo);
                    } catch (FkException e) {
                        return e.getResultEnum();
                    }
                    processApplyPo.setState(ApprovalNodeStateEnum.APPROVE);
                    processApplyPo.setOpreationstate(ApprovalApplyStateEnum.APPROVE);
                    processApplyService.updateById(processApplyPo);
                    //最后一个节点发送通过邮箱
                    try {
                        return sendEmailToResult(processApplyPo.getApprovalCode(), ApprovalApplyStateEnum.APPROVE);
                    }catch (FkException e){
                        return ResultEnum.EMAIL_NOT_SEND;
                    }
                } else {
                    ProcessNodePO processNodePo = processNodeMap.get(processApplyPo.getApproverNode());
                    int i = processNodePo.getLevels() + 1;
                    ProcessNodePO processNodePoNext = processNodes.get(i);
                    try {
                        //递归处理自动同意
                        ProcessServiceImpl processService = (ProcessServiceImpl) AopContext.currentProxy();
                        return processService.saveApply(processInfo,processNodePoNext, processApplyPo, loginUserInfo, processNodes, processPersonMap);
                    } catch (Exception e) {
                        return ResultEnum.ERROR;
                    }
                }
                //拒绝
            } else if (dto.getFlag() == 2) {
                processApplyPo.setState(ApprovalNodeStateEnum.REFUSED);
                processApplyPo.setOpreationstate(ApprovalApplyStateEnum.APPROVE);
                processApplyService.updateById(processApplyPo);
                //发送拒绝邮箱
                try {
                    return sendEmailToResult(processApplyPo.getApprovalCode(), ApprovalApplyStateEnum.REFUSED);
                }catch (FkException e){
                    return ResultEnum.EMAIL_NOT_SEND;
                }
            } else {
                return ResultEnum.PARAMTER_ERROR;
            }
        } else {
            return ResultEnum.ACCOUNT_CANNOT_OPERATION_API;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveNoteByAll(ApprovalDTO dto,
                                    ProcessInfoPO processInfo,
                                    ProcessApplyPO processApplyPo,
                                    List<ProcessNodePO> processNodes,
                                    Map<Integer, List<ProcessPersonPO>> processPersonMap) {
        UserInfo loginUserInfo = userHelper.getLoginUserInfo();
        List<ProcessPersonPO> processPersonPos = processPersonMap.get((int) processApplyPo.getApproverNode());
        List<Long> userIds = getUserIds(processPersonPos);
        if (dto.getAdminMark()){
            ResultEntity<Boolean> processQuery = userClient.verifyPageByUserId(loginUserInfo.id.intValue(), "processQuery");
            if (processQuery.code == ResultEnum.SUCCESS.getCode() && processQuery.getData() != null) {
                if (!processQuery.getData()){
                    return ResultEnum.ACCOUNT_CANNOT_OPERATION_API;
                }
            } else {
                log.error("远程调用失败，错误code: " + processQuery.getCode() + ",错误信息: " + processQuery.getMsg());
                throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
            }
        }
        //校验是否可以审批
        if (userIds.contains(loginUserInfo.id)) {
            //保存
            ProcessApplyNotesPO processApplyNotesPo = new ProcessApplyNotesPO();
            processApplyNotesPo.setState(typeConversionUtils.intToApprovalNodeStateEnum(dto.getFlag()));
            processApplyNotesPo.setRemark(dto.getDescription());
            processApplyNotesPo.setProcessapplyId((int)processApplyPo.getId());
            processApplyNotesPo.setProcessnodeId(processApplyPo.getApproverNode());
            processApplyNotesService.save(processApplyNotesPo);
            Map<Integer, ProcessNodePO> processNodeMap = processNodes.stream().collect(Collectors.toMap(i -> (int) i.getId(), i -> i));
            ProcessNodePO processNodePo = processNodes.get(processNodes.size() - 1);
            if (dto.getFlag() == 1) {
                if (processApplyPo.getApproverNode() == processNodePo.getId()) {
                    try {
                        dataSynchronization(processInfo, processApplyPo);
                    } catch (FkException e) {
                        return e.getResultEnum();
                    }
                    processApplyPo.setState(ApprovalNodeStateEnum.APPROVE);
                    processApplyPo.setOpreationstate(ApprovalApplyStateEnum.APPROVE);
                    processApplyService.updateById(processApplyPo);
                    //所有节点都需要审批无需特殊处理
                    try {
                        return sendEmailToResult(processApplyPo.getApprovalCode(), ApprovalApplyStateEnum.APPROVE);
                    }catch (FkException e){
                        return ResultEnum.EMAIL_NOT_SEND;
                    }
                } else {
                    ProcessNodePO processNodePO1 = processNodeMap.get(processApplyPo.getApproverNode());
                    int i = processNodePO1.getLevels() + 1;
                    ProcessNodePO processNodeP02 = processNodes.get(i);
                    processApplyPo.setState(ApprovalNodeStateEnum.IN_PROGRESS);
                    processApplyPo.setOpreationstate(ApprovalApplyStateEnum.IN_PROGRESS);
                    processApplyPo.setApproverNode((int) processNodeP02.getId());
                    processApplyService.updateById(processApplyPo);
                    //发送节点通知邮箱

                    try {
                        return sendEmailToProcessNode(processApplyPo,loginUserInfo, userIds);
                    }catch (FkException e){
                        return ResultEnum.EMAIL_NOT_SEND;
                    }
                }
            } else if (dto.getFlag() == 2) {
                processApplyPo.setState(ApprovalNodeStateEnum.REFUSED);
                processApplyPo.setOpreationstate(ApprovalApplyStateEnum.REFUSED);
                processApplyService.updateById(processApplyPo);
                //发送拒绝邮箱
                try {
                    return sendEmailToResult(processApplyPo.getApprovalCode(), ApprovalApplyStateEnum.REFUSED);
                }catch (FkException e){
                    return ResultEnum.EMAIL_NOT_SEND;
                }
            } else {
                return ResultEnum.PARAMTER_ERROR;
            }
        } else {
            return ResultEnum.ACCOUNT_CANNOT_OPERATION_API;
        }
    }

    private ResultEnum sendEmailToResult(String applyCode, ApprovalApplyStateEnum stateEnum) {
        try {
            String emailAddress = null;
            //获取需要通知的人员邮箱
            ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(Collections.singletonList((long) userHelper.getLoginUserInfo().id));
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
                    message = "你申请的流程" + applyCode + "已通过";
                    break;
                case REFUSED:
                    message = "你申请的流程" + applyCode + "被拒绝";
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

    private ResultEnum sendEmailToProcessNode(ProcessApplyPO processApplyPo,UserInfo loginUserInfo, List<Long> userIds) {

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
                        "提交了一个审批流程<a>"+processApplyPo.getApprovalCode()+"</a>，请前往MDS系统中的我的审批页面查看并审批，系统地址：</span></div><div>&nbsp; &nbsp; &nbsp; " +
                        "&nbsp; &nbsp; &nbsp; &nbsp; &nbsp;&nbsp;<a href=\""+approvalUrl+"myApproval\">" +
                        ""+approvalUrl+"myApproval</a>");
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
    public ResultEnum saveApply(ProcessInfoPO processInfo,
                                ProcessNodePO processNode,
                                ProcessApplyPO processApplyPo,
                                UserInfo loginUserInfo,
                                List<ProcessNodePO> processNodes,
                                Map<Integer, List<ProcessPersonPO>> processPersonMap) {
        List<ProcessPersonPO> processPersonPos = processPersonMap.get((int) processNode.getId());
        List<Long> userIds = getUserIds(processPersonPos);
        //节点改变重新校验
        if (userIds.contains((long) loginUserInfo.id)) {
            //保存
            ProcessApplyNotesPO processApplyNotesPo = new ProcessApplyNotesPO();
            processApplyNotesPo.setState(ApprovalNodeStateEnum.APPROVE);
            processApplyNotesPo.setRemark("auto");
            processApplyNotesPo.setProcessapplyId((int)processApplyPo.getId());
            processApplyNotesPo.setProcessnodeId((int) processNode.getId());
            processApplyNotesService.save(processApplyNotesPo);
            //判断是否最后一个节点
            if (processNode.getLevels() + 1 < processNodes.size()) {
                ProcessNodePO processNodePo = processNodes.get(processNode.getLevels() + 1);
                //当前节点支持审批继续递归处理下一个节点
                return saveApply(processInfo,processNodePo, processApplyPo, loginUserInfo, processNodes, processPersonMap);
            } else {
                try {
                    dataSynchronization(processInfo, processApplyPo);
                } catch (FkException e) {
                    return e.getResultEnum();
                }
                processApplyPo.setApproverNode((int) processNode.getId());
                processApplyPo.setState(ApprovalNodeStateEnum.APPROVE);
                processApplyPo.setOpreationstate(ApprovalApplyStateEnum.APPROVE);
                processApplyService.updateById(processApplyPo);
                //最后一个节点发送同意通知
                return sendEmailToResult(processApplyPo.getApprovalCode(), ApprovalApplyStateEnum.APPROVE);
            }
        } else {
            if (processNode.getLevels() + 1 < processNodes.size()) {
                ProcessNodePO processNodePo = processNodes.get(processNode.getLevels() + 1);
                processApplyPo.setState(ApprovalNodeStateEnum.IN_PROGRESS);
                processApplyPo.setOpreationstate(ApprovalApplyStateEnum.IN_PROGRESS);
                processApplyPo.setApproverNode((int) processNodePo.getId());
                processApplyService.updateById(processApplyPo);
                //当前用户不能审批则发送通知节点用户邮箱
                try {
                    return sendEmailToProcessNode(processApplyPo,loginUserInfo, userIds);
                }catch (FkException e){
                    return ResultEnum.EMAIL_NOT_SEND;
                }
            } else {
                processApplyPo.setState(ApprovalNodeStateEnum.APPROVE);
                processApplyPo.setOpreationstate(ApprovalApplyStateEnum.APPROVE);
                processApplyPo.setApproverNode((int) processNode.getId());
                processApplyService.updateById(processApplyPo);
                try {
                    return sendEmailToResult(processApplyPo.getApprovalCode(), ApprovalApplyStateEnum.APPROVE);
                }catch (FkException e){
                    return ResultEnum.EMAIL_NOT_SEND;
                }
            }
        }
    }

    /**
     * 只有首个节点需审批
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveOnlyOneApply(ProcessInfoPO processInfo,
                                       ProcessNodePO processNode,
                                       ProcessApplyPO processApplyPo,
                                       UserInfo loginUserInfo,
                                       List<ProcessNodePO> processNodes,
                                       Map<Integer, List<ProcessPersonPO>> processPersonMap) {
        boolean flag = false;
        LambdaQueryWrapper<ProcessApplyNotesPO> poLambdaQueryWrapper = new LambdaQueryWrapper<>();
        poLambdaQueryWrapper.eq(ProcessApplyNotesPO::getProcessapplyId, processApplyPo.getId());
        List<ProcessApplyNotesPO> list = processApplyNotesService.list(poLambdaQueryWrapper);
        List<Integer> applyNoteUser = list.stream().map(i -> Integer.valueOf(i.getCreateUser())).collect(Collectors.toList());
        List<ProcessPersonPO> processPersonPos = processPersonMap.get((int)processNode.getId());
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
            processApplyNotesPo.setState(ApprovalNodeStateEnum.APPROVE);
            processApplyNotesPo.setRemark("auto");
            processApplyNotesPo.setProcessapplyId((int)processApplyPo.getId());
            processApplyNotesPo.setProcessnodeId((int) processNode.getId());
            processApplyNotesService.save(processApplyNotesPo);
            //是否最后一个节点
            if (processNode.getLevels() + 1 < processNodes.size()) {
                ProcessNodePO processNodePo = processNodes.get(processNode.getLevels() + 1);
                //递归
                return saveOnlyOneApply(processInfo,processNodePo, processApplyPo, loginUserInfo, processNodes, processPersonMap);
            } else {
                //最后一个节点发送通过通知
                try {
                    dataSynchronization(processInfo, processApplyPo);
                } catch (FkException e) {
                    return e.getResultEnum();
                }
                processApplyPo.setApproverNode((int) processNode.getId());
                processApplyPo.setState(ApprovalNodeStateEnum.APPROVE);
                processApplyPo.setOpreationstate(ApprovalApplyStateEnum.APPROVE);
                processApplyService.updateById(processApplyPo);
                try {
                    return sendEmailToResult(processApplyPo.getApprovalCode(), ApprovalApplyStateEnum.APPROVE);
                }catch (FkException e){
                    return ResultEnum.EMAIL_NOT_SEND;
                }
            }
        } else {
            //当前节点无需自动审批通知当前节点人员进行审批
            processApplyPo.setApproverNode((int) processNode.getId());
            processApplyPo.setState(ApprovalNodeStateEnum.IN_PROGRESS);
            processApplyPo.setOpreationstate(ApprovalApplyStateEnum.IN_PROGRESS);
            processApplyService.updateById(processApplyPo);
            try {
                return sendEmailToProcessNode(processApplyPo,loginUserInfo, userIds);
            }catch (FkException e){
                return ResultEnum.EMAIL_NOT_SEND;
            }
        }
    }

    /**
     * 获取审批流程节点下所有成员
     */
    public List<Long> getUserIds(List<ProcessPersonPO> processPersons) throws FkException {
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

    private Map<Integer, String> getRoleMap(List<Integer> roleIds) {
        //远程调用接口获取角色信息
        Map<Integer, String> roleMap = new HashMap<>(roleIds.size());
        ResultEntity<List<RoleInfoDTO>> roles = userClient.getRoles(roleIds);
        if (roles.code == ResultEnum.SUCCESS.getCode() && roles.getData() != null) {
            roleMap = roles.getData().stream().collect(Collectors.toMap(i -> (int) i.getId(), RoleInfoDTO::getRoleName));
        } else {
            log.error("远程调用失败，错误code: " + roles.getCode() + ",错误信息: " + roles.getMsg());
        }
        return roleMap;
    }

    private Map<Integer, String> getUserMap(List<Long> userIds) {
        //远程调用接口获取用户信息
        Map<Integer, String> userMap = new HashMap<>(userIds.size());
        ResultEntity<List<UserDTO>> userDtos = userClient.getUserListByIds(userIds);
        if (userDtos.code == ResultEnum.SUCCESS.getCode() && userDtos.getData() != null) {
            userMap = userDtos.getData().stream().collect(Collectors.toMap(i -> i.getId().intValue(), UserDTO::getUsername));
        } else {
            log.error("远程调用失败，错误code: " + userDtos.getCode() + ",错误信息: " + userDtos.getMsg());
        }
        return userMap;
    }

    private void dataSynchronization(ProcessInfoPO processInfo, ProcessApplyPO processApplyPo) throws FkException {
        ResultEnum resultEnum = dataSynchronizationUtils.stgDataSynchronize(processInfo.getEntityId(), processApplyPo.getFidataBatchCode());
        EntityPO entityPO = entityMapper.selectById(processInfo.getEntityId());
        if (entityPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(ModelPO::getId, entityPO.getModelId());
        ModelPO modelPO = modelService.getOne(modelLambdaQueryWrapper);
        if (modelPO == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        String tableName = TableNameGenerateUtils.generateStgTableName(modelPO.getName(),entityPO.getName());
        if (resultEnum.getCode() != ResultEnum.DATA_SYNCHRONIZATION_SUCCESS.getCode()) {
            //where条件
            String queryConditions = " and fidata_batch_code ='" + processApplyPo.getFidataBatchCode() + "'";
            IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
            String sql = buildSqlCommand.buildQueryData(tableName, queryConditions);
            List<Map<String, Object>> maps = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
            //返回错误信息
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, maps.get(0).get("fidata_error_msg").toString());
        }
    }
}
