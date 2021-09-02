package com.fisk.task.consumer.postgre.datainput;

import com.alibaba.fastjson.JSON;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.task.dto.doris.UpdateLogAndImportDataDTO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.service.IPostgreBuild;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/31 13:04
 * Description:
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

        //#endregion
    }
}
