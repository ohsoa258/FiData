package com.fisk.dataservice.entity;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 模型
 * @date 2022/1/6 14:51
 */
public class CubePO {
    public String name;
    public String uniqueName;
    public List<DimensionPO> dimensions;
    public List<MeasurePO> measures;
}
