package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import com.fisk.datafactory.map.NifiCustomWorkflowDetailMap;
import com.fisk.datafactory.mapper.NifiCustomWorkflowDetailMapper;
import com.fisk.datafactory.service.INifiComponent;
import com.fisk.datafactory.service.INifiCustomWorkflow;
import com.fisk.datafactory.service.INifiCustomWorkflowDetail;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import com.fisk.task.dto.task.BuildNifiCustomWorkFlowDTO;
import com.fisk.task.dto.task.NifiCustomWorkDTO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangyan and Lock
 */
@Service
@Slf4j
public class NifiCustomWorkflowDetailImpl extends ServiceImpl<NifiCustomWorkflowDetailMapper, NifiCustomWorkflowDetailPO> implements INifiCustomWorkflowDetail {

    @Resource
    INifiCustomWorkflow workflowService;
    @Resource
    NifiCustomWorkflowDetailMapper mapper;
    @Resource
    INifiComponent componentService;
    @Resource
    UserHelper userHelper;
    @Resource
    NifiCustomWorkflowImpl nifiCustomWorkflowImpl;

    @Override
    public NifiCustomWorkflowDetailDTO addData(NifiCustomWorkflowDetailDTO dto) {

        // dto-> po
        NifiCustomWorkflowDetailPO model = NifiCustomWorkflowDetailMap.INSTANCES.dtoToPo(dto);
        // 参数校验
        if (model == null) {
            throw new FkException(ResultEnum.PARAMTER_NOTNULL);
        }

        try {
            baseMapper.insert(model);
        } catch (Exception e) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        dto.id = model.id;
        // 保存
        return dto;
    }

    @Override
    public NifiCustomWorkflowDetailDTO getData(long id) {

        NifiCustomWorkflowDetailPO model = this.query().eq("id", id).one();
        if (model == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        return NifiCustomWorkflowDetailMap.INSTANCES.poToDto(model);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEntity<NifiCustomWorkListDTO> editData(NifiCustomWorkflowDetailVO dto) {

        // 修改tb_nifi_custom_wokflow
        NifiCustomWorkflowDTO workflowDTO = dto.dto;
        if (workflowDTO == null) {
            return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL);
        }
        try {
            workflowService.editData(workflowDTO);
        } catch (Exception e) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        List<NifiCustomWorkflowDetailPO> list = NifiCustomWorkflowDetailMap.INSTANCES.listDtoToPo(dto.list);

        // 批量保存tb_nifi_custom_wokflow_detail
        boolean success = this.updateBatchById(list);
        if (!success) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        NifiCustomWorkListDTO workListDTO = getWorkListDTO(workflowDTO.id, workflowDTO.workflowId, workflowDTO.workflowName, dto.list);

        return ResultEntityBuild.build(ResultEnum.SUCCESS, workListDTO);
    }

    /**
     * 组装nifi参数
     *
     * @param pipelineId   tb_nifi_custom_workflow表 id
     * @param workflowId   tb_nifi_custom_workflow表 workflowId
     * @param pipelineName tb_nifi_custom_workflow表 pipelineName
     * @param list         list
     * @return NifiCustomWorkListDTO
     */
    private NifiCustomWorkListDTO getWorkListDTO(Long pipelineId, String workflowId, String pipelineName, List<NifiCustomWorkflowDetailDTO> list) {
        UserInfo userInfo = userHelper.getLoginUserInfo();
        NifiCustomWorkListDTO workListDTO = new NifiCustomWorkListDTO();

        // 登录人
        workListDTO.userId = userInfo.id;
        // 管道
        workListDTO.pipelineId = pipelineId;
        // workflowId
        workListDTO.nifiCustomWorkflowId = workflowId;
        // 管道名称
        workListDTO.pipelineName = pipelineName;
        // 封装nifi所有节点
        workListDTO.nifiCustomWorkDTOS = getNifiCustomWorkList(list);
        // 管道详情-tree
        workListDTO.structure = getMenuTree(list);
        // 管道详情下的任务组-tree
        workListDTO.externalStructure = getMenuTree(workflowId, list);
        return workListDTO;
    }

    /**
     * 封装nifi所有节点
     *
     * @param list list
     * @return List<NifiCustomWorkDTO>
     */
    private List<NifiCustomWorkDTO> getNifiCustomWorkList(List<NifiCustomWorkflowDetailDTO> list) {
        String componentType = "schedule_task";
        List<NifiCustomWorkDTO> nifiCustomWorkDTOList = new ArrayList<>();
        list.stream().map(e -> {
            NifiCustomWorkDTO dto = new NifiCustomWorkDTO();

            // 每一个节点需要分别组装 inputDucts outputDucts
            NifiCustomWorkflowDetailPO po = this.query().eq("id", e.id).one();
            if (po.inport != null && !po.inport.equalsIgnoreCase("") && po.inport.length() > 0) {
                dto.inputDucts = getInputDucts(po);
            }
            if (po.outport != null && !po.outport.equalsIgnoreCase("") && po.outport.length() > 0) {
                dto.outputDucts = getOutputDucts(po);
            }
            dto.NifiNode = getBuildNifiCustomWorkFlowDTO(e);
            return nifiCustomWorkDTOList.add(dto);
        }).collect(Collectors.toList());

        return nifiCustomWorkDTOList;
    }

    private List<BuildNifiCustomWorkFlowDTO> getInputDucts(NifiCustomWorkflowDetailPO po) {
        List<BuildNifiCustomWorkFlowDTO> list;

        String inport = po.inport;
        String[] inportIds = inport.split(",");
        List<BuildNifiCustomWorkFlowDTO> result = new ArrayList<>();
        for (String inportId : inportIds) {
            NifiCustomWorkflowDetailPO id = this.query().eq("id", inportId).one();
            NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO = NifiCustomWorkflowDetailMap.INSTANCES.poToDto(id);
            BuildNifiCustomWorkFlowDTO buildNifiCustomWorkFlowDTO = getBuildNifiCustomWorkFlowDTO(nifiCustomWorkflowDetailDTO);
            result.add(buildNifiCustomWorkFlowDTO);
        }
        list = result;
        return list;
    }

    private List<BuildNifiCustomWorkFlowDTO> getOutputDucts(NifiCustomWorkflowDetailPO po) {
        List<BuildNifiCustomWorkFlowDTO> list = new ArrayList<>();
        String outport = po.outport;
        String[] outportIds = outport.split(",");

        return Arrays.stream(outportIds).map(outportId ->
                        this.query().eq("id", outportId).one())
                .map(NifiCustomWorkflowDetailMap.INSTANCES::poToDto)
                .map(this::getBuildNifiCustomWorkFlowDTO)
                .collect(Collectors.toList());
    }

    /**
     * 组装节点参数
     *
     * @param dto NifiCustomWorkflowDetailDTO
     * @return BuildNifiCustomWorkFlowDTO
     */
    private BuildNifiCustomWorkFlowDTO getBuildNifiCustomWorkFlowDTO(NifiCustomWorkflowDetailDTO dto) {

        String scheduleType = "开始";
        String taskGroupTpye = "任务组";
        BuildNifiCustomWorkFlowDTO flow = new BuildNifiCustomWorkFlowDTO();
        // 操作类型
        flow.type = getDataClassifyEnum(dto.componentsId);
        // 表类型
        flow.tableType = getOlapTableEnum(dto.componentsId);
        flow.tableId = dto.tableId;

        if (dto.pid == 0) {
            flow.groupId = this.query().eq("id", dto.id).one().workflowId;
        } else{
            flow.groupId = dto.pid.toString();
        }

        flow.workflowDetailId = dto.id;

        // 任务组时，appId即tb_nifi_custom_workflow_detail表id
        if (taskGroupTpye.equalsIgnoreCase(dto.componentType)) {
            flow.appId = dto.id;
        }
        // 开始才有的属性
        if (scheduleType.equalsIgnoreCase(dto.componentType)) {
            flow.nifiCustomWorkflowName = dto.componentName;
            flow.nifiCustomWorkflowId = dto.id;
            flow.scheduleExpression = dto.script;
            if (dto.schedule == 1) {
                flow.scheduleType = SchedulingStrategyTypeEnum.TIMER;
            } else {
                flow.scheduleType = SchedulingStrategyTypeEnum.CRON;
            }
        }

        return flow;
    }

    /**
     * 获取操作类型
     *
     * @param componentsId componentsId
     * @return DataClassifyEnum
     */
    private DataClassifyEnum getDataClassifyEnum(Integer componentsId) {
        switch (componentsId) {
            // 开始
            case 1:
                return DataClassifyEnum.CUSTOMWORKSCHEDULINGCOMPONENT;
            // 任务组
            case 2:
                return DataClassifyEnum.CUSTOMWORKSTRUCTURE;
            // 数据接入(数据湖)
            case 3:
                return DataClassifyEnum.CUSTOMWORKDATAACCESS;
            // 数仓维度、事实
            case 4:
            case 5:
                return DataClassifyEnum.CUSTOMWORKDATAMODELING;
            // 分析模型维度、事实
            case 6:
            case 7:
                return DataClassifyEnum.CUSTOMWORKDATAMODELKPL;
            default:
                break;
        }
        return null;
    }

    /**
     * 获取表类型
     *
     * @param componentsId componentsId
     * @return OlapTableEnum
     */
    private OlapTableEnum getOlapTableEnum(Integer componentsId) {
        switch (componentsId) {
            // 开始
            // 任务组
            case 1:
            case 2:
                break;
            // 数据接入(数据湖)
            case 3:
                return OlapTableEnum.CUSTOMWORKPHYSICS;
            // 数仓维度
            case 4:
                return OlapTableEnum.CUSTOMWORKDIMENSION;
            // 数仓事实
            case 5:
                return OlapTableEnum.CUSTOMWORKFACT;
            // 分析模型维度、事实
            case 6:
            case 7:
                return OlapTableEnum.CUSTOMWORKKPI;
            default:
                break;
        }
        return null;
    }

    /**
     * 管道详情-tree
     *
     * @param list tb_nifi_custom_workflow_detail表 list对象
     * @return map
     */
    private Map<Map, Map> getMenuTree(List<NifiCustomWorkflowDetailDTO> list) {
        String componentType = "任务组";
        List<NifiCustomWorkflowDetailDTO> collect = list.stream().filter(item -> componentType.equalsIgnoreCase(item.componentType)).collect(Collectors.toList());
        // 父
        List<NifiCustomWorkflowDetailDTO> collect1 = collect.stream().filter(item -> item.pid == 0L).collect(Collectors.toList());
        // 子
        List<NifiCustomWorkflowDetailDTO> collect2 = collect.stream().filter(item -> item.pid != 0L).collect(Collectors.toList());
        Map<Map, Map> structure = new HashMap<>();
        for (NifiCustomWorkflowDetailDTO dto1 : collect1) {
            // 父
            Map<Long, String> structure1 = new HashMap<>();
            // 子
            Map<Long, String> structure2 = new HashMap<>();
            for (NifiCustomWorkflowDetailDTO dto2 : collect2) {
                if (dto1.id == dto2.pid) {
                    structure1.put(dto1.id, dto1.componentName);
                    structure2.put(dto2.id, dto2.componentName);
                    structure.put(structure1, structure2);
                }
            }
            structure1 = null;
            structure2 = null;
        }
        return structure;
    }

    /**
     * 管道详情下的任务组-tree
     *
     * @param workflowId tb_nifi_custom_workflow表 workflowId
     * @param list tb_nifi_custom_workflow_detail表 list对象
     * @return map
     */
    private Map<Map, Map> getMenuTree(String workflowId, List<NifiCustomWorkflowDetailDTO> list) {
        String componentType = "任务组";
        NifiCustomWorkflowPO workflowPo = nifiCustomWorkflowImpl.query().eq("workflow_id", workflowId).one();
        List<NifiCustomWorkflowDetailDTO> collect = list.stream().filter(item -> item.pid == 0 && componentType.equalsIgnoreCase(item.componentType)).collect(Collectors.toList());
        Map<Map, Map> structure = new HashMap<>();
        Map structure1 = new HashMap();
        structure1.put(workflowPo.workflowId, workflowPo.workflowName);

        Map<Long, String> map = new HashMap<>();
        for (NifiCustomWorkflowDetailDTO dto : collect) {
            map.put(dto.id, dto.componentName);
        }
        Map structure2 = map;
        structure.put(structure1, structure2);
        return structure;
    }

    @Override
    public ResultEnum deleteData(long id) {
        // 参数校验
        NifiCustomWorkflowDetailPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 执行删除
        return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum editWorkflow(NifiCustomWorkflowDetailDTO dto) {
        // 参数校验
        NifiCustomWorkflowDetailPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // dto -> po
        NifiCustomWorkflowDetailPO po = NifiCustomWorkflowDetailMap.INSTANCES.dtoToPo(dto);
        // 执行修改
        return this.updateById(po) ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }
}
