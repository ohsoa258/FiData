package com.fisk.chartvisual.dto.folder;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class FolderDTO {

    @ApiModelProperty(value = "pid")
    public Integer pid;

    @ApiModelProperty(value = "名称")
    public String name;
}
