package com.fisk.task.service.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.davis.client.ApiException;
import com.davis.client.model.BulletinEntity;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessorEntity;
import com.fisk.common.response.ResultEntity;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.task.dto.nifi.NifiStageMessageDTO;
import com.fisk.task.dto.task.TableNifiSettingPO;
import com.fisk.task.entity.NifiStagePO;
import com.fisk.task.entity.OlapPO;
import com.fisk.task.entity.PipelineTableLogPO;
import com.fisk.task.enums.NifiStageTypeEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.mapper.NifiStageMapper;
import com.fisk.task.mapper.PipelineTableLogMapper;
import com.fisk.task.service.nifi.INifiStage;
import com.fisk.task.utils.NifiHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class NifiStageImpl extends ServiceImpl<NifiStageMapper, NifiStagePO> implements INifiStage {

    @Resource
    NifiStageMapper nifiStageMapper;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;
    @Resource
    OlapImpl olap;
    @Resource
    DataFactoryClient dataFactoryClient;
    @Resource
    PipelineTableLogMapper pipelineTableLog;


    @Override
    public NifiStagePO getNifiStage(NifiCustomWorkflowDetailDTO nifiCustomWorkflowDetail) {
        QueryWrapper<NifiStagePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(NifiStagePO::getComponentId, nifiCustomWorkflowDetail.id);
        NifiStagePO nifiStage = nifiStageMapper.selectOne(queryWrapper);
        //通过组件id查到nifi阶段
        return nifiStage;
    }

    @Override
    public NifiStagePO saveNifiStage(String data, Acknowledgment acke) {
        NifiStagePO nifiStagePO = new NifiStagePO();
        try {
            NifiStageMessageDTO nifiStageMessageDTO = JSON.parseObject(data, NifiStageMessageDTO.class);
            String topicName = nifiStageMessageDTO.topic;
            String[] topic = topicName.split("\\.");
            if (topic.length == 5) {
                return null;
            }
            String pipelineName = topic[3];
            TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.query()
                    .eq("table_component_id", nifiStageMessageDTO.groupId).eq("del_flag", 1).one();
            //通过应用简称+表类别+表id,查到组件id
            String tableName = tableNifiSettingPO.tableName;
            Integer tableAccessId = tableNifiSettingPO.tableAccessId;
            int type = tableNifiSettingPO.type;
            NifiGetPortHierarchyDTO nifiGetPortHierarchyDTO = olap.getNifiGetPortHierarchy(pipelineName, type, tableName, tableAccessId);
            //三个阶段,默认正在运行
            nifiStagePO.insertPhase = NifiStageTypeEnum.RUNNING.getValue();
            nifiStagePO.queryPhase = NifiStageTypeEnum.RUNNING.getValue();
            nifiStagePO.transitionPhase = NifiStageTypeEnum.RUNNING.getValue();
            ResultEntity<NifiPortsHierarchyDTO> nIfiPortHierarchy = dataFactoryClient.getNIfiPortHierarchy(nifiGetPortHierarchyDTO);
            NifiCustomWorkflowDetailDTO itselfPort = nIfiPortHierarchy.data.itselfPort;
            nifiStagePO.componentId = Math.toIntExact(itselfPort.id);
            if (nifiStageMessageDTO.message == null || "".equals(nifiStageMessageDTO.message)) {
                nifiStagePO.comment = "运行成功";
            } else {
                nifiStagePO.comment = nifiStageMessageDTO.message;
                ProcessGroupEntity processGroup = NifiHelper.getProcessGroupsApi().getProcessGroup(nifiStageMessageDTO.groupId);
                List<BulletinEntity> bulletins = processGroup.getBulletins();
                if (bulletins != null && bulletins.size() != 0) {
                    String sourceId = bulletins.get(bulletins.size() - 1).getSourceId();
                    ProcessorEntity processor = NifiHelper.getProcessorsApi().getProcessor(sourceId);
                    String description = processor.getComponent().getConfig().getComments();
                    if (Objects.equals(description, NifiStageTypeEnum.QUERY_PHASE.getName())) {
                        nifiStagePO.queryPhase = NifiStageTypeEnum.RUN_FAILED.getValue();
                        nifiStagePO.insertPhase = NifiStageTypeEnum.NOT_RUN.getValue();
                        nifiStagePO.transitionPhase = NifiStageTypeEnum.NOT_RUN.getValue();
                    } else if (Objects.equals(description, NifiStageTypeEnum.TRANSITION_PHASE.getName())) {
                        nifiStagePO.insertPhase = NifiStageTypeEnum.NOT_RUN.getValue();
                        nifiStagePO.transitionPhase = NifiStageTypeEnum.RUN_FAILED.getValue();
                    } else if (Objects.equals(description, NifiStageTypeEnum.INSERT_PHASE.getName())) {
                        nifiStagePO.insertPhase = NifiStageTypeEnum.RUN_FAILED.getValue();
                    }
                }
            }
            nifiStageMapper.deleteByComponentId(nifiStagePO.componentId);
            this.save(nifiStagePO);
            PipelineTableLogPO pipelineTableLogPO = new PipelineTableLogPO();
            pipelineTableLogPO.comment = nifiStagePO.comment;
            pipelineTableLogPO.componentId = nifiStagePO.componentId;
            pipelineTableLogPO.tableId = tableAccessId;
            pipelineTableLogPO.tableType = type;
            if (Objects.equals(nifiStagePO.insertPhase, NifiStageTypeEnum.RUN_FAILED.getValue()) ||
                    Objects.equals(nifiStagePO.queryPhase, NifiStageTypeEnum.RUN_FAILED.getValue()) ||
                    Objects.equals(nifiStagePO.transitionPhase, NifiStageTypeEnum.RUN_FAILED.getValue())) {
                pipelineTableLogPO.state = NifiStageTypeEnum.RUN_FAILED.getValue();
            } else if (Objects.equals(nifiStagePO.insertPhase, NifiStageTypeEnum.RUNNING.getValue()) ||
                    Objects.equals(nifiStagePO.queryPhase, NifiStageTypeEnum.RUNNING.getValue()) ||
                    Objects.equals(nifiStagePO.transitionPhase, NifiStageTypeEnum.RUNNING.getValue())) {
                pipelineTableLogPO.state = NifiStageTypeEnum.RUNNING.getValue();
            } else if (Objects.equals(nifiStagePO.insertPhase, NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue()) &&
                    Objects.equals(nifiStagePO.queryPhase, NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue()) &&
                    Objects.equals(nifiStagePO.transitionPhase, NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue())) {
                pipelineTableLogPO.state = NifiStageTypeEnum.SUCCESSFUL_RUNNING.getValue();
            } else if (Objects.equals(nifiStagePO.insertPhase, NifiStageTypeEnum.NOT_RUN.getValue()) &&
                    Objects.equals(nifiStagePO.queryPhase, NifiStageTypeEnum.NOT_RUN.getValue()) &&
                    Objects.equals(nifiStagePO.transitionPhase, NifiStageTypeEnum.NOT_RUN.getValue())) {
                pipelineTableLogPO.state = NifiStageTypeEnum.NOT_RUN.getValue();
            }
            pipelineTableLog.deleteByComponentId(nifiStagePO.componentId);
            pipelineTableLog.insert(pipelineTableLogPO);
        } catch (ApiException e) {
            log.error(e.getResponseBody());
        } finally {
            acke.acknowledge();
        }
        return nifiStagePO;
    }

}
