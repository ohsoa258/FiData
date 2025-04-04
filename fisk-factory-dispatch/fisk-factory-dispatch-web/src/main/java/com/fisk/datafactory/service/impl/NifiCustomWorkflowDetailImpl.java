package com.fisk.datafactory.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.CronUtils;
import com.fisk.common.core.utils.Dto.cron.NextCronTimeDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.datasource.ExternalDataSourceDTO;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.datafactory.dto.check.CheckPhyDimFactTableIfExistsDTO;
import com.fisk.datafactory.dto.components.ChannelDataChildDTO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import com.fisk.datafactory.dto.components.NifiComponentsDTO;
import com.fisk.datafactory.dto.components.TableUsageDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflowdetail.*;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datafactory.map.NifiCustomWorkflowDetailMap;
import com.fisk.datafactory.map.NifiCustomWorkflowMap;
import com.fisk.datafactory.mapper.NifiCustomWorkflowDetailMapper;
import com.fisk.datafactory.service.INifiCustomWorkflow;
import com.fisk.datafactory.service.INifiCustomWorkflowDetail;
import com.fisk.datafactory.service.ITaskSetting;
import com.fisk.datafactory.utils.Base64ToMultipartFile;
import com.fisk.datafactory.vo.customworkflowdetail.KafkaConfigVO;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.mdm.client.MdmClient;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.BuildNifiCustomWorkFlowDTO;
import com.fisk.task.dto.task.NifiCustomWorkDTO;
import com.fisk.task.dto.task.NifiCustomWorkListDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    UserHelper userHelper;
    @Resource
    NifiCustomWorkflowImpl nifiCustomWorkflowImpl;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    DataModelClient dataModelClient;
    @Resource
    MdmClient mdmClient;
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    DataFactoryImpl dataFactoryImpl;
    @Resource
    private NifiCustomWorkflowDetailImpl nifiCustomWorkflowDetailImpl;

    @Resource
    UserClient userClient;
    @Resource
    ITaskSetting taskSetting;

    @Value("${pipeline-async-switch}")
    private Boolean pipelineAsyncSwitch;
    @Value("${kafkaBroker}")
    private String kafkaBroker;


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
            log.error("调度报错", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        // 保存
        return NifiCustomWorkflowDetailMap.INSTANCES.poToDto(baseMapper.selectById(model.id));

    }

    @Override
    public List<NifiCustomWorkflowDetailDTO> addDataList(List<NifiCustomWorkflowDetailDTO> list) {
        List<NifiCustomWorkflowDetailDTO> collect = list.stream().map(this::addData).collect(Collectors.toList());
        List<NifiCustomWorkflowDetailDTO> nifiCustomWorkflowDetailDtos = new ArrayList<>();
        collect.stream().filter(Objects::nonNull)
                .forEach(e -> {
                    NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDto = NifiCustomWorkflowDetailMap.INSTANCES.poToDto(mapper.selectById(e.id));
                    nifiCustomWorkflowDetailDtos.add(JSON.parseObject(JSON.toJSONString(nifiCustomWorkflowDetailDto), NifiCustomWorkflowDetailDTO.class));
                });
        return nifiCustomWorkflowDetailDtos;
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

    @Override
    public KafkaConfigVO getKafkaConfig(long id) {
        KafkaConfigVO kafkaConfigVO = new KafkaConfigVO();
        kafkaConfigVO.setKafkaBroker(kafkaBroker);
        kafkaConfigVO.setTopic(MqConstants.QueueConstants.BUILD_TASK_PUBLISH_FLOW);
        kafkaConfigVO.setValue("{\"topic\": \"dmp.datafactory.nifi.\""+id+", \"topicType\": \"2\"}");
        return kafkaConfigVO;
    }

    @Transactional(rollbackFor = Exception.class)
//    @Override
    @Deprecated
    public ResultEntity<NifiCustomWorkListDTO> editData1(NifiCustomWorkflowDetailVO dto) {
        //这类任务天生只有父级,所以需要我们每次生成子集,所在在生成子集之前需要先把原来的子集删掉
        deleteOldTask(dto);

        String componentType = "触发器";

        // 修改tb_nifi_custom_wokflow
        NifiCustomWorkflowDTO workflowDTO = dto.dto;
        if (workflowDTO == null) {
            return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL);
        }
        try {
            NifiCustomWorkflowPO one = workflowService.query().eq("id", dto.dto.id).one();
            workflowDTO.workStatus = one.workStatus;
            if (!dto.flag) {
                // 正在发布
                workflowDTO.status = 3;
            }
            workflowService.editData(workflowDTO);
        } catch (Exception e) {
            log.error("修改管道报错", e);
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        List<NifiCustomWorkflowDetailPO> list = NifiCustomWorkflowDetailMap.INSTANCES.listDtoToPo(dto.list);

        // 判断开始组件是否有调度参数(用于确保开始组件的调度参数不为空)
        List<NifiCustomWorkflowDetailPO> start = list.stream().filter(e -> componentType.equalsIgnoreCase(e.componentType)).collect(Collectors.toList());
        for (NifiCustomWorkflowDetailPO e : start) {
            if (e.schedule == null || e.script == null || "".equals(e.script)) {
                return ResultEntityBuild.build(ResultEnum.SCHEDULE_PARAME_NULL);
            }
            if (Objects.equals(e.schedule, SchedulingStrategyTypeEnum.CRON) && !CronUtils.isValidExpression(e.script)) {
                return ResultEntityBuild.build(ResultEnum.CRON_ERROR);
            }
        }
        //去除空workflowId
        if (CollectionUtils.isNotEmpty(start)) {
            String workflowId = start.get(0).workflowId;
            list.stream().filter(Objects::nonNull).forEach(e -> {
                if (StringUtils.isEmpty(e.workflowId)) {
                    e.workflowId = workflowId;
                }
            });
        }
        // 批量保存tb_nifi_custom_wokflow_detail
        boolean success = this.saveOrUpdateBatch(list);
        for (NifiCustomWorkflowDetailDTO detail : dto.list) {
            taskSetting.updateTaskSetting(detail.id, detail.taskSetting);
        }
        if (!success) {
            log.error("修改管道报错2");
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }


        return getNifiCustomWorkListDTOResultEntity(dto, workflowDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEntity<NifiCustomWorkListDTO> editData(NifiCustomWorkflowDetailVO dto) {
        LambdaQueryWrapper<NifiCustomWorkflowDetailPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NifiCustomWorkflowDetailPO::getWorkflowId, dto.dto.workflowId);
        mapper.delete(queryWrapper);

        String componentType = "触发器";

        // 修改tb_nifi_custom_wokflow
        NifiCustomWorkflowDTO workflowDTO = dto.dto;
        if (workflowDTO == null) {
            return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL);
        }
        try {
            NifiCustomWorkflowPO one = workflowService.query().eq("id", dto.dto.id).one();
            workflowDTO.workStatus = one.workStatus;
            if (!dto.flag) {
                // 正在发布
                workflowDTO.status = 3;
            }
            workflowService.editData(workflowDTO);
        } catch (Exception e) {
            log.error("修改管道报错", e);
            return ResultEntityBuild.build(ResultEnum.SAVE_DATA_ERROR);
        }

        List<NifiCustomWorkflowDetailPO> list = NifiCustomWorkflowDetailMap.INSTANCES.listDtoToPo(dto.list);

        // 判断开始组件是否有调度参数(用于确保开始组件的调度参数不为空)
        List<NifiCustomWorkflowDetailPO> start = list.stream().filter(e -> componentType.equalsIgnoreCase(e.componentType)).collect(Collectors.toList());
        for (NifiCustomWorkflowDetailPO e : start) {
            if (e.schedule == null || e.script == null || "".equals(e.script)) {
                return ResultEntityBuild.build(ResultEnum.SCHEDULE_PARAME_NULL);
            }
            if (Objects.equals(e.schedule, SchedulingStrategyTypeEnum.CRON) && !CronUtils.isValidExpression(e.script)) {
                return ResultEntityBuild.build(ResultEnum.CRON_ERROR);
            }
        }
        List<NifiCustomWorkflowDetailDTO> WorkflowDetailDTOS = new ArrayList<>();
        List<ChildrenWorkflowDetailDTO> childrenWorkflowDetailDTOS = new ArrayList<>();
        Map<String, Long> map = new HashMap<>();
        List<NifiCustomWorkflowDetailDTO> collect = dto.list.stream().map(i -> {
            i.setPid(0L);
            i.setDelFlag(1);
            NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPO = NifiCustomWorkflowDetailMap.INSTANCES.dtoToPo(i);
            if (nifiCustomWorkflowDetailPO.id != 0) {
                nifiCustomWorkflowDetailPO.setUpdateTime(LocalDateTime.now());
                nifiCustomWorkflowDetailPO.setUpdateUser(userHelper.getLoginUserInfo().id.toString());
                this.mapper.updateByDetailId(nifiCustomWorkflowDetailPO);
            } else {
                this.save(nifiCustomWorkflowDetailPO);
            }
            if (!StringUtils.isEmpty(i.uuid)) {
                map.put(i.uuid, nifiCustomWorkflowDetailPO.id);
                i.setId(nifiCustomWorkflowDetailPO.id);
            }
            return i;
        }).collect(Collectors.toList());
        for (NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO : collect) {
            if (nifiCustomWorkflowDetailDTO.componentType.equals(ChannelDataEnum.SFTP_FILE_COPY_TASK.getName())) {
                Map<String, String> taskSettingMap = nifiCustomWorkflowDetailDTO.getTaskSetting();
                String sourceFile = taskSettingMap.get("sourceFile");
                saveSftpFile(dto, nifiCustomWorkflowDetailDTO, sourceFile, sourceFile);
                String targetFile = taskSettingMap.get("targetFile");
                saveSftpFile(dto, nifiCustomWorkflowDetailDTO, sourceFile, targetFile);
                taskSettingMap.remove("sourceFile");
                taskSettingMap.remove("targetFile");
                nifiCustomWorkflowDetailDTO.setTaskSetting(taskSettingMap);
            }
            taskSetting.updateTaskSetting(nifiCustomWorkflowDetailDTO.id, nifiCustomWorkflowDetailDTO.taskSetting);
            if (!StringUtils.isEmpty(nifiCustomWorkflowDetailDTO.inportUuid)) {
                List<String> inport = new ArrayList<>();
                String[] InportUuid = nifiCustomWorkflowDetailDTO.inportUuid.split(",");
                for (String s : InportUuid) {
                    Long aLong = map.get(s);
                    if (aLong != null) {
                        inport.add(aLong.toString());
                    } else {
                        inport.add(s);
                    }
                }
                nifiCustomWorkflowDetailDTO.setInport(StringUtils.join(inport, ","));
            }
            if (!StringUtils.isEmpty(nifiCustomWorkflowDetailDTO.outportUuid)) {
                List<String> outport = new ArrayList<>();
                String[] OutportUuid = nifiCustomWorkflowDetailDTO.outportUuid.split(",");
                for (String s : OutportUuid) {
                    Long aLong = map.get(s);
                    if (aLong != null) {
                        outport.add(aLong.toString());
                    } else {
                        outport.add(s);
                    }
                }
                nifiCustomWorkflowDetailDTO.setOutport(StringUtils.join(outport, ","));
            }
            if (CollectionUtils.isNotEmpty(nifiCustomWorkflowDetailDTO.childrenWorkflowDetailList)) {
                for (ChildrenWorkflowDetailDTO childrenWorkflowDetailDTO : nifiCustomWorkflowDetailDTO.childrenWorkflowDetailList) {
                    childrenWorkflowDetailDTO.setPid(nifiCustomWorkflowDetailDTO.id);
                    childrenWorkflowDetailDTO.setInport(nifiCustomWorkflowDetailDTO.inport);
                    childrenWorkflowDetailDTO.setOutport(nifiCustomWorkflowDetailDTO.outport);
                    childrenWorkflowDetailDTOS.add(childrenWorkflowDetailDTO);

                }
            }
            WorkflowDetailDTOS.add(nifiCustomWorkflowDetailDTO);
        }

        List<NifiCustomWorkflowDetailPO> workflowDetailList = NifiCustomWorkflowDetailMap.INSTANCES.listDtoToPo(WorkflowDetailDTOS);

        List<NifiCustomWorkflowDetailPO> childrenList = NifiCustomWorkflowDetailMap.INSTANCES.listChildrenDtoToPo(childrenWorkflowDetailDTOS);

        this.updateBatchById(workflowDetailList);
        this.saveOrUpdateBatch(childrenList);

        return getNifiCustomWorkListDTOResultEntity(dto, workflowDTO);
    }

    private ResultEntity<NifiCustomWorkListDTO> getNifiCustomWorkListDTOResultEntity(NifiCustomWorkflowDetailVO dto, NifiCustomWorkflowDTO workflowDTO) {
        NifiCustomWorkListDTO workListDTO = new NifiCustomWorkListDTO();

        if (dto.flag) {
            // 前端有时会传入已经删除的组件,后端使用入库后的数据
            List<NifiCustomWorkflowDetailPO> originalWorkflowDetailPoList = this.query().eq("workflow_id", workflowDTO.workflowId).eq("del_flag", 1).list();

            // 重新组装参数
            List<NifiCustomWorkflowDetailPO> workflowDetailPoList = buildWorkflowDetailPoList(originalWorkflowDetailPoList);

            // 给nifi封装参数
            if (CollectionUtils.isNotEmpty(workflowDetailPoList)) {
                List<NifiCustomWorkflowDetailDTO> workflowDetailDTOList = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(workflowDetailPoList);
                // 调用组装nifi参数方法
                workListDTO = getWorkListDTO(workflowDTO.id, workflowDTO.workflowId, workflowDTO.workflowName, workflowDetailDTOList);
                workListDTO.workStatus = workflowDTO.workStatus;
            }
            // 将当前管道的task结构存入redis
            log.info("即将要存入redis的管道id" + workflowDTO.id);
            dataFactoryImpl.setTaskLinkedList(workflowDTO.id);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, workListDTO);
    }

    private void saveSftpFile(NifiCustomWorkflowDetailVO dto, NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetailDTO, String sourceFile, String targetFile) {
        if (!StringUtils.isEmpty(targetFile)) {
            final String[] base64Array = sourceFile.split(",");
            String dataUir, data;
            if (base64Array.length > 1) {
                dataUir = base64Array[0];
                data = base64Array[1];
            } else {
                //根据你base64代表的具体文件构建
                dataUir = "data:application/octet-stream;base64";
                data = base64Array[0];
            }
            MultipartFile multipartFile = new Base64ToMultipartFile(data, dataUir);
            taskSetting.uploadSecretKeyFile((int) dto.dto.id, (int) nifiCustomWorkflowDetailDTO.id, multipartFile, 0);
        }
    }

    public void deleteOldTask(NifiCustomWorkflowDetailVO dto) {

        List<NifiCustomWorkflowDetailDTO> collect =
                dto.list.stream().filter(e -> (e.componentType.equals(ChannelDataEnum.CUSTOMIZE_SCRIPT_TASK.getName()) ||
                        e.componentType.equals(ChannelDataEnum.SFTP_FILE_COPY_TASK.getName()) ||
                        e.componentType.equals(ChannelDataEnum.POWERBI_DATA_SET_REFRESH_TASK.getName())) && e.pid == 0).collect(Collectors.toList());
        String workflowId = "";
        if (CollectionUtils.isNotEmpty(collect)) {
            for (NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetail : collect) {
                List<NifiCustomWorkflowDetailPO> original = this.query().eq("pid", nifiCustomWorkflowDetail.id).list();
                if (CollectionUtils.isNotEmpty(original)) {
                    this.removeByIds(original.stream().map(d -> d.id).collect(Collectors.toList()));
                    //因为有时候给pid是空的,所以在下面还要删一下
                    workflowId = nifiCustomWorkflowDetail.workflowId;
                }
            }
            //删除跳过pid!=0,workflowId = workflowId,类型=ChannelDataEnum.CUSTOMIZE_SCRIPT_TASK.getName()
            NifiCustomWorkflowDetailPO nifiCustomWorkflowDetail = new NifiCustomWorkflowDetailPO();
            nifiCustomWorkflowDetail.workflowId = workflowId;
            nifiCustomWorkflowDetail.componentType = ChannelDataEnum.CUSTOMIZE_SCRIPT_TASK.getName();
            mapper.deleteByType(nifiCustomWorkflowDetail);
            nifiCustomWorkflowDetail.componentType = ChannelDataEnum.SFTP_FILE_COPY_TASK.getName();
            mapper.deleteByType(nifiCustomWorkflowDetail);
            nifiCustomWorkflowDetail.componentType = ChannelDataEnum.POWERBI_DATA_SET_REFRESH_TASK.getName();
            mapper.deleteByType(nifiCustomWorkflowDetail);
        }
    }

    /**
     * 重新组装job和task的componentsId
     *
     * @param workflowDetailPoList 所有的job和task
     * @return java.util.List<com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO>
     * @author Lock
     * @date 2022/5/7 14:31
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
                // 数据湖非实时api任务
                case CUSTOMIZE_SCRIPT_TASK:
                    e.componentsId = 13;
                    break;
                case SFTP_FILE_COPY_TASK:
                    e.componentsId = 14;
                    break;
                case MDM_TABLE_TASK:
                    e.componentsId = 16;
                    break;
                case POWERBI_DATA_SET_REFRESH_TASK:
                    e.componentsId = 15;
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
     * @param id   节点主键id,tb_nifi_
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
            }
//            else if (Objects.equals(ChannelDataEnum.TASKGROUP.getName(), po.componentType)) {
//                log.info("任务组不做处理");
//            }
            else {
                List<NifiCustomWorkflowDetailPO> detailPoList = new ArrayList<>();
                detailPoList = this.query().eq("pid", po.id).orderByAsc("table_order").list();

                if (CollectionUtils.isNotEmpty(detailPoList)) {
                    for (NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPo : detailPoList) {
                        NifiCustomWorkDTO dto = new NifiCustomWorkDTO();
                        dto.NifiNode = getBuildNifiCustomWorkFlowDTO(NifiCustomWorkflowDetailMap.INSTANCES.poToDto(nifiCustomWorkflowDetailPo));
                        nifiCustomWorkDTOList.add(dto);
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
                //提取变量
                List<NifiCustomWorkflowDetailPO> list = new ArrayList<>();
                list = this.query().eq("pid", id).orderByAsc("table_order").list();
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }
                if (pipelineAsyncSwitch) {
                    for (NifiCustomWorkflowDetailPO workflowDetailPo : list) {
                        if (Objects.equals(workflowDetailPo.componentType, ChannelDataEnum.DATALAKE_API_TASK)) {
                            buildNifiCustomWorkFlows.add(getBuildNifiCustomWorkFlowDTO(NifiCustomWorkflowDetailMap.INSTANCES.poToDto(workflowDetailPo)));
                        }
                    }
                    if (Objects.equals(list.get(0).componentType, ChannelDataEnum.DATALAKE_API_TASK)) {
                        buildNifiCustomWorkFlows.remove(0);
                    }
                    NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPO2 = list.get(0);
                    buildNifiCustomWorkFlows.add(getBuildNifiCustomWorkFlowDTO(NifiCustomWorkflowDetailMap.INSTANCES.poToDto(nifiCustomWorkflowDetailPO2)));
                } else {
                    NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPO2 = list.get(0);
                    buildNifiCustomWorkFlows.add(getBuildNifiCustomWorkFlowDTO(NifiCustomWorkflowDetailMap.INSTANCES.poToDto(nifiCustomWorkflowDetailPO2)));
                }

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

        String scheduleType = "触发器";
        //  String taskGroupTpye = "任务组";
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
     * 获取操作类型(新版改动)
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
            case CUSTOMIZE_SCRIPT_TASK:
                return DataClassifyEnum.CUSTOMWORKCUSTOMIZESCRIPT;
            case SFTP_FILE_COPY_TASK:
                return DataClassifyEnum.SFTPFILECOPYTASK;
            case POWERBI_DATA_SET_REFRESH_TASK:
                return DataClassifyEnum.POWERBIDATASETREFRESHTASK;
            case MDM_TABLE_TASK:
                return DataClassifyEnum.MDM_DATA_ACCESS;
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
            //主数据表任务
            case MDM_TABLE_TASK:
                return OlapTableEnum.MDM_DATA_ACCESS;
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
            case SFTP_FILE_COPY_TASK:
                return OlapTableEnum.SFTPFILECOPYTASK;
            case POWERBI_DATA_SET_REFRESH_TASK:
                return OlapTableEnum.POWERBIDATASETREFRESHTASK;

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
        log.info("NifiCustomWorkflowDetailDTO参数:{}", JSON.toJSONString(dto));
        NifiCustomWorkflowDetailPO model = this.getById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        log.info("model参数:{}", JSON.toJSONString(model));
        //校验cron格式是否正确
        //{"componentType":"数据湖表任务","createTime":"2022-11-09T12:07:51","createUser":"60","delFlag":1,"id":2834,"workflowId":"5f663512-0800-4481-8c59-0a0e5ef114b8"}
        if (Objects.equals(model.schedule, SchedulingStrategyTypeEnum.CRON.getValue()) && !StringUtils.isEmpty(dto.script)) {
            if (!CronUtils.isValidExpression(dto.script)) {
                throw new FkException(ResultEnum.CRON_ERROR);
            }
        }
        if (CollectionUtils.isNotEmpty(dto.taskSetting)) {
            //修改sftp组件的父子级配置,旨在解决修改一个组件就要发布整个管道的
            //if (Objects.equals(dto.componentsId, ChannelDataEnum.SFTP_FILE_COPY_TASK.getValue())) {
            taskSetting.updateTaskSetting(dto.id, dto.taskSetting);
            NifiCustomWorkflowDetailPO nifiCustomWorkflowDetailPo = this.query().eq("pid", dto.id).one();
            if (Objects.nonNull(nifiCustomWorkflowDetailPo)) {
                taskSetting.updateTaskSetting(nifiCustomWorkflowDetailPo.id, dto.taskSetting);
            }
            //}
        }

        // dto -> po
        NifiCustomWorkflowDetailPO po = NifiCustomWorkflowDetailMap.INSTANCES.dtoToPo(dto);
        // 执行修改
        boolean dtoSuccecc = this.updateById(po);
        log.info("是否修改成功{}", dtoSuccecc);
        return dtoSuccecc ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public ResultEnum deleteDataList(WorkflowTaskGroupDTO dto) {
//        try {
//
//            List<NifiCustomWorkflowDetailDTO> dtoList = dto.list;
//            if (CollectionUtils.isNotEmpty(dtoList)) {
//                //获取节点本身
//                NifiCustomWorkflowDetailPO one = this.query().eq("id", dtoList.get(0).id).one();
//                //获取管道id
//                String workflowId = one.workflowId;
//                NifiCustomWorkflowPO workflow = workflowService.query().eq("workflow_id", workflowId).one();
//                long id = workflow.id;
//                List<TableTopicDTO> topicDtos = new ArrayList<>();
//                for (NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetail : dtoList) {
//                    if (StringUtils.isNotEmpty(nifiCustomWorkflowDetail.tableId)) {
//                        TableTopicDTO topicDto = new TableTopicDTO();
//                        topicDto.tableId = Integer.parseInt(nifiCustomWorkflowDetail.tableId);
//                        topicDto.topicType = TopicTypeEnum.COMPONENT_NIFI_FLOW.getValue();
//                        ChannelDataEnum value = ChannelDataEnum.getValue(nifiCustomWorkflowDetail.componentType);
//                        OlapTableEnum olapTableEnum = ChannelDataEnum.getOlapTableEnum(value.getValue());
//                        topicDto.tableType = olapTableEnum.getValue();
//                        topicDto.topicName = MqConstants.TopicPrefix.TOPIC_PREFIX + "." + id;
//                        topicDtos.add(topicDto);
//                    }
//                }
//                log.info("删除组件的topic组装参数:" + JSON.toJSONString(topicDtos));
//                publishTaskClient.deleteTableTopicGroup(topicDtos);
//                //先删除组件配置 tb_task_setting表的数据
//                dtoList.forEach(e -> taskSetting.deleteByTaskId(e.id));
//                //再删除（逻辑删除）tb_nifi_custom_workflow_detail表的数据
//                dtoList.forEach(e -> mapper.deleteByIdWithFill(NifiCustomWorkflowDetailMap.INSTANCES.dtoToPo(e)));
//            }
//        } catch (Exception e) {
//            return ResultEnum.DELETE_TASK_GRUOP_ERROR;
//        }
//        return null;
//    }

    /**
     * 2023-05-24 lsj  该管道删除方法目前不再调用task模块处理tb_table_topic表的主题了
     *
     * @param dto dto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum deleteDataList(WorkflowTaskGroupDTO dto) {
        try {
            List<NifiCustomWorkflowDetailDTO> dtoList = dto.list;
            if (CollectionUtils.isNotEmpty(dtoList)) {
                //获取节点本身
                NifiCustomWorkflowDetailPO one = this.query().eq("id", dtoList.get(0).id).one();

                //先删除组件配置 tb_task_setting表的数据
                dtoList.forEach(e -> taskSetting.deleteByTaskId(e.id));
                //再删除（逻辑删除）tb_nifi_custom_workflow_detail表的数据
                dtoList.forEach(e -> mapper.deleteByIdWithFill(NifiCustomWorkflowDetailMap.INSTANCES.dtoToPo(e)));
                //再删除（逻辑删除）节点下面挂载的任务
                UpdateWrapper<NifiCustomWorkflowDetailPO> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("pid", one.id).set("del_flag", 0);
                nifiCustomWorkflowDetailImpl.update(updateWrapper);
            }
        } catch (Exception e) {
            return ResultEnum.DELETE_TASK_GRUOP_ERROR;
        }
        return null;
    }

    @Override
    public List<ChannelDataDTO> getTableIds(NifiComponentsDTO dto) {
        List<ChannelDataDTO> list = new ArrayList<>();
        ChannelDataEnum channelDataEnum = ChannelDataEnum.getName(Math.toIntExact(dto.id));

        switch (Objects.requireNonNull(channelDataEnum)) {
            // 数据湖非实时物理表任务
            case DATALAKE_TASK:
            case DATALAKE_FTP_TASK:
            case DATALAKE_API_TASK:
                ResultEntity<List<ChannelDataDTO>> result = dataAccessClient.getTableId();
                list = result.data;
                log.info("数据湖非实时物理表任务" + list.toString());
                break;
            // 数仓维度表任务
            case DW_DIMENSION_TASK:
                // 数仓事实表任务
            case DW_FACT_TASK:
                List<ChannelDataDTO> dwDimensionList = new ArrayList<>();
                List<ChannelDataDTO> dwFactList = new ArrayList<>();
                dto.id = ChannelDataEnum.DW_DIMENSION_TASK.getValue();
                ResultEntity<List<ChannelDataDTO>> dwDimension = dataModelClient.getTableId(dto);
                if (dwDimension.data != null) {
                    dwDimensionList = dwDimension.data;
                }
                dto.id = ChannelDataEnum.DW_FACT_TASK.getValue();
                ResultEntity<List<ChannelDataDTO>> dwFact = dataModelClient.getTableId(dto);
                if (dwFact.data != null) {
                    dwFactList = dwFact.data;
                }
                // 创建空的结果列表
                // 同步循环遍历两个列表
                for (int i = 0; i < dwDimensionList.size(); i++) {
                    ChannelDataDTO a = dwDimensionList.get(i);
                    ChannelDataDTO b = dwFactList.get(i);
                    a.getList().addAll(b.getList());
                    list.add(a);
                }
                break;
            //分析模型维度表任务
            case OLAP_DIMENSION_TASK:
                // 分析模型事实表任务
            case OLAP_FACT_TASK:
                // 分析模型宽表任务
            case OLAP_WIDETABLE_TASK:
                dto.id = ChannelDataEnum.OLAP_DIMENSION_TASK.getValue();
                ResultEntity<List<ChannelDataDTO>> olapDimension = dataModelClient.getTableId(dto);
                if (olapDimension.data != null) {
                    list.addAll(olapDimension.data);
                }
                dto.id = ChannelDataEnum.OLAP_FACT_TASK.getValue();
                ResultEntity<List<ChannelDataDTO>> olapFact = dataModelClient.getTableId(dto);
                if (olapFact.data != null) {
                    list.addAll(olapFact.data);
                }
                dto.id = ChannelDataEnum.OLAP_WIDETABLE_TASK.getValue();
                ResultEntity<List<ChannelDataDTO>> olapWideTable = dataModelClient.getTableId(dto);
                if (olapWideTable.data != null) {
                    list.addAll(olapWideTable.data);
                }
                break;
            //主数据表任务
            case MDM_TABLE_TASK:
                ResultEntity<List<ChannelDataDTO>> mdmData = mdmClient.getTableId();
                list = mdmData.data;
                break;
            default:
                break;
        }
        //查出表被哪个管道哪个任务用
//        getTableUsage(list);
        return list;
    }

    @Override
    public List<ChannelDataChildDTO> getTableList(ChannelDataDTO dto) {
        String type = dto.type;
        if (CollectionUtils.isNotEmpty(dto.list)) {
            return dto.list.stream().filter(Objects::nonNull)
                    .map(v -> {
                        //组装数据
                        if (v.id != 0L) {

                            ChannelDataEnum channelDataEnum = ChannelDataEnum.getValue(type);
                            switch (Objects.requireNonNull(channelDataEnum)) {
                                // 数据湖非实时物理表任务
                                case DATALAKE_TASK:
                                    v.setSourceId(DataSourceConfigEnum.DMP_ODS.getValue());
                                    v.setTableBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                    v.setTableType(ChannelDataEnum.DATALAKE_TASK.getValue());
                                    v.setComponentType(ChannelDataEnum.DATALAKE_TASK.getName());
                                    break;
                                case DATALAKE_FTP_TASK:
                                    v.setSourceId(DataSourceConfigEnum.DMP_ODS.getValue());
                                    v.setTableBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                    v.setTableType(ChannelDataEnum.DATALAKE_FTP_TASK.getValue());
                                    v.setComponentType(ChannelDataEnum.DATALAKE_FTP_TASK.getName());
                                    break;
                                case DATALAKE_API_TASK:
                                    v.setSourceId(DataSourceConfigEnum.DMP_ODS.getValue());
                                    v.setTableBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                    v.setTableType(ChannelDataEnum.DATALAKE_API_TASK.getValue());
                                    v.setComponentType(ChannelDataEnum.DATALAKE_API_TASK.getName());
                                    break;
                                // 数仓维度表任务
                                case DW_DIMENSION_TASK:
                                    // 数仓事实表任务
                                case DW_FACT_TASK:
                                    v.setSourceId(DataSourceConfigEnum.DMP_DW.getValue());
                                    if (v.getTableBusinessType() == TableBusinessTypeEnum.DW_DIMENSION.getValue()) {
                                        v.setTableType(ChannelDataEnum.DW_DIMENSION_TASK.getValue());
                                        v.setComponentType(ChannelDataEnum.DW_DIMENSION_TASK.getName());
                                    } else if (v.getTableBusinessType() == TableBusinessTypeEnum.DW_FACT.getValue()) {
                                        v.setTableType(ChannelDataEnum.DW_FACT_TASK.getValue());
                                        v.setComponentType(ChannelDataEnum.DW_FACT_TASK.getName());
                                    }
                                    break;
                                // 主数据表任务
                                case MDM_TABLE_TASK:
                                    v.setSourceId(DataSourceConfigEnum.DMP_ODS.getValue());
                                    v.setTableBusinessType(TableBusinessTypeEnum.ENTITY_TABLR.getValue());
                                    v.setTableType(ChannelDataEnum.MDM_TABLE_TASK.getValue());
                                    v.setComponentType(ChannelDataEnum.MDM_TABLE_TASK.getName());
                                    break;
                                default:
                                    break;
                            }

                            List<NifiCustomWorkflowDetailPO> nifiCustomWorkflowDetailPos = this.query().eq("component_type", type).eq("table_id", v.id).eq("table_type", v.tableType).list();
                            List<TableUsageDTO> tableUsageDtos = new ArrayList<>();
                            if (CollectionUtils.isNotEmpty(nifiCustomWorkflowDetailPos)) {
                                nifiCustomWorkflowDetailPos.stream().filter(Objects::nonNull)
                                        .forEach(a -> {
                                            String workflowId = a.workflowId;
                                            NifiCustomWorkflowPO nifiCustomWorkflowPo = nifiCustomWorkflowImpl.query().eq("workflow_id", workflowId).one();
                                            TableUsageDTO tableUsage = new TableUsageDTO();
                                            tableUsage.jobId = a.pid;
                                            NifiCustomWorkflowDetailPO one = this.query().eq("id", a.pid).one();
                                            log.info(a.pid + "==============================" + JSON.toJSONString(one));
                                            if (Objects.isNull(one)) {
                                                return;
                                            }
                                            tableUsage.jobName = one.componentName;
                                            tableUsage.pipelId = nifiCustomWorkflowPo.id;
                                            tableUsage.pipelName = nifiCustomWorkflowPo.workflowName;
                                            tableUsage.taskId = a.id;
                                            tableUsage.taskName = a.componentName;
                                            tableUsage.tableOrder = a.tableOrder;
                                            tableUsageDtos.add(tableUsage);
                                        });
                            }
                            v.tableUsages = tableUsageDtos;
                        }
                        return v;
                    }).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private void getTableUsage(List<ChannelDataDTO> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.stream().filter(Objects::nonNull)
                    .forEach(e -> {
                        log.info("----------------------" + JSON.toJSONString(e));
                        String type = e.type;
                        if (CollectionUtils.isNotEmpty(e.list)) {
                            e.list.stream().filter(Objects::nonNull)
                                    .forEach(v -> {
                                        //组装数据
                                        if (v.id != 0L) {

                                            ChannelDataEnum channelDataEnum = ChannelDataEnum.getValue(type);
                                            switch (Objects.requireNonNull(channelDataEnum)) {
                                                // 数据湖非实时物理表任务
                                                case DATALAKE_TASK:
                                                    v.setSourceId(DataSourceConfigEnum.DMP_ODS.getValue());
                                                    v.setTableBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                                    v.setTableType(ChannelDataEnum.DATALAKE_TASK.getValue());
                                                    break;
                                                case DATALAKE_FTP_TASK:
                                                    v.setSourceId(DataSourceConfigEnum.DMP_ODS.getValue());
                                                    v.setTableBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                                    v.setTableType(ChannelDataEnum.DATALAKE_FTP_TASK.getValue());
                                                    break;
                                                case DATALAKE_API_TASK:
                                                    v.setSourceId(DataSourceConfigEnum.DMP_ODS.getValue());
                                                    v.setTableBusinessType(TableBusinessTypeEnum.NONE.getValue());
                                                    v.setTableType(ChannelDataEnum.DATALAKE_API_TASK.getValue());
                                                    break;
                                                // 数仓维度表任务
                                                case DW_DIMENSION_TASK:
                                                    v.setSourceId(DataSourceConfigEnum.DMP_DW.getValue());
                                                    v.setTableBusinessType(TableBusinessTypeEnum.DW_DIMENSION.getValue());
                                                    v.setTableType(ChannelDataEnum.DW_DIMENSION_TASK.getValue());
                                                    break;
                                                // 数仓事实表任务
                                                case DW_FACT_TASK:
                                                    v.setSourceId(DataSourceConfigEnum.DMP_DW.getValue());
                                                    v.setTableBusinessType(TableBusinessTypeEnum.DW_FACT.getValue());
                                                    v.setTableType(ChannelDataEnum.DW_FACT_TASK.getValue());
                                                    break;
                                                // 主数据表任务
                                                case MDM_TABLE_TASK:
                                                    v.setSourceId(DataSourceConfigEnum.DMP_ODS.getValue());
                                                    v.setTableBusinessType(TableBusinessTypeEnum.ENTITY_TABLR.getValue());
                                                    v.setTableType(ChannelDataEnum.MDM_TABLE_TASK.getValue());
                                                    break;
                                                default:
                                                    break;
                                            }

                                            List<NifiCustomWorkflowDetailPO> nifiCustomWorkflowDetailPos = this.query().eq("component_type", type).eq("table_id", v.id).list();
                                            List<TableUsageDTO> tableUsageDtos = new ArrayList<>();
                                            if (CollectionUtils.isNotEmpty(nifiCustomWorkflowDetailPos)) {
                                                nifiCustomWorkflowDetailPos.stream().filter(Objects::nonNull)
                                                        .forEach(a -> {
                                                            String workflowId = a.workflowId;
                                                            NifiCustomWorkflowPO nifiCustomWorkflowPo = nifiCustomWorkflowImpl.query().eq("workflow_id", workflowId).one();
                                                            TableUsageDTO tableUsage = new TableUsageDTO();
                                                            tableUsage.jobId = a.pid;
                                                            NifiCustomWorkflowDetailPO one = this.query().eq("id", a.pid).one();
                                                            log.info(a.pid + "==============================" + JSON.toJSONString(one));
                                                            if (Objects.isNull(one)) {
                                                                return;
                                                            }
                                                            tableUsage.jobName = one.componentName;
                                                            tableUsage.pipelId = nifiCustomWorkflowPo.id;
                                                            tableUsage.pipelName = nifiCustomWorkflowPo.workflowName;
                                                            tableUsage.taskId = a.id;
                                                            tableUsage.taskName = a.componentName;
                                                            tableUsage.tableOrder = a.tableOrder;
                                                            tableUsageDtos.add(tableUsage);
                                                        });
                                            }
                                            v.tableUsages = tableUsageDtos;
                                        }
                                    });
                        }
                    });
        }
    }

    @Override
    public List<NifiCustomWorkflowDetailDTO> getComponentList(long id) {

        NifiCustomWorkflowDetailPO po = this.query().eq("id", id).one();
        if (po == null) {
            throw new FkException(ResultEnum.COMPONENT_NOT_EXISTS);
        }
        return NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(this.query().eq("pid", po.id).list());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum editDataByDeleteTable(List<DeleteTableDetailDTO> list) {
        log.info("删除表对应删除管道任务方法:" + JSON.toJSONString(list));
        list.stream()
                .filter(Objects::nonNull)
                .forEach(e ->
                        // 查询出有多少条这样的task任务
                        this.query()
                                .eq("app_id", e.appId)
                                .eq("table_id", e.tableId)
                                .eq("component_type", e.channelDataEnum.getName())
                                .list()
                                .stream()
                                .filter(Objects::nonNull)
                                .forEach(po -> {
                                    // 删除当前task
                                    this.deleteData(po.id);
                                    // 查询出当前task属于哪个job
                                    NifiCustomWorkflowDetailPO job = this.query().eq("id", po.pid).one();
                                    // 获取当前job的所有task,过滤出tableOrder > po.tableOrder的
                                    this.query()
                                            .eq("pid", job.id)
                                            .list()
                                            .stream()
                                            .filter(Objects::nonNull)
                                            .filter(task -> Long.valueOf(task.tableOrder) > Long.valueOf(po.tableOrder))
                                            .forEach(task -> {
                                                // 将过滤出的所有task中的tableOrder-1
                                                task.tableOrder -= 1;
                                                // 调用修改单个管道接口
                                                this.editWorkflow(NifiCustomWorkflowDetailMap.INSTANCES.poToDto(task));
                                            });
                                    // 构造参数,调用editData
                                    NifiCustomWorkflowDetailVO vo = new NifiCustomWorkflowDetailVO();
                                    NifiCustomWorkflowDTO dto = NifiCustomWorkflowMap.INSTANCES
                                            .poToDto(nifiCustomWorkflowImpl.query().eq("workflow_id", job.workflowId).one());
                                    vo.dto = dto;
//                                    vo.flag = dto.status == 1;
                                    // 先只改配置,不发布整体流程
                                    vo.flag = false;
                                    vo.list = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(this.query().eq("workflow_id", dto.workflowId).list());

                                    this.editData(vo);
                                }));
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<ExternalDataSourceDTO> getExternalDataSourceList() {
        ResultEntity<List<DataSourceDTO>> allExternalDataSource = userClient.getAllExternalDataSource();
        if (allExternalDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        List<ExternalDataSourceDTO> list = new ArrayList<>();
        for (DataSourceDTO item : allExternalDataSource.data) {
            ExternalDataSourceDTO data = new ExternalDataSourceDTO();
            data.id = item.id;
            data.name = item.name;
            list.add(data);
        }
        return list;
    }

    @Override
    public List<String> getNextCronExeTime(NextCronTimeDTO dto) {
        return CronUtils.nextCronExeTime(dto);
    }

    @Override
    public List<DispatchJobHierarchyDTO> getJobList(QueryJobHierarchyDTO dto) {
        NifiCustomWorkflowPO one = nifiCustomWorkflowImpl.query().eq("id", dto.nifiCustomWorkflowId).eq("del_flag", 1).one();
        if (Objects.isNull(one)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        List<NifiCustomWorkflowDetailPO> list = this.query().eq("workflow_id", one.workflowId).eq("del_flag", 1).eq("pid", 0).ne("component_type", ChannelDataEnum.SCHEDULE_TASK.getName()).list();
        List<DispatchJobHierarchyDTO> dtos = new ArrayList<>();
        list.forEach(e -> {
            //初始状态为未运行
            DispatchJobHierarchyDTO dispatchJobHierarchy = new DispatchJobHierarchyDTO();
            dispatchJobHierarchy.id = e.id;
            dispatchJobHierarchy.jobName = e.componentName;
            //job初始状态为未运行
            dispatchJobHierarchy.jobStatus = NifiStageTypeEnum.NOT_RUN;
            if (StringUtils.isNotEmpty(e.inport)) {
                dispatchJobHierarchy.inport = JSON.parseArray(JSON.toJSONString(Arrays.asList(e.inport.split(","))), Long.class);
            }
            if (StringUtils.isNotEmpty(e.outport)) {
                dispatchJobHierarchy.outport = JSON.parseArray(JSON.toJSONString(Arrays.asList(e.outport.split(","))), Long.class);
            } else {
                //是管道某个支线的最后一级
                dispatchJobHierarchy.last = true;
            }
            dispatchJobHierarchy.forbidden = e.forbidden;

            dtos.add(dispatchJobHierarchy);
        });
        return dtos;
    }

    @Override
    public ResultEnum forbiddenTask(List<ForbiddenTaskDTO> dto) {
        if (CollectionUtils.isNotEmpty(dto)) {
            boolean ifNext = false;
            for (ForbiddenTaskDTO forbiddenTask : dto) {
                NifiCustomWorkflowDetailPO nifiCustomWorkflowDetail = this.getById(forbiddenTask.taskId);
                nifiCustomWorkflowDetail.forbidden = forbiddenTask.forbidden;
                mapper.forbiddenTask(nifiCustomWorkflowDetail);
                if (nifiCustomWorkflowDetail.pid != 0) {
                    ifNext = true;
                }
            }
            //如果是任务组,要改任务组下所有的task
            if (!ifNext) {
                for (ForbiddenTaskDTO forbiddenTask : dto) {
                    NifiCustomWorkflowDetailPO nifiCustomWorkflowDetail = this.getById(forbiddenTask.taskId);
                    if (nifiCustomWorkflowDetail.pid == 0 && !forbiddenTask.forbidden) {
                        List<NifiCustomWorkflowDetailPO> nifiCustomWorkflowDetailPos = this.query().eq("pid", nifiCustomWorkflowDetail.id).list();
                        for (NifiCustomWorkflowDetailPO detail : nifiCustomWorkflowDetailPos) {
                            detail.forbidden = false;
                            mapper.forbiddenTask(detail);
                        }
                    }
                }
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum runOnce(Long id) {
        NifiCustomWorkflowPO nifiCustomWorkflowPO = nifiCustomWorkflowImpl.getById(id);
        LambdaQueryWrapper<NifiCustomWorkflowDetailPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NifiCustomWorkflowDetailPO::getWorkflowId, nifiCustomWorkflowPO.getWorkflowId())
                .eq(NifiCustomWorkflowDetailPO::getComponentType, ChannelDataEnum.SCHEDULE_TASK.getName());
        NifiCustomWorkflowDetailPO nifiCustomWorkflowDetail = this.getOne(queryWrapper);
        ResultEntity<Object> objectResultEntity = publishTaskClient.runOnce(nifiCustomWorkflowDetail.getId());
        if (objectResultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
            log.error("task模块调用runOnce失败，[{}]", objectResultEntity.getMsg());
            return ResultEnum.getEnum(objectResultEntity.getCode());
        } else {
            return ResultEnum.SUCCESS;
        }
    }

    /**
     * 数据接入、数仓建模 - 校验非实时物理表、维度表、事实表是否被配置到管道
     *
     * @param dto
     * @return
     */
    @Override
    public List<NifiCustomWorkflowDetailDTO> checkPhyDimFactTableIfExists(CheckPhyDimFactTableIfExistsDTO dto) {
        //物理表在配置管道时的表类别是 3    数据湖表任务
        int tblType = dto.getChannelDataEnum().getValue();
        LambdaQueryWrapper<NifiCustomWorkflowDetailPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NifiCustomWorkflowDetailPO::getTableId, dto.getTblId())
                .eq(NifiCustomWorkflowDetailPO::getTableType, tblType);
        List<NifiCustomWorkflowDetailPO> list = nifiCustomWorkflowDetailImpl.list(wrapper);
        List<String> collect = list.stream().map(NifiCustomWorkflowDetailPO::getWorkflowId).collect(Collectors.toList());

        List<NifiCustomWorkflowDetailDTO> dtos = NifiCustomWorkflowDetailMap.INSTANCES.listPoToDto(list);
        for (int i = 0; i < collect.size(); i++) {
            NifiCustomWorkflowPO one = nifiCustomWorkflowImpl
                    .getOne(new LambdaQueryWrapper<NifiCustomWorkflowPO>()
                            .eq(NifiCustomWorkflowPO::getWorkflowId, collect.get(i)));
            //把workflowid替换为workflowName
            dtos.get(i).setWorkflowName(one.workflowName);
            dtos.get(i).setDispatchId(Math.toIntExact(one.id));
        }

        return dtos;
    }

}
