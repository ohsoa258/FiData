package com.fisk.task.listener.pipeline;

import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiPortsHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.PipeDagDTO;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

/**
 * @author: cfk
 * CreateTime: 2022/04/21 15:05
 * Description:
 */
public interface IPipelineTaskPublishCenter {
    /**
     * 发布中心
     *
     * @param dataInfo
     * @param acke
     * @return
     */
    void msg(String dataInfo, Acknowledgment acke);

    /**
     * 获取一部分dag图
     *
     * @param nifiGetPortHierarchy
     * @return NifiPortsHierarchyDTO
     */
    NifiPortsHierarchyDTO getNifiPortHierarchy(NifiGetPortHierarchyDTO nifiGetPortHierarchy, String pipelTraceId);

    /**
     * 获取全部dag图
     *
     * @param nifiGetPortHierarchy
     * @return PipeDagDTO
     */
    PipeDagDTO getPipeDagDto(NifiGetPortHierarchyDTO nifiGetPortHierarchy, String pipelTraceId);
}
