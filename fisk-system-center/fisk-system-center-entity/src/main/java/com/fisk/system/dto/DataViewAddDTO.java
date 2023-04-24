package com.fisk.system.dto;

import com.fisk.system.enums.serverModuleTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/4 16:24
 */
@Data
public class DataViewAddDTO {
    @ApiModelProperty(value = "视图名称")
    @NotNull
    private String viewName;
    @ApiModelProperty(value = "服务器模块")
    private serverModuleTypeEnum serverModule;
    @ApiModelProperty(value = "用户Id")
    private Integer userId;
    @ApiModelProperty(value = "视图类型")
    private String viewType;
    @ApiModelProperty(value = "过滤DTO")
    private List<DataViewFilterDTO> filterDTO;
}
