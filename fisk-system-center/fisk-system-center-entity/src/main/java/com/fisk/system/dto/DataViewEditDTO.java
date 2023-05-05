package com.fisk.system.dto;

import com.fisk.system.enums.serverModuleTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/4 17:56
 */
@Data
public class DataViewEditDTO {

    @ApiModelProperty(value = "id")
    @NotNull
    private Integer id;
    @ApiModelProperty(value = "视图名称")
    private String viewName;
    @ApiModelProperty(value = "服务器模块")
    private serverModuleTypeEnum serverModule;
    @ApiModelProperty(value = "视图类型")
    private String viewType;
    @ApiModelProperty(value = "用户id")
    private Integer userId;
    @ApiModelProperty(value = "查看过滤器DTO列表")
    private List<DataViewFilterDTO> viewFilterDTOList;
}
