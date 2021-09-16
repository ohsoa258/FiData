package com.fisk.dataservice.dto;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/9/4 23:21
 */
@Data
public class OutParameterDTO {
    /**
     * 是否有关联 0 没关联 1有关联
     */
    public int whether;

    public String dimensionName;

    public String factName;

}
