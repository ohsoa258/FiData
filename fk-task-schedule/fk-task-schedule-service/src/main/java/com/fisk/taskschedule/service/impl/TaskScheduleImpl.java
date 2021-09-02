package com.fisk.taskschedule.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.taskschedule.dto.TaskCronDTO;
import com.fisk.taskschedule.dto.TaskScheduleDTO;
import com.fisk.taskschedule.entity.TaskSchedulePO;
import com.fisk.taskschedule.mapper.TaskScheduleMapper;
import com.fisk.taskschedule.service.ITaskSchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.fisk.taskschedule.map.TaskScheduleMap.INSTANCES;

/**
 * @author Lock
 */
@Service
@Slf4j
public class TaskScheduleImpl extends ServiceImpl<TaskScheduleMapper, TaskSchedulePO> implements ITaskSchedule {

    @Resource
    TaskScheduleMapper mapper;

    @Override
    public ResultEntity<TaskCronDTO> addData(TaskScheduleDTO dto) {

        TaskSchedulePO model = INSTANCES.dtoToPo(dto);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.SAVE_VERIFY_ERROR);
        }
        String cronNextTime = "";
        if ("CRON".equalsIgnoreCase(model.syncMode)) {
            CronSequenceGenerator cron = new CronSequenceGenerator(model.expression);
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date = cron.next(d);
            cronNextTime = sdf.format(date);
        }

        TaskCronDTO taskCronDTO = new TaskCronDTO();
        boolean save = this.save(model);
        if (save) {
            taskCronDTO.code = ResultEnum.SUCCESS;
            taskCronDTO.cronNextTime = cronNextTime;
        } else {
            taskCronDTO.code = ResultEnum.SAVE_DATA_ERROR;
            taskCronDTO.cronNextTime = null;
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, taskCronDTO);
    }

    @Override
    public ResultEntity<TaskCronDTO> editData(TaskScheduleDTO dto) {
        TaskSchedulePO model = INSTANCES.dtoToPo(dto);
        if (model == null) {
            return ResultEntityBuild.build(ResultEnum.SAVE_VERIFY_ERROR);
        }

        int id = 0;
        // 判断jobPid不为空
        if (dto.jobPid != 0) {
            id = mapper.getIdTwo(dto.jobPid, dto.jobId);
        } else {
            id = mapper.getId(model.jobId);
        }
        model.id = id;
        boolean update = this.updateById(model);

        String cronNextTime = "";
        if ("CRON".equalsIgnoreCase(model.syncMode)) {
            CronSequenceGenerator cron = new CronSequenceGenerator(model.expression);
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date = cron.next(d);
            cronNextTime = sdf.format(date);
        }

        TaskCronDTO taskCronDTO = new TaskCronDTO();
        if (update) {
            taskCronDTO.code = ResultEnum.SUCCESS;
            taskCronDTO.cronNextTime = cronNextTime;
        } else {
            taskCronDTO.code = ResultEnum.SAVE_DATA_ERROR;
            taskCronDTO.cronNextTime = null;
        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, taskCronDTO);
    }

    @Override
    public ResultEntity<TaskScheduleDTO> getData(TaskScheduleDTO dto) {

        if (dto == null) {
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
        }
        TaskSchedulePO model;
        // 判断jobPid不为空
        if (dto.jobPid != 0) {
            model = mapper.getDataTwo(dto.jobId, dto.jobPid);
            if (model == null) {
                return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
            }
        } else {
            model = mapper.getData(dto.jobId);
            if (model == null) {
                return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS);
            }

        }

        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, INSTANCES.poToDto(model));
    }
}
