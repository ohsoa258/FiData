package com.fisk.dataaccess.dto.sapbw;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lsj
 * @description sapbw的对象，装载单个cube下的所有维度和指标
 * @date 2022/5/27 15:36
 */
@Data
public class CubeDimsAndMeas {

    /**
     * 单个cube下的所有维度
     */
    @ApiModelProperty(value = "单个cube下的所有维度")
    public List<CubeDim> cubeDimList;

    /**
     * 单个cube下的所有指标
     */
    @ApiModelProperty(value = "单个cube下的所有指标")
    public List<CubeMes> cubeMeaList;

}
