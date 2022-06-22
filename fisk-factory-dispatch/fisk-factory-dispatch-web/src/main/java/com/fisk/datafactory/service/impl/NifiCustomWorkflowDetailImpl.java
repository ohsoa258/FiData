package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.task.TopicTypeEnum;
import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.WorkflowTaskGroupDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datafactory.map.NifiCustomWorkflowDetailMap;
import com.fisk.datafactory.mapper.NifiCustomWorkflowDetailMapper;
import com.fisk.datafactory.service.INifiComponent;
import com.fisk.datafactory.service.INifiCustomWorkflow;
import com.fisk.datafactory.service.INifiCustomWorkflowDetail;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.BuildNifiCustomWorkFlowDTO;
import com.fisk.task.dto.task.NifiCustomWorkDTO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;
import com.fisk.task.dto.task.TableTopicDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    DataModelClient dataModelClient;
    @Resource
    DataAccessClient dataAccessClient;


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
    public List<NifiCustomWorkflowDetailDTO> addDataList(List<NifiCustomWorkflowDetailDTO> list) {

        return list.stream().map(this::addData).collect(Collectors.toList());
    }

    @Override
    public ResultEnum editDataList(List<NifiCustomWorkflowDetailDTO> list) {

        return this.updateBatchById(NifiCustomWorkflowDetailMap.INSTANCES.listDtoToPo(list)) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
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

        String componentType = "开始";

        // 修改tb_nifi_custom_wokflow
        NifiCustomWorkflowDTO workflowDTO = dto.dto;
        if (workflowDTO == null) {
            return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL);
        }
        try {
            if (dto.flag) {
                // 正在发布
                workflowDTO.status = 3;
            }
            workflowService.editData(workflowDTO);
        } catch (Exception e) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        List<NifiCustomWorkflowDetailPO> list = NifiCustomWorkflowDetailMap.INSTANCES.listDtoToPo(dto.list);

        // 判断开始组件是否有调度参数(用于确保开始组件的调度参数不为空)
        List<NifiCustomWorkflowDetailPO> start = list.stream().filter(e -> componentType.equalsIgnoreCase(e.componentType)).collect(Collectors.toList());
        for (NifiCustomWorkflowDetailPO e : start) {
            if (e.schedule == null || e.script == null || "".equals(e.script)) {
                return ResultEntityBuild.build(ResultEnum.SCHEDULE_PARAME_NULL);
            }
        }

        // 批量保存tb_nifi_custom_wokflow_detail
        boolean success = this.updateBatchById(list);
        if (!success) {
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        NifiCustomWorkListDTO workListDTO = new NifiCustomWorkListDTO();
        if (dto.flag) {
            // 前端有时会传入已经删除的组件,后端使用入库后的数据
            List<NifiCustomWorkflowDetailPO> originalWorkflowDetailPoList = this.query().eq("workflow_id", workflowDTO.workflowId).list();

            // 重新组装参数
            List<NifiCustomWorkflowDetailPO> workflowDetailPoList = buildWorkflowDetailPoList(originalWorkflowDetailPoList);

            // 给nifi封装参数
            if (CollectionUtils.isNotEmpty(workflowDetailPoList)) {
                List<NifiCustomWorkflowDetailDTO> workflowDetailDTOList = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(workflowDetailPoList);
                // 调用组装nifi参数方法
                workListDTO = getWorkListDTO(workflowDTO.id, workflowDTO.workflowId, workflowDTO.workflowName, workflowDetailDTOList);
            }
        }


        return ResultEntityBuild.build(ResultEnum.SUCCESS, workListDTO);
    }

    /**
     * 重新组装管道详情的componentsId
     *
     * @return java.util.List<com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO>
     * @description 重新组装管道详情的componentsId
     * @author Lock
     * @date 2022/5/7 14:31
     * @version v1.0
     * @params workflowDetailPoList
     */
    private List<NifiCustomWorkflowDetailPO> buildWorkflowDetailPoList(List<NifiCustomWorkflowDetailPO> workflowDetailPoList) {
        for (NifiCustomWorkflowDetailPO e : workflowDetailPoList) {
            ChannelDataEnum channelDataEnum = ChannelDataEnum.getValue(e.componentType);
            switch (Objects.requireNonNull(channelDataEnum)) {
                // 开始
                case SCHEDULE_TASK:
                    // 任务组
                case TASKGROUP:
                    // 数据湖表任务
                case DATALAKE_TASK:
                    break;
                case DW_TASK:
                    e.componentsId = 11;
                    break;
                // 数仓维度表任务
                case DW_DIMENSION_TASK:
                    e.componentsId = 4;
                    break;
                // 数仓事实表任务
                case DW_FACT_TASK:
                    e.componentsId = 5;
                    break;
                case OLAP_TASK:
                    e.componentsId = 12;
                    break;
                // 分析模型维度表任务
                case OLAP_DIMENSION_TASK:
                    e.componentsId = 6;
                    break;
                // 分析模型事实表任务
                case OLAP_FACT_TASK:
                    e.componentsId = 7;
                    break;
                // 分析模型宽表任务
                case OLAP_WIDETABLE_TASK:
                    e.componentsId = 8;
                    break;
                // 数据湖ftp任务
                case DATALAKE_FTP_TASK:
                    e.componentsId = 9;
                    break;
                // 数据湖非实时api任务
                case DATALAKE_API_TASK:
                    e.componentsId = 10;
                    break;
                default:
                    break;
            }
        }
        return workflowDetailPoList;
    }

    /**
     * 组装nifi参数
     *
     * @param pipelineId   tb_nifi_custom_workflow表 id
     * @param workflowId   tb_nifi_custom_workflow表 workflowId
     * @param pipelineName tb_nifi_custom_workflow表 调度的管道名称作为nifi的pipelineName
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
        // TODO 封装nifi所有节点(大量改动)
        workListDTO.nifiCustomWorkDTOS = getNifiCustomWorkList(pipelineId, list);
        // TODO 管道详情-父子级tree,
        workListDTO.structure = getMenuTree(list);
        // 管道详情下的任务组-tree
        workListDTO.externalStructure = getMenuTree(workflowId, list);
        return workListDTO;
    }

    /**
     * 封装nifi所有节点
     *
     * @param list list
     * @param id   管道主键id
     * @return List<NifiCustomWorkDTO>
     */
    private List<NifiCustomWorkDTO> getNifiCustomWorkList(Long id, List<NifiCustomWorkflowDetailDTO> list) {
        List<NifiCustomWorkDTO> nifiCustomWorkDTOList = new ArrayList<>();
        /*list.stream().map(e -> {
            NifiCustomWorkDTO dto = new NifiCustomWorkDTO();
            // 只有调度组件有下一级,其他的不要上下级,余下的只传绑定有表的
            NifiCustomWorkflowDetailPO po = this.query().eq("id", e.id).one();
            if (Objects.equals(ChannelDataEnum.SCHEDULE_TASK.getName(), po.componentType)) {
                dto.NifiNode = getBuildNifiCustomWorkFlowDTO(NifiCustomWorkflowDetailMap.INSTANCES.poToDto(po));
                dto.outputDucts = getOutputDucts(po);
            } else if (Objects.equals(ChannelDataEnum.TASKGROUP.getName(), po.componentType)) {
                log.info("任务组不做处理");
            } else {
                NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPO = this.query().eq("pid", po.id).orderByAsc("table_order").list().get(0);
                dto.NifiNode = getBuildNifiCustomWorkFlowDTO(NifiCustomWorkflowDetailMap.INSTANCES.poToDto(nifiCustomWorkflowDetailPO));
            }


            return nifiCustomWorkDTOList.add(dto);
        }).collect(Collectors.toList());*/
        for (NifiCustomWorkflowDetailDTO e : list) {
            // 只有调度组件有下一级,其他的不要上下级,余下的只传绑定有表的
            NifiCustomWorkflowDetailPO po = this.query().eq("id", e.id).one();
            if (Objects.equals(ChannelDataEnum.SCHEDULE_TASK.getName(), po.componentType)) {
                NifiCustomWorkDTO dto = new NifiCustomWorkDTO();
                dto.NifiNode = getBuildNifiCustomWorkFlowDTO(NifiCustomWorkflowDetailMap.INSTANCES.poToDto(po));
                dto.outputDucts = getOutputDucts(po);
                if (dto.NifiNode != null) {
                    nifiCustomWorkDTOList.add(dto);
                }
            } else if (Objects.equals(ChannelDataEnum.TASKGROUP.getName(), po.componentType)) {
                log.info("任务组不做处理");
            } else {
                List<NifiCustomWorkflowDetailPO> detailPoList = this.query().eq("pid", po.id).orderByAsc("table_order").list();
                if (CollectionUtils.isNotEmpty(detailPoList)) {
                    for (NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPO : detailPoList) {
                        NifiCustomWorkDTO dto = new NifiCustomWorkDTO();
                        dto.NifiNode = getBuildNifiCustomWorkFlowDTO(NifiCustomWorkflowDetailMap.INSTANCES.poToDto(nifiCustomWorkflowDetailPO));
                        nifiCustomWorkDTOList.add(dto);
                        // 保存topic
                        TableTopicDTO topicDTO = new TableTopicDTO();
                        topicDTO.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();
                        topicDTO.topicName = MqConstants.TopicPrefix.TOPIC_PREFIX + id + "." + OlapTableEnum.PHYSICS_API.getValue() + "." + nifiCustomWorkflowDetailPO.appId + "." + nifiCustomWorkflowDetailPO.tableId;
                        topicDTO.tableType = OlapTableEnum.PHYSICS_API.getValue();
                        topicDTO.tableId = Integer.parseInt(dto.NifiNode.tableId);
                        topicDTO.componentId = Math.toIntExact(nifiCustomWorkflowDetailPO.id);
                        publishTaskClient.updateTableTopicByComponentId(topicDTO);
                    }
                }
//                NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPO = this.query().eq("pid", po.id).orderByAsc("table_order").list().get(0);
            }


        }

        return nifiCustomWorkDTOList;
    }

    /**
     * 封装输入节点(只有前端组件存在连线关系的,才适用这个方法)
     *
     * @param po po
     * @return list
     */
    private List<BuildNifiCustomWorkFlowDTO> getInputDucts(NifiCustomWorkflowDetailPO po) {

        String inport = po.inport;
        String[] inportIds = inport.split(",");
        // 确保当前inport没有删除
        List<BuildNifiCustomWorkFlowDTO> list = new ArrayList<>();
        for (String inportId : inportIds) {
            NifiCustomWorkflowDetailPO id = this.query().eq("id", inportId).one();
            if (id != null) {
                NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO = NifiCustomWorkflowDetailMap.INSTANCES.poToDto(id);
                BuildNifiCustomWorkFlowDTO buildNifiCustomWorkFlowDTO = getBuildNifiCustomWorkFlowDTO(nifiCustomWorkflowDetailDTO);
                list.add(buildNifiCustomWorkFlowDTO);
            }
        }
        return list;
    }

    /**
     * 封装输出节点(只有前端组件存在连线关系的,才适用这个方法)
     *
     * @param po po
     * @return list
     */
    private List<BuildNifiCustomWorkFlowDTO> getOutputDucts(NifiCustomWorkflowDetailPO po) {

        if (StringUtils.isBlank(po.outport)) {
            log.info("未找到下一级,{}", po.id);
            return new ArrayList<>();
        }

        String outport = po.outport;
        String[] outportIds = outport.split(",");
        List<BuildNifiCustomWorkFlowDTO> buildNifiCustomWorkFlows = new ArrayList<>();
        for (String id : outportIds) {
            NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPo = this.query().eq("id", id).one();
            if (!Objects.equals(nifiCustomWorkflowDetailPo.componentType, ChannelDataEnum.TASKGROUP)) {

                List<NifiCustomWorkflowDetailPO> list = this.query().eq("pid", id).orderByAsc("table_order").list();
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }
                NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPO = list.get(0);
                buildNifiCustomWorkFlows.add(getBuildNifiCustomWorkFlowDTO(NifiCustomWorkflowDetailMap.INSTANCES.poToDto(nifiCustomWorkflowDetailPO)));
            }

        }
        return buildNifiCustomWorkFlows;
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
        // 调用组装操作类型方法
        flow.type = getDataClassifyEnum(dto.componentType);
        // 调用表类型方法
        flow.tableType = getOlapTableEnum(dto.componentType);
        flow.tableId = dto.tableId;

        if (dto.pid == 0) {
            flow.groupId = this.query().eq("id", dto.id).one().workflowId;
        } else {
            flow.groupId = dto.pid.toString();
        }

        flow.workflowDetailId = dto.id;

        // 任务组时，appId即tb_nifi_custom_workflow_detail表id
//        if (taskGroupTpye.equalsIgnoreCase(dto.componentType)) {
        if (StringUtils.isNotBlank(dto.appId)) {
            flow.appId = Long.valueOf(dto.appId);
        }
//        }
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
     * TODO 获取操作类型(新版改动)
     *
     * @param componentType componentType
     * @return DataClassifyEnum
     */
    private DataClassifyEnum getDataClassifyEnum(String componentType) {

        ChannelDataEnum channelDataEnum = ChannelDataEnum.getValue(componentType);
        switch (Objects.requireNonNull(channelDataEnum)) {
            // 开始
            case SCHEDULE_TASK:
                return DataClassifyEnum.CUSTOMWORKSCHEDULINGCOMPONENT;
            // 任务组
            case TASKGROUP:
                return DataClassifyEnum.CUSTOMWORKSTRUCTURE;
            // 数据湖表任务
            case DATALAKE_TASK:
                // 数据湖ftp任务
            case DATALAKE_FTP_TASK:
                return DataClassifyEnum.CUSTOMWORKDATAACCESS;
            // 数据湖非实时api任务
            case DATALAKE_API_TASK:
                return DataClassifyEnum.DATAACCESS_API;
            // 数仓维度表任务
            case DW_DIMENSION_TASK:
                // 数仓事实表任务
            case DW_FACT_TASK:
                return DataClassifyEnum.CUSTOMWORKDATAMODELING;
            // 分析模型维度表任务
            case OLAP_DIMENSION_TASK:
                return DataClassifyEnum.CUSTOMWORKDATAMODELDIMENSIONKPL;
            // 分析模型事实表任务
            case OLAP_FACT_TASK:
                return DataClassifyEnum.CUSTOMWORKDATAMODELFACTKPL;
            // 分析模型宽表任务
            case OLAP_WIDETABLE_TASK:
                return DataClassifyEnum.DATAMODELWIDETABLE;
            case DW_TASK:
            case OLAP_TASK:

            default:
                break;
        }

        return null;
    }

    /**
     * 获取表类型
     *
     * @param componentType componentType
     * @return OlapTableEnum
     */
    private OlapTableEnum getOlapTableEnum(String componentType) {

        ChannelDataEnum channelDataEnum = ChannelDataEnum.getValue(componentType);
        switch (Objects.requireNonNull(channelDataEnum)) {
            // 开始
            case SCHEDULE_TASK:
                // 任务组
            case TASKGROUP:
                // 数仓表任务
            case DW_TASK:
                // 分析模型任务
            case OLAP_TASK:
                break;
            // 数据湖表任务
            case DATALAKE_TASK:
                // 数据湖ftp任务
            case DATALAKE_FTP_TASK:
                return OlapTableEnum.CUSTOMWORKPHYSICS;
            // 数据湖非实时api任务
            case DATALAKE_API_TASK:
                return OlapTableEnum.PHYSICS_API;
            // 数仓维度表任务
            case DW_DIMENSION_TASK:
                return OlapTableEnum.CUSTOMWORKDIMENSION;
            // 数仓事实表任务
            case DW_FACT_TASK:
                return OlapTableEnum.CUSTOMWORKFACT;
            // 分析模型维度表任务
            case OLAP_DIMENSION_TASK:
                return OlapTableEnum.CUSTOMWORKDIMENSIONKPI;
            // 分析模型事实表任务
            case OLAP_FACT_TASK:
                return OlapTableEnum.CUSTOMWORKFACTKPI;
            // 分析模型宽表任务
            case OLAP_WIDETABLE_TASK:
                return OlapTableEnum.WIDETABLE;

            default:
                break;
        }

        return null;
    }

    /**
     * 管道详情-父子级tree
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
        Map<Map, Map> structure = new HashMap<>(1000);
        for (NifiCustomWorkflowDetailDTO dto1 : collect1) {
            // 父
            Map<Long, String> structure1 = new HashMap<>(1000);
            // 子
            Map<Long, String> structure2 = new HashMap<>(1000);
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
     * @param list       tb_nifi_custom_workflow_detail表 list对象
     * @return map
     */
    private Map<Map, Map> getMenuTree(String workflowId, List<NifiCustomWorkflowDetailDTO> list) {
        String componentType = "任务组";
        NifiCustomWorkflowPO workflowPo = nifiCustomWorkflowImpl.query().eq("workflow_id", workflowId).one();
        List<NifiCustomWorkflowDetailDTO> collect = list.stream()
                .filter(item -> item.pid == 0 && componentType.equalsIgnoreCase(item.componentType))
                .collect(Collectors.toList());
        Map<Map, Map> structure = new HashMap<>(1000);
        Map structure1 = new HashMap(1000);
        structure1.put(workflowPo.workflowId, workflowPo.workflowName);

        Map<Long, String> map = new HashMap<>(1000);
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
        //删除topic_name
        List<Integer> ids = new ArrayList<>();
        ids.add(Math.toIntExact(id));
        publishTaskClient.deleteTableTopicByComponentId(ids);

        // TODO 修改inport&outport


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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum deleteDataList(WorkflowTaskGroupDTO dto) {
        try {
            List<NifiCustomWorkflowDetailDTO> dtoList = dto.list;
            if (CollectionUtils.isNotEmpty(dtoList)) {
                dtoList.forEach(e -> mapper.deleteByIdWithFill(NifiCustomWorkflowDetailMap.INSTANCES.dtoToPo(e)));
            }
        } catch (Exception e) {
            return ResultEnum.DELETE_TASK_GRUOP_ERROR;
        }
        return null;
    }

    @Override
    public List<ChannelDataDTO> getTableIds(NifiComponentsDTO dto) {

        ChannelDataEnum channelDataEnum = ChannelDataEnum.getName(Math.toIntExact(dto.id));

        switch (Objects.requireNonNull(channelDataEnum)) {
            // 数据湖非实时物理表任务
            case DATALAKE_TASK:
            case DATALAKE_FTP_TASK:
            case DATALAKE_API_TASK:
                ResultEntity<List<ChannelDataDTO>> result = dataAccessClient.getTableId();
                return result.data;
            // 数仓维度表任务
            case DW_DIMENSION_TASK:
                // 数仓事实表任务
            case DW_FACT_TASK:
                //分析模型维度表任务
            case OLAP_DIMENSION_TASK:
                // 分析模型事实表任务
            case OLAP_FACT_TASK:
                // 分析模型宽表任务
            case OLAP_WIDETABLE_TASK:
                ResultEntity<List<ChannelDataDTO>> resultEntity = dataModelClient.getTableId(dto);
                return resultEntity.data;
            default:
                break;
        }
        return null;
    }

    @Override
    public List<NifiCustomWorkflowDetailDTO> getComponentList(long id) {

        NifiCustomWorkflowDetailPO po = this.query().eq("id", id).one();
        if (po == null) {
            throw new FkException(ResultEnum.COMPONENT_NOT_EXISTS);
        }
        return NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(this.query().eq("pid", po.id).list());
    }
}
