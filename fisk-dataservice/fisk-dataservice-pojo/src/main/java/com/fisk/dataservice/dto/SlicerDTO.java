package com.fisk.dataservice.dto;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/10/22 16:05
 * 获取切片器数据
 */
@Data
public class SlicerDTO {

    private Integer filedId;
    private String filedName;
    private Integer isDimension;
}
