package com.fisk.task.consumer.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.task.dto.doris.UpdateLogAndImportDataDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.nifi.IPostgreBuild;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.quartz.TriggerUtils;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.cronutils.model.CronType.QUARTZ;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/31 13:04
 * Description:pgsql stg表的数据同步到ogs中，目前已弃用，改用存储过程实现。
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_DATAINPUT_PGSQL_STGTOODS_FLOW)
@Slf4j
public class BuildDataInputStgToOdsListener {
    @Resource
    IPostgreBuild pg;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DATAINPUT_PG_STGTOODS_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        log.info("执行更新数据导入log状态");
        log.info("dataInfo:" + dataInfo);
        UpdateLogAndImportDataDTO inpData = JSON.parseObject(dataInfo, UpdateLogAndImportDataDTO.class);
        //doris.updateNifiLogsAndImportOdsData(inpData);
        pg.postgreDataStgToOds(inpData.tablename.replace("ods", "stg"),inpData.tablename,inpData);
        log.info("stg数据同步完成");
        //#region 更新增量表
        log.info("开始更新增量表");
        //计算下次更新时间
        String expressiion= inpData.corn;
        List<Date> nextExecTime = null;
        boolean b = checkValid(expressiion);
        if (b) {
            //解释cron表达式
            String s = describeCron(expressiion);
            //获取下次运行时间
            try {
                nextExecTime = getNextExecTime(expressiion, 1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            nextExecTime.stream().forEach(d -> {
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d));
            });
        };



        //#endregion
    }
    /**
     * 解释cron表达式
     */
    public  String describeCron(String expressiion) {
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(expressiion);
        //设置语言
        CronDescriptor descriptor = CronDescriptor.instance(Locale.CHINESE);
        return descriptor.describe(cron);
    }
    /**
     * 检查cron表达式的合法性
     *
     * @param cron cron exp
     * @return true if valid
     */
    public  boolean checkValid(String cron) {
        try {
            CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
            CronParser parser = new CronParser(cronDefinition);
            parser.parse(cron);
        } catch (IllegalArgumentException e) {
            System.out.println(String.format("cron=%s not valid", cron));
            return false;
        }
        return true;
    }

    /**
     * @param cronExpression cron表达式
     * @param numTimes       下一(几)次运行的时间
     * @return
     */
    public List<Date> getNextExecTime(String cronExpression, Integer numTimes) throws ParseException {
        List<String> list = new ArrayList<>();
        CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
        cronTriggerImpl.setCronExpression(cronExpression);
        // 这个是重点，一行代码搞定
        return TriggerUtils.computeFireTimes(cronTriggerImpl, null, numTimes);
    }
}
