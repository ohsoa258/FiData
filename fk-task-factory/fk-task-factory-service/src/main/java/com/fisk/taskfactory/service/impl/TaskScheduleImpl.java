package com.fisk.taskfactory.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.businessprocess.BusinessAreaContentDTO;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.taskfactory.entity.TaskSchedulePO;
import com.fisk.taskfactory.map.TaskScheduleMap;
import com.fisk.taskschedule.dto.TaskCronDTO;
import com.fisk.taskschedule.dto.TaskScheduleDTO;
import com.fisk.taskschedule.dto.dataaccess.DataAccessIdDTO;
import com.fisk.taskfactory.mapper.TaskScheduleMapper;
import com.fisk.taskfactory.service.ITaskSchedule;
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

        TaskSchedulePO model = TaskScheduleMap.INSTANCES.dtoToPo(dto);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.SAVE_VERIFY_ERROR);
        }

        if ("TIMER".equalsIgnoreCase(model.syncMode)) {
            model.syncMode = "Timer driven";
            dto.syncMode = "Timer driven";
            model.expression += " sec";
            dto.expression += " sec";
        } else if ("CRON".equalsIgnoreCase(model.syncMode)){
            model.syncMode = "CRON driven";
            dto.syncMode = "CRON driven";
        }

        boolean save = this.save(model);
        String cronNextTime = "";
        if ("CRON".equalsIgnoreCase(model.syncMode)) {
            CronSequenceGenerator cron = null;
            try {
                cron = new CronSequenceGenerator(model.expression);
            } catch (Exception e) {
                return ResultEntityBuild.build(ResultEnum.TASK_SCHEDULE_CRONEXPRESSION_ERROR);
            }
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date = cron.next(d);
            cronNextTime = sdf.format(date);
        }

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
        TaskSchedulePO model = TaskScheduleMap.INSTANCES.dtoToPo(dto);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.SAVE_VERIFY_ERROR);
        }

        TaskSchedulePO taskSchedule = mapper.getTaskSchedule(dto.jobId, dto.flag);

        model.id = taskSchedule.id;
        boolean update = this.updateById(model);

        String cronNextTime = "";
        if ("CRON".equalsIgnoreCase(model.syncMode)) {
            CronSequenceGenerator cron = null;
            try {
                cron = new CronSequenceGenerator(model.expression);
            } catch (Exception e) {
                return ResultEntityBuild.build(ResultEnum.TASK_SCHEDULE_CRONEXPRESSION_ERROR);
            }
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date = cron.next(d);
            cronNextTime = sdf.format(date);
        }

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

        if (dto == null) {
            return ResultEntityBuild.build(ResultEnum.PARAMTER_NOTNULL);
        }

        TaskSchedulePO model = mapper.getTaskSchedule(dto.jobId, dto.flag);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, TaskScheduleMap.INSTANCES.poToDto(model));
    }
}
