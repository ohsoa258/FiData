package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/5/19 14:06
 */
@Data
public class ApiSelectDTO {

    @ApiModelProperty(value = "应用主键")
    public Long id;
    @ApiModelProperty(value = "应用名称")
    public String appName;
    @ApiModelProperty(value = "应用类型  (0:实时应用  1:非实时应用)")
    public int appType;

//    public List<ApiSelectChildDTO> apiSelectChildren;
}
