package com.fisk.taskfactory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.response.ResultEnum;
import com.fisk.taskfactory.dto.customworkflow.NifiCustomWorkflowDTO;
import com.fisk.taskfactory.dto.customworkflow.NifiCustomWorkflowQueryDTO;
import com.fisk.taskfactory.entity.NifiCustomWorkflowPO;
import com.fisk.taskfactory.entity.TaskSchedulePO;
import com.fisk.taskfactory.vo.customworkflow.NifiCustomWorkflowVO;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author Lock
 */
public interface INifiCustomWorkflow extends IService<NifiCustomWorkflowPO> {
    /**
     * 添加管道
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum addData(NifiCustomWorkflowDTO dto);

    /**
     * 回显
     * @param id id
     * @return dto
     */
    NifiCustomWorkflowDTO getData(long id);

    /**
     * 修改
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum editData(NifiCustomWorkflowDTO dto);

    /**
     * 删除
     * @param id id
     * @return 执行结果
     */
    ResultEnum deleteData(long id);

    /**
     * 获取过滤器表字段
     * @return 字段
     */
    List<FilterFieldDTO> getColumn();

    /**
     * 筛选器
     * @param query 查询条件
     * @return 筛选结果
     */
    Page<NifiCustomWorkflowVO> listData(NifiCustomWorkflowQueryDTO query);
}
