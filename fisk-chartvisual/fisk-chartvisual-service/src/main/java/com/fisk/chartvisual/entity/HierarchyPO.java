package com.fisk.chartvisual.entity;

import com.fisk.chartvisual.enums.MatrixElemTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 层级
 *
 * @author JinXingWang
 */
@Data
public class HierarchyPO {
    public String name;
    public String uniqueName;
    public String uniqueNameAll;
    public String uniqueNameAllMember;
    public MatrixElemTypeEnum dimensionTypeEnum;
    public List<MemberPO> members;
}
