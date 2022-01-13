package com.fisk.dataservice.entity;

import com.fisk.dataservice.enums.DimensionTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 层级
 * @date 2022/1/6 14:51
 */
@Data
public class HierarchyPO {
    public String name;
    public String uniqueName;
    public DimensionTypeEnum dimensionTypeEnum;
    public List<MemberPO> members;
}
