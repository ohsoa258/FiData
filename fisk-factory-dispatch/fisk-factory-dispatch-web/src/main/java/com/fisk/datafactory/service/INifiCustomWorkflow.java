package com.fisk.datafactory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkFlowDropDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowNumDTO;
import com.fisk.datafactory.dto.customworkflow.NifiCustomWorkflowQueryDTO;
import com.fisk.datafactory.entity.NifiCustomWorkflowPO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import com.fisk.datafactory.vo.customworkflowdetail.NifiCustomWorkflowDetailVO;

import java.util.List;

/**
 * @author Lock
 */
public interface INifiCustomWorkflow extends IService<NifiCustomWorkflowPO> {
    /**
     * 添加管道
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(NifiCustomWorkflowDTO dto);

    /**
     * 回显
     *
     * @param id id
     * @return dto
     */
    NifiCustomWorkflowDetailVO getData(long id);

    /**
     * 修改
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(NifiCustomWorkflowDTO dto);

    /**
     * 删除
     *
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);

    /**
     * 获取过滤器表字段
     *
     * @return 字段
     */
    List<FilterFieldDTO> getColumn();

    /**
     * 筛选器
     *
     * @param query 查询条件
     * @return 筛选结果
     */
    Page<NifiCustomWorkflowVO> listData(NifiCustomWorkflowQueryDTO query);

    /**
     * 查询数据调度图当天运行情况
     *
     * @return dto
     */
    NifiCustomWorkflowNumDTO getNum();

    /**
     * 修改管道发布状态
     *
     * @param dto dto
     */
    void updatePublishStatus(NifiCustomWorkflowDTO dto);

    /**
     * 根据管道id查询组件绑定的表集合
     *
     * @param id id
     * @return list
     */
    List<String> getTableListById(Long id);

    /**
     * 获取所有管道
     *
     * @return
     */
    List<NifiCustomWorkFlowDropDTO> getNifiCustomWorkFlowDrop();


    /**
     * 获取数据调度中的应用总数
     *
     * @return Integer
     */
    Integer getDataDispatchNum();

    /**
     * 暂停/恢复管道工作状态
     * @param nifiCustomWorkflowId
     * @param ifFire
     * @return
     */
    ResultEntity<Object> updateWorkStatus(String nifiCustomWorkflowId, boolean ifFire);

    /**
     * 依据pipelTraceId获取管道部分字段信息
     * @param pipelTraceId
     * @return
     */
    ResultEntity<Object> getNifiCustomWorkFlowPartInfo(String pipelTraceId);
}
