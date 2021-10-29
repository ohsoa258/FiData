package com.fisk.dataservice.dto;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/10/22 16:05
 * 获取切片器数据
 */
@Data
public class SlicerDTO {

    private Integer FiledId;
    private String FiledName;
    private Integer isDimension;
}
