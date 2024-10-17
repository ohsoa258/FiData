package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.datagovernance.vo.dataquality.datacheck.AppRegisterVO;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author wangjian
 * @version v1.0
 * @description 应用筛选器查询 DTO
 * @date 2024/10/14 14:51
 */
public class AppRegisterQueryDTO
{
    /**
     * 筛选器对象
     */
    @ApiModelProperty(value = "筛选器对象")
    public List<FilterQueryDTO> dto;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<AppRegisterVO> page;
}
