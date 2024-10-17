package com.fisk.datagovernance.dto.dataquality.datacheck;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.datagovernance.vo.dataquality.datacheck.AppRegisterVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version v1.0
 * @description 应用筛选器 DTO
 * @date 2022/1/6 14:51
 */
public class AppRegisterPageDTO
{
    /**
     * 条件
     */
    @ApiModelProperty(value = "条件")
    public String where;

    /**
     * 分页
     */
    @ApiModelProperty(value = "分页")
    public Page<AppRegisterVO> page;
}
