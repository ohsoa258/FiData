package com.fisk.datamodel.dto.widetableconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class WideTableListDTO {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "dorisPublish")
    public int dorisPublish;

    @ApiModelProperty(value = "字段列表")
    public List<String> fieldList;

}
