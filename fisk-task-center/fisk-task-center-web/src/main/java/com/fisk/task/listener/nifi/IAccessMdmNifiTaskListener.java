package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author: wangjian
 */
public interface IAccessMdmNifiTaskListener {
    /**
     * 接入同步mdm nifi流程
     *
     * @param dataInfo
     * @param acke
     * @return
     */
    ResultEnum accessMdmMsg(String dataInfo, Acknowledgment acke);
}
