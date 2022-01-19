package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description api编辑 DTO
 * @date 2022/1/15 16:52
 */
public class ApiRegisterEditDTO {
    /**
     * api信息
     */
    @ApiModelProperty(value = "api信息")
    @NotNull()
    public ApiConfigEditDTO apiDTO;

    /**
     * 字段列表
     */
    @ApiModelProperty(value = "字段列表")
    @NotNull()
    public List<FieldConfigDTO> fieldDTO;

    /**
     * 条件列表
     */
    @ApiModelProperty(value = "条件列表")
    public List<FilterConditionConfigDTO> whereDTO;

    /**
     * 参数列表
     */
    @ApiModelProperty(value = "参数列表")
    public List<ParmConfigEditDTO> parmDTO;
}
