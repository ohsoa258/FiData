package com.fisk.taskfactory.dto.customworkflow;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.filter.dto.FilterQueryDTO;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import com.fisk.taskfactory.vo.customworkflow.NifiCustomWorkflowVO;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class NifiCustomWorkflowQueryDTO {

    /**
     * 查询字段值
     */
    public String key;
    public List<FilterQueryDTO> dto;
    /**
     * 分页,返回给前端的数据对象
     */
    public Page<NifiCustomWorkflowVO> page;
}
