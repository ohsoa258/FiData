package com.fisk.dataservice.dto.tableservice;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.dataservice.vo.app.AppRegisterVO;
import com.fisk.dataservice.vo.tableservice.TableAppVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class TableAppQueryDTO {
    /**
     * 筛选器对象
     */
    @ApiModelProperty(value = "筛选器对象")
    public List<FilterQueryDTO> dto;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<TableAppVO> page;
}
