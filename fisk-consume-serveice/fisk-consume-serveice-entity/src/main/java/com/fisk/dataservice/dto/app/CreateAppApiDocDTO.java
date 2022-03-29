package com.fisk.dataservice.dto.app;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 生成应用api文档
 * @date 2022/1/28 18:53
 */
public class CreateAppApiDocDTO
{
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    @NotNull()
    public Integer appId;

    /**
     * 应用api信息
     */
//    @ApiModelProperty(value = "应用api信息")
//    @NotNull()
//    public List<AppApiSubDTO> appApiDto;
}
