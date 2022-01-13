package com.fisk.dataservice.dto.datasource;

import com.fisk.dataservice.enums.DimensionTypeEnum;
import com.fisk.dataservice.enums.DragElemTypeEnum;

/**
 * @author dick
 * @version v1.0
 * @description
 * @date 2022/1/6 14:51
 */
public class ColumnDetailsSsas {
    public String name;
    public String uniqueName;
    public DimensionTypeEnum dimensionType;
    public DragElemTypeEnum dragElemType;
}
