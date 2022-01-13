package com.fisk.dataservice.vo.datasource;

import com.fisk.dataservice.enums.DimensionTypeEnum;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description HierarchyVO
 * @date 2022/1/6 14:51
 */
@Data
public class HierarchyVO {
    public String name;
    public String uniqueName;
    public DimensionTypeEnum dimensionType;
}
