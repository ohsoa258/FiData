package com.fisk.task.listener.pipeline;

import com.fisk.datafactory.dto.customworkflowdetail.DispatchJobHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.NifiGetPortHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.TaskHierarchyDTO;
import com.fisk.datafactory.dto.tasknifi.PipeDagDTO;
import org.springframework.kafka.support.Acknowledgment;

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
    TaskHierarchyDTO getNifiPortHierarchy(NifiGetPortHierarchyDTO nifiGetPortHierarchy, String pipelTraceId);

    /**
     * 获取全部dag图
     *
     * @param nifiGetPortHierarchy
     * @return PipeDagDTO
     */
    PipeDagDTO getPipeDagDto(NifiGetPortHierarchyDTO nifiGetPortHierarchy, String pipelTraceId);

    /**
     * 通过jobid找redis里面的job对象
     *
     * @param jobId jobid
     * @return
     */
    DispatchJobHierarchyDTO getDispatchJobHierarchy(String pipelTraceId, String jobId);

    /**
     * 通过taskid找redis里面的task对象
     *
     * @param taskId
     * @return
     */
    TaskHierarchyDTO getTaskHierarchy(String pipelTraceId, String taskId);

    /**
     *
     * @param pipelTraceId
     * @param taskId
     * @return
     */
    DispatchJobHierarchyDTO getDispatchJobHierarchyByTaskId(String pipelTraceId, String taskId);
}
