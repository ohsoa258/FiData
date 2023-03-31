package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.process.ProcessInfoDTO;
import com.fisk.mdm.dto.process.ProcessNodeDTO;
import com.fisk.mdm.entity.ProcessInfoPO;
import com.fisk.mdm.entity.ProcessNodePO;
import com.fisk.mdm.entity.ProcessPersonPO;
import com.fisk.mdm.enums.ProcessPersonTypeEnum;
import com.fisk.mdm.map.ProcessInfoMap;
import com.fisk.mdm.map.ProcessNodeMap;
import com.fisk.mdm.map.ProcessPersonMap;
import com.fisk.mdm.service.IProcessInfoService;
import com.fisk.mdm.service.IProcessNodeService;
import com.fisk.mdm.service.IProcessPersonService;
import com.fisk.mdm.service.ProcessService;
import com.fisk.mdm.vo.process.ProcessInfoVO;
import com.fisk.mdm.vo.process.ProcessNodeVO;
import com.fisk.mdm.vo.process.ProcessPersonVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.roleinfo.RoleInfoDTO;
import com.fisk.system.dto.userinfo.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private UserClient userClient;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum saveProcess(ProcessInfoDTO dto) {
        //逻辑删除流程信息
        processInfoService.deleteProcessInfo(dto.getEntityId());
        //逻辑删除流程节点信息
        LambdaQueryWrapper<ProcessNodePO> processNodePOLambdaQueryWrapper=new LambdaQueryWrapper<>();
        processNodePOLambdaQueryWrapper.eq(ProcessNodePO::getDelFlag,1);
        processNodeService.getBaseMapper().delete(processNodePOLambdaQueryWrapper);
        //逻辑删除流程节点角色或用户信息
        LambdaQueryWrapper<ProcessPersonPO> personPOLambdaQueryWrapper=new LambdaQueryWrapper<>();
        personPOLambdaQueryWrapper.eq(ProcessPersonPO::getDelFlag,1);
        processPersonService.getBaseMapper().delete(personPOLambdaQueryWrapper);
        //保存流程信息
        ProcessInfoPO processInfoPO = ProcessInfoMap.INSTANCES.dtoToPo(dto);
        boolean save = processInfoService.save(processInfoPO);
        if (!save){
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        //保存流程节点信息
        List<ProcessNodeDTO> processNodes = dto.getProcessNodes();
        if (processNodes.size()>0){
            List<ProcessNodePO> processNodePOS = ProcessNodeMap.INSTANCES.dtoListToPoList(processNodes);
            for (ProcessNodePO processNodePO : processNodePOS) {
                processNodePO.setProcessId((int) processInfoPO.getId());
            }
            boolean b = processNodeService.saveBatch(processNodePOS);
            if (!b){
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
            //保存流程节点用户及角色信息
            List<ProcessPersonPO> personList = new ArrayList<>();
            //赋值人员流程节点ID
            for (int i = 0; i < processNodes.size(); i++) {
                ProcessNodePO processNodePO = processNodePOS.get(i);
                ProcessNodeDTO processNodeDTO = processNodes.get(i);
                List<ProcessPersonPO> personPOS = ProcessPersonMap.INSTANCES
                        .dtoListToPoList(processNodeDTO.getPersonList());
                for (ProcessPersonPO personPO : personPOS) {
                    personPO.setRocessNodeId((int) processNodePO.getId());
                    personList.add(personPO);
                }
            }
            if (personList.size()>0){
                boolean batch = processPersonService.saveBatch(personList);
                if (!batch){
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                }
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ProcessInfoVO getProcess(Integer entityId) {
        if (entityId == null){
            return null;
        }
        //根据实体id获取流程对象
        ProcessInfoVO processInfo = processInfoService.getProcessInfo(entityId);
        if (processInfo == null){
            return null;
        }
        //根据流程id获取流程节点对象
        List<ProcessNodeVO> processNodes = processNodeService.getProcessNode(processInfo.getId());
        List<Integer> processNodeIds = processNodes.stream().map(ProcessNodeVO::getId).collect(Collectors.toList());
        //根据流程节点id获取流程节点人员对象
        List<ProcessPersonVO> personByNodes = processPersonService.getPersonByNodeIds(processNodeIds);
        //获取流程人员中角色id
        List<Integer> personRoleIds = personByNodes.stream().filter(i -> {
            if (ProcessPersonTypeEnum.PERSON_TYPE1_ENUM.getValue() == i.getType()){
                return true;
            }
            return false;
        }).map(ProcessPersonVO::getUrid).collect(Collectors.toList());
        //获取流程人员中用户id
        List<Long> personUserIds = personByNodes.stream().filter(i -> {
            if (ProcessPersonTypeEnum.PERSON_TYPE2_ENUM.getValue() == i.getType()){
                return true;
            }
            return false;
        }).map(i->(long)i.getUrid()).collect(Collectors.toList());
        //远程调用接口获取角色及用户信息
        Map<Integer, String> roleMap = new HashMap<>(personRoleIds.size());
        Map<Integer, String> userMap = new HashMap<>(personUserIds.size());
        ResultEntity<List<RoleInfoDTO>> roles = userClient.getRoles(personRoleIds);
        ResultEntity<List<UserDTO>> users = userClient.getUserListByIds(personUserIds);
        List<RoleInfoDTO> rolesData = roles.getData();
        List<UserDTO> usersData = users.getData();
        if (rolesData!=null && rolesData.size()>0){
            roleMap = rolesData.stream().collect(Collectors.toMap(i->(int)i.getId(), RoleInfoDTO::getRoleName));
        }
        if (usersData !=null && usersData.size()>0){
            userMap = usersData.stream().collect(Collectors.toMap(i->i.getId().intValue(), UserDTO::getUsername));
        }
        //根据节点分组获取每个节点下面的人员信息
        Map<Integer, List<ProcessPersonVO>> collect = personByNodes.stream()
                .collect(Collectors.groupingBy(ProcessPersonVO::getRocessNodeId));
        if (collect.size()>0){
            for (ProcessNodeVO processNode : processNodes) {
                //获取当前节点下的所有人员信息
                List<ProcessPersonVO> processPersonVOS = collect.get(processNode.getId());
                //给当前节点下的人员名称赋值
                for (ProcessPersonVO processPersonVO : processPersonVOS) {
                    if (ProcessPersonTypeEnum.PERSON_TYPE1_ENUM.getValue() == processPersonVO.getType()){
                        processPersonVO.setUrName(roleMap.get(processPersonVO.getUrid()));
                    }else if (ProcessPersonTypeEnum.PERSON_TYPE2_ENUM.getValue() == processPersonVO.getType()){
                        processPersonVO.setUrName(userMap.get(processPersonVO.getUrid()));
                    }
                }
                processNode.setPersonList(processPersonVOS);
            }
        }
        processInfo.setProcessNodes(processNodes);
        return processInfo;
    }
}
