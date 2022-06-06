package com.fisk.task.listener.mdm;

import com.fisk.common.core.response.ResultEnum;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author WangYan
 * @date 2022/4/13 15:11
 */
public interface BuildModelListener {

    /**
     * 创建属性日志表
     * @param dataInfo
     * @param acke
     */
    ResultEnum msg(String dataInfo, Acknowledgment acke);

    /**
     * 创建后台任务表
     * @param dataInfo
     * @param acke
     */
    ResultEnum backgroundCreateTasks(String dataInfo, Acknowledgment acke);
}
