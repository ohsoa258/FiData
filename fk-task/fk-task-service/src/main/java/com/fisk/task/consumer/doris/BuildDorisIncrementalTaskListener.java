package com.fisk.task.consumer.doris;

import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fisk.common.constants.MqConstants;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.task.dto.doris.UpdateLogAndImportDataDTO;
import com.fisk.task.entity.TBETLlogPO;
import com.fisk.task.extend.aop.MQConsumerLog;
import com.fisk.task.mapper.TBETLLogMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/30 17:25
 * Description:
 */
@Component
@RabbitListener(queues = MqConstants.QueueConstants.BUILD_DORIS_INCREMENTAL_FLOW)
@Slf4j
@DS("datainputdb")
public class BuildDorisIncrementalTaskListener  {

    @Resource
    TBETLLogMapper etlmapper;

    @RabbitHandler
    @MQConsumerLog(type = TraceTypeEnum.DORIS_INCREMENTAL_MQ_BUILD)
    public void msg(String dataInfo, Channel channel, Message message) {
        log.info("执行更新数据导入log");
        log.info("dataInfo:" + dataInfo);
        UpdateLogAndImportDataDTO inpData = JSON.parseObject(dataInfo, UpdateLogAndImportDataDTO.class);
        //etlmapper.update(inpData)
        TBETLlogPO modeletllog = inpData.toEntity(TBETLlogPO.class);
        modeletllog.setStatus(2);
        UpdateWrapper<TBETLlogPO> updateWrapper=new UpdateWrapper<>();
        updateWrapper.eq("code",inpData.code);
        int updateres=etlmapper.update(modeletllog,updateWrapper);

    }
}
