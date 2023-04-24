package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationTreeDTO {

    @ApiModelProperty(value = "guid")
    public String guid;

    @ApiModelProperty(value = "名称")
    public String name;

    public List<ClassificationTreeDTO> child;

}
