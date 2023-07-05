package com.fisk.datamanagement.dto.glossary;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JinXingWang
 */
@Data
public class GlossaryCategoryPathDto {
     @ApiModelProperty(value = "父级id")
     public int pid;
     @ApiModelProperty(value = "id")
     public int id;
     @ApiModelProperty(value = "名称")
     public String name;
     @ApiModelProperty(value = "路径")
     public String path;
}
