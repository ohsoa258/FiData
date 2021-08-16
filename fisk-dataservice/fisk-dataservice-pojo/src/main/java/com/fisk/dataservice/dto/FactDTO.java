package com.fisk.dataservice.dto;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/16 15:06
 * 事实表 三级
 */
@Data
public class FactDTO {
    public String factTableEnName;
    public List<AtomicIndicatorsDTO> atomicIndicatorsList;
}
