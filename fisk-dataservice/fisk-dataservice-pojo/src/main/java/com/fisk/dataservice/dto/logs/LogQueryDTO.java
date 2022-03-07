package com.fisk.dataservice.dto.logs;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.vo.logs.ApiLogVO;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 查询日志
 * @date 2022/3/7 12:14
 */
public class LogQueryDTO
{
    /**
     * API ID
     */
    @ApiModelProperty(value = "apiId")
    public List<Integer> apiIds;

    /**
     * APP ID
     */
    @ApiModelProperty(value = "appId")
    public Integer appId;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<ApiLogVO> page;
}
