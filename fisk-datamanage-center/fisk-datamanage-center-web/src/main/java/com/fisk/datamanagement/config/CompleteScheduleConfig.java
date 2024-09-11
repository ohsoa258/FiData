package com.fisk.datamanagement.config;

import com.fisk.datamanagement.mapper.MetaAnalysisEmailConfigMapper;
import com.fisk.datamanagement.service.impl.MetaAnalysisEmailConfigServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
@Slf4j
public class CompleteScheduleConfig implements SchedulingConfigurer {

    @Resource
    private MetaAnalysisEmailConfigMapper metaAnalysisEmailConfigMapper;

    @Resource
    private MetaAnalysisEmailConfigServiceImpl metaAnalysisEmailConfigService;

//    /**
//     * 执行定时任务.  默认
//     */
//    @Override
//    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        taskRegistrar.addTriggerTask(
//                //1.添加任务内容(Runnable)
//                this::sendEmailWithMetaAuditConfig,
//                // 2. 设置执行周期(Trigger)
//                this::getNextExecutionTime);
//    }

    /**
     * 执行定时任务. 线程池
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        //可以设置线程池大小
//        scheduler.setPoolSize(10);
        scheduler.initialize();

        taskRegistrar.setScheduler(scheduler);

        //定时任务1
        taskRegistrar.addTriggerTask(
                // 1. 添加任务内容(Runnable)
                this::sendEmailWithMetaAuditConfig,
                // 2. 设置执行周期(Trigger)
                this::getNextExecutionTime
        );

        //可以设置多个定时任务 执行周期和执行的任务内容可以按需修改
        /*
        执行周期的方法可以按需修改，举例：根据要跑的任务，getNextExecutionTime方法
        从库里拿执行的cron表达式时，库里可以有个字段，去确定我这个cron表达式是什么任务用的
         */
        //定时任务2
//        taskRegistrar.addTriggerTask(
//                //添加别的任务内容(Runnable)
//                this::sendEmailWithMetaAuditConfig,
//                //设置执行周期(Trigger)
//                this::getNextExecutionTime
//        );
        //定时任务3......
    }

    /**
     * 获取定时任务的cron表达式
     */
    private Date getNextExecutionTime(TriggerContext triggerContext) {
        // 从配置库获取定时任务执行时间
        String cron = getCronFromConfig();
        //如果为空或获取不到 则使用默认配置
        if (cron == null || cron.isEmpty()) {
            log.error("未配置或非法的cron表达式。使用默认配置。");
            return getDefaultCronTrigger();
        }

        //校验cron合法性
        try {
            CronSequenceGenerator generator = new CronSequenceGenerator(cron);
            Date nextExecutionTime = generator.next(new Date());
            log.info("cron表达式合法：" + cron + " -- 转换为时间：" + nextExecutionTime);
            return new CronTrigger(cron).nextExecutionTime(triggerContext);
        } catch (Exception e) {
            log.error("解析cron表达式失败: " + cron + "。详细报错信息：" + e.getMessage());
            return getDefaultCronTrigger();
        }
    }

    /**
     * 从配置库获取定时任务执行时间  cron表达式
     */
    private String getCronFromConfig() {
        try {
            return metaAnalysisEmailConfigMapper.getCron().get(0);
        } catch (Exception e) {
            log.error("从数据库获取cron表达式失败。详细报错信息：" + e.getMessage());
            return "";  // 返回空字符串或默认值
        }
    }

    /**
     * 获取默认Cron表达式触发时间
     */
    private Date getDefaultCronTrigger() {
        // 返回默认的cron表达式触发时间，确保任务能够执行
        String defaultCron = "0 0 12 * * ?";  // 每天中午12点执行
        try {
            CronSequenceGenerator generator = new CronSequenceGenerator(defaultCron);
            return generator.next(new Date());
        } catch (IllegalArgumentException e) {
            log.error("默认cron表达式 " + defaultCron + " 配置错误。详细报错信息：" + e.getMessage());
            return null;
        }
    }

    /**
     * 发送邮件的方法
     */
    private void sendEmailWithMetaAuditConfig() {
        // 发送邮件的逻辑
        metaAnalysisEmailConfigService.sendEmailOfMetaAudit();
    }

}
