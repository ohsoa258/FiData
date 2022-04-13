package com.fisk.task.listener.mdm.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.task.dto.model.ModelDTO;
import com.fisk.task.listener.mdm.BuildModelListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Description: 创建模型
 *
 * @author wangyan
 */
@Component
@Slf4j
public class BuildModelListenerImpl implements BuildModelListener {

    @Override
    public void msg(String dataInfo, Acknowledgment acke) {
        System.out.println(("mdm模型队列消息:" + dataInfo));

        try {
            ModelDTO modelDTO = JSON.parseObject(dataInfo, ModelDTO.class);
            System.out.println(modelDTO);

        } catch (Exception e) {

        } finally {
            acke.acknowledge();
        }
    }
}
