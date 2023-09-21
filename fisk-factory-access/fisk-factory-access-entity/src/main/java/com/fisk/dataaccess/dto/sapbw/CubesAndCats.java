package com.fisk.dataaccess.dto.sapbw;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lsj
 * @description sapbw的对象，装载cube的名称和cube的cat名称
 * @date 2022/5/27 15:36
 */
@Data
public class CubesAndCats {

    /**
     * cube名称
     */
    @ApiModelProperty(value = "cube名称")
    public List<String> cubeNames;

    /**
     * cat名称
     */
    @ApiModelProperty(value = "cat名称")
    public List<String> catNames;


}
