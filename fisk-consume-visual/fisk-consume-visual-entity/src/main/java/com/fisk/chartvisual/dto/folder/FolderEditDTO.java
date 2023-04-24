package com.fisk.chartvisual.dto.folder;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class FolderEditDTO {

    @ApiModelProperty(value = "id")
    public Integer id;

    @ApiModelProperty(value = "名称")
    public String name;
}
