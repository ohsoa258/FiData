package com.fisk.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fisk.task.scheduled.CronTaskRegistrar;
import com.fisk.task.scheduled.SchedulingRunnable;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.entity.ScheduleSettingPO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = {SwaggerConfig.SCHEDULE})
@RestController
@RequestMapping("/schedule")
public class ScheduleSettingController {

    @Autowired
    private CronTaskRegistrar cronTaskRegistrar;

    /**
     * 添加定时任务
     *
     * @param sysJob
     * @return
     */
    @ApiOperation(value = "添加定时任务")
    @PostMapping("add")
    public boolean add(@RequestBody ScheduleSettingPO sysJob) {
        boolean insert = sysJob.insert();
        if (!insert) {
            return false;
        } else {
            if (sysJob.getJobStatus().equals(1)) {// 添加成功,并且状态是1，直接放入任务器
                SchedulingRunnable task = new SchedulingRunnable(sysJob.getBeanName(), sysJob.getMethodName(), sysJob.getMethodParams());
                cronTaskRegistrar.addCronTask(task, sysJob.getCronExpression());
            }
        }
        return insert;
    }

    /**
     * 修改定时任务
     *
     * @param sysJob
     * @return
     */
    @ApiOperation(value = "修改定时任务")
    @PostMapping("update")
    public boolean update(@RequestBody ScheduleSettingPO sysJob) {

        // 查询修改前任务
        ScheduleSettingPO existedSysJob = new ScheduleSettingPO();
        existedSysJob = existedSysJob.selectOne(new QueryWrapper<ScheduleSettingPO>().eq("job_id", sysJob.getJobId()));
        // 修改任务
        boolean update = sysJob.update(new UpdateWrapper<ScheduleSettingPO>().eq("job_id", sysJob.getJobId()));
        if (!update) {
            return false;
        } else {
            // 修改成功,则先删除任务器中的任务,并重新添加
            SchedulingRunnable task1 = new SchedulingRunnable(existedSysJob.getBeanName(), existedSysJob.getMethodName(), existedSysJob.getMethodParams());
            cronTaskRegistrar.removeCronTask(task1);
            if (sysJob.getJobStatus().equals(1)) {// 如果修改后的任务状态是1就加入任务器
                SchedulingRunnable task = new SchedulingRunnable(sysJob.getBeanName(), sysJob.getMethodName(), sysJob.getMethodParams());
                cronTaskRegistrar.addCronTask(task, sysJob.getCronExpression());
            }
        }
        return update;
    }

    /**
     * 删除任务
     *
     * @param jobId
     * @return
     */
    @ApiOperation(value = "删除任务")
    @PostMapping("del/{jobId}")
    public boolean del(@PathVariable("jobId") Integer jobId) {
        // 先查询要删除的任务信息
        ScheduleSettingPO existedSysJob = new ScheduleSettingPO();
        existedSysJob = existedSysJob.selectOne(new QueryWrapper<ScheduleSettingPO>().eq("job_id", jobId));

        // 删除
        boolean del = existedSysJob.delete(new QueryWrapper<ScheduleSettingPO>().eq("job_id", jobId));
        if (!del) {
            return false;
        } else {// 删除成功时要清除定时任务器中的对应任务
            SchedulingRunnable task = new SchedulingRunnable(existedSysJob.getBeanName(), existedSysJob.getMethodName(), existedSysJob.getMethodParams());
            cronTaskRegistrar.removeCronTask(task);
        }
        return del;
    }

    /**
     *  停止/启动任务
     *
     * @param jobId
     * @param stop
     * @return
     */
    @ApiOperation(value = "停止/启动任务")
    @PostMapping("changesStatus/{jobId}/{stop}")
    public boolean changesStatus(@PathVariable("jobId") Integer jobId, @PathVariable("stop") Integer stop) {
        // 修改任务状态
        ScheduleSettingPO scheduleSetting = new ScheduleSettingPO();
        scheduleSetting.setJobStatus(stop);
        boolean job_id = scheduleSetting.update(new UpdateWrapper<ScheduleSettingPO>().eq("job_id", jobId));
        if (!job_id) {
            return false;
        }
        // 查询修改后的任务信息
        ScheduleSettingPO existedSysJob = new ScheduleSettingPO();
        existedSysJob = existedSysJob.selectOne(new QueryWrapper<ScheduleSettingPO>().eq("job_id", jobId));

        // 如果状态是1则添加任务
        if (existedSysJob.getJobStatus().equals(1)) {
            SchedulingRunnable task = new SchedulingRunnable(existedSysJob.getBeanName(), existedSysJob.getMethodName(), existedSysJob.getMethodParams());
            cronTaskRegistrar.addCronTask(task, existedSysJob.getCronExpression());
        } else {
            // 否则清除任务
            SchedulingRunnable task = new SchedulingRunnable(existedSysJob.getBeanName(), existedSysJob.getMethodName(), existedSysJob.getMethodParams());
            cronTaskRegistrar.removeCronTask(task);
        }
        return true;
    }
}