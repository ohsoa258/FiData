package com.fisk.dataservice.dto.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.vo.api.ApiConfigVO;
import io.swagger.annotations.ApiModelProperty;


/**
 * @author dick
 * @version v1.0
 * @description API查询 DTO
 * @date 2022/1/6 14:51
 */
public class ApiRegisterQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<ApiConfigVO> page;
}
