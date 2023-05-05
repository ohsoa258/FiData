package com.fisk.datafactory.dto.customworkflow;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.datafactory.vo.customworkflow.NifiCustomWorkflowVO;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "key")
    public String key;

    @ApiModelProperty(value = "dto")
    public List<FilterQueryDTO> dto;
    /**
     * 分页,返回给前端的数据对象
     */
    @ApiModelProperty(value = "分页,返回给前端的数据对象")
    public Page<NifiCustomWorkflowVO> page;
}
