package com.fisk.dataservice.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class IsDimensionDTO {

    public Integer fieldIdOne;
    /**
     * 是否维度 0 否  1 是维度
     */
    public int dimensionOne;

    public Integer fieldIdTwo;
    /**
     * 是否维度 0 否  1 是维度
     */
    public int dimensionTwo;
}
