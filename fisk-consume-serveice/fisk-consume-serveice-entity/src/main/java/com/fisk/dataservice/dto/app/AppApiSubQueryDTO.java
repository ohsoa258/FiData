package com.fisk.dataservice.dto.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataservice.vo.app.AppApiSubVO;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 应用订阅API查询 DTO
 * @date 2022/1/6 14:51
 */
public class AppApiSubQueryDTO {
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    @NotNull()
    public Integer appId;

    @ApiModelProperty(value = "类型：1api服务 2表服务 3 文件服务")
    @NotNull()
    public Integer type;

    /**
     * 关键字
     */
    @ApiModelProperty(value = "关键字")
    public String keyword;

    /**
     * 分页信息
     */
    @ApiModelProperty(value = "分页信息")
    public Page<AppApiSubVO> page;
}
