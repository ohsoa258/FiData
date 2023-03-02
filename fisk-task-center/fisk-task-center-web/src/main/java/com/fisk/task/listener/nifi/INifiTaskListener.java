package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author: cfk
 * CreateTime: 2022/03/22 15:05
 * Description:
 */
public interface INifiTaskListener {
    /**
     * 接入,建模nifi流程
     *
     * @param dataInfo
     * @param acke
     * @return
     */
    ResultEnum msg(String dataInfo, Acknowledgment acke);

    /**
     * 表服务nifi流程
     *
     * @param dataInfo
     * @param acke
     * @return
     */
    ResultEnum buildDataServices(String dataInfo, Acknowledgment acke);

    /**
     * 创建接入nifi流程
     *
     * @param dataInfo
     * @param acke
     * @return
     */
    ResultEnum buildAccessNifiProcess(String dataInfo, Acknowledgment acke);

    /**
     * 创建建模nifi流程
     *
     * @param dataInfo
     * @param acke
     * @return
     */
    ResultEnum buildModelNifiProcess(String dataInfo, Acknowledgment acke);
}
