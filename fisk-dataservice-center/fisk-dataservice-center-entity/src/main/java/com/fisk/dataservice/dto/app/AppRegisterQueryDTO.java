package com.fisk.dataservice.dto.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.filter.dto.FilterQueryDTO;
import com.fisk.dataservice.vo.app.AppRegisterVO;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 应用筛选器查询 DTO
 * @date 2022/1/6 14:51
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
