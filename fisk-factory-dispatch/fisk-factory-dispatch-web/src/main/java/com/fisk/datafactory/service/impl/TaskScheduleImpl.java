package com.fisk.datafactory.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datafactory.dto.dataaccess.DataAccessIdDTO;
import com.fisk.datafactory.dto.taskschedule.TaskCronDTO;
import com.fisk.datafactory.dto.taskschedule.TaskScheduleDTO;
import com.fisk.datafactory.entity.TaskSchedulePO;
import com.fisk.datafactory.map.TaskScheduleMap;
import com.fisk.datafactory.mapper.TaskScheduleMapper;
import com.fisk.datafactory.service.ITaskSchedule;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.businessprocess.BusinessAreaContentDTO;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wangyan and Lock
 */
@Service
@Slf4j
public class TaskScheduleImpl extends ServiceImpl<TaskScheduleMapper, TaskSchedulePO> implements ITaskSchedule {

    @Resource
    TaskScheduleMapper mapper;
    @Resource
    DataModelClient dataModelClient;

    @Override
    public ResultEntity<TaskCronDTO> addData(TaskScheduleDTO dto) {
        TaskSchedulePO model = encapsulationTaskScheduleDto(dto);
        boolean save = this.save(model);
        String cronNextTime = getCronNextTime(model);
        TaskCronDTO taskCronDTO = new TaskCronDTO();
        DataAccessIdDTO dataAccessIdDTO = new DataAccessIdDTO();
        if (save) {
            taskCronDTO.code = ResultEnum.SUCCESS;
            taskCronDTO.cronNextTime = cronNextTime;
            taskCronDTO.flag = dto.flag;
            switch (dto.flag) {
                // 数据接入data-access
                case 1:
                    if (dto.jobPid != 0 && dto.appType == 1) {
                        dataAccessIdDTO.appId = dto.jobPid;
                        dataAccessIdDTO.tableId = dto.jobId;
                        dataAccessIdDTO.syncMode = dto.syncMode;
                        dataAccessIdDTO.expression = dto.expression;
                        dataAccessIdDTO.olapTableEnum = OlapTableEnum.PHYSICS;
                        taskCronDTO.dto = dataAccessIdDTO;
                    }
                    break;
                // 数据建模data-model
                // 指标
                case 5:
                    if (dto.jobPid != 0) {
                        ResultEntity<Object> result = dataModelClient.getBusinessId(dto.jobId);
                        if (result.code == 0) {
                            ResultEntity<BusinessAreaContentDTO> resultEntity = JSON.parseObject(JSON.toJSONString(result.data), ResultEntity.class);
                            BusinessAreaContentDTO businessAreaContentDTO = JSON.parseObject(JSON.toJSONString(resultEntity.data), BusinessAreaContentDTO.class);
                            dataAccessIdDTO.appId = businessAreaContentDTO.businessAreaId;
                            dataAccessIdDTO.factTableName = businessAreaContentDTO.factTableName;
                        }
                        dataAccessIdDTO.tableId = dto.jobId;
                        dataAccessIdDTO.syncMode = dto.syncMode;
                        dataAccessIdDTO.expression = dto.expression;
                        dataAccessIdDTO.olapTableEnum = OlapTableEnum.KPI;
                        taskCronDTO.dto = dataAccessIdDTO;
                    }
                    break;
                // 维度
                case 8:
                    if (dto.jobPid != 0) {
                        dataAccessIdDTO.appId = dto.jobPid;
                        dataAccessIdDTO.tableId = dto.jobId;
                        dataAccessIdDTO.syncMode = dto.syncMode;
                        dataAccessIdDTO.expression = dto.expression;
                        dataAccessIdDTO.olapTableEnum = OlapTableEnum.DIMENSION;
                        taskCronDTO.dto = dataAccessIdDTO;
                    }
                    break;
                // 事实表
                case 10:
                    if (dto.jobPid != 0) {
                        ResultEntity<Object> result = dataModelClient.getBusinessId(dto.jobPid);
                        if (result.code == 0) {
                            ResultEntity<BusinessAreaContentDTO> resultEntity = JSON.parseObject(JSON.toJSONString(result.data), ResultEntity.class);
                            BusinessAreaContentDTO businessAreaContentDTO = JSON.parseObject(JSON.toJSONString(resultEntity.data), BusinessAreaContentDTO.class);
                            dataAccessIdDTO.appId = businessAreaContentDTO.businessAreaId;
                        }
                        dataAccessIdDTO.tableId = dto.jobId;
                        dataAccessIdDTO.syncMode = dto.syncMode;
                        dataAccessIdDTO.expression = dto.expression;
                        dataAccessIdDTO.olapTableEnum = OlapTableEnum.FACT;
                        taskCronDTO.dto = dataAccessIdDTO;
                    }
                    break;
                default:
                    break;
            }
        } else {
            taskCronDTO.code = ResultEnum.SAVE_DATA_ERROR;
            taskCronDTO.cronNextTime = null;
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, taskCronDTO);
    }

    @Override
    public ResultEntity<TaskCronDTO> editData(TaskScheduleDTO dto) {
        TaskSchedulePO model = encapsulationTaskScheduleDto(dto);
        TaskSchedulePO taskSchedule = mapper.getTaskSchedule(dto.jobId, dto.flag);
        model.id = taskSchedule.id;
        boolean update = this.updateById(model);

        String cronNextTime = getCronNextTime(model);
        TaskCronDTO taskCronDTO = new TaskCronDTO();
        DataAccessIdDTO dataAccessIdDTO = new DataAccessIdDTO();
        if (update) {
            taskCronDTO.code = ResultEnum.SUCCESS;
            taskCronDTO.cronNextTime = cronNextTime;
            taskCronDTO.flag = dto.flag;
            switch (dto.flag) {
                // 数据接入data-access
                case 1:
                    //
                    if (dto.jobPid != 0 && dto.appType == 1) {
                        dataAccessIdDTO.appId = dto.jobPid;
                        dataAccessIdDTO.tableId = dto.jobId;
                        dataAccessIdDTO.syncMode = dto.syncMode;
                        dataAccessIdDTO.expression = dto.expression;
                        dataAccessIdDTO.olapTableEnum = OlapTableEnum.PHYSICS;
                        taskCronDTO.dto = dataAccessIdDTO;
                    }
                    break;
                case 2:
                    break;
                default:
                    break;
            }
        } else {
            taskCronDTO.code = ResultEnum.SAVE_DATA_ERROR;
            taskCronDTO.cronNextTime = null;
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, taskCronDTO);
    }

    @Override
    public ResultEntity<TaskScheduleDTO> getData(TaskScheduleDTO dto) {
        String str1 = "T";
        String str2 = "sec";
        String str3 = "TIMER";
        int forCycle = 4;

        if (dto == null) {
            return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL);
        }

        TaskSchedulePO model = mapper.getTaskSchedule(dto.jobId, dto.flag);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        // TIMER_DRIVEN值去除" sec"
        if (model.syncMode.contains(str1) && model.expression.contains(str2)) {
            for (int i = 0; i < forCycle; i++) {
                model.expression = model.expression.substring(0, model.expression.length() - 1);
            }
            model.syncMode = str3;
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, TaskScheduleMap.INSTANCES.poToDto(model));
    }

    private TaskSchedulePO encapsulationTaskScheduleDto(TaskScheduleDTO dto) {
        String timerType = "TIMER";
        String timerDrivenType1 = "Timer driven";
        String timerDrivenType2 = "TIMER_DRIVEN";
        String cronType = "CRON";
        String cronDrivenType1 = "CRON driven";
        String cronDrivenType2 = "CRON_DRIVEN";
        String secTpye = " sec";
        TaskSchedulePO model = TaskScheduleMap.INSTANCES.dtoToPo(dto);
        if (model == null) {
            throw new FkException(ResultEnum.SAVE_VERIFY_ERROR);
        }
        if (timerType.equalsIgnoreCase(model.syncMode)) {
            model.syncMode = timerDrivenType1;
            dto.syncMode = timerDrivenType2;
            model.expression += secTpye;
            dto.expression += secTpye;
        } else if (cronType.equalsIgnoreCase(model.syncMode)) {
            model.syncMode = cronDrivenType1;
            dto.syncMode = cronDrivenType2;
        }
        return model;
    }

    private String getCronNextTime(TaskSchedulePO model) {
        String cronType = "CRON";
        String cronNextTime = "";
        if (cronType.equalsIgnoreCase(model.syncMode)) {
            CronSequenceGenerator cron = null;
            try {
                cron = new CronSequenceGenerator(model.expression);
            } catch (Exception e) {
                throw new FkException(ResultEnum.TASK_SCHEDULE_CRONEXPRESSION_ERROR);
            }
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = cron.next(d);
            cronNextTime = sdf.format(date);
        }

        return cronNextTime;
    }
}
