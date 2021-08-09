package com.fisk.chartvisual.entity;

import lombok.Data;

import java.util.List;

/**
 * 层级
 *
 * @author JinXingWang
 */
@Data
public class HierarchyPO {
    public String Name;
    public String UniqueName;
    public List<MemberPO> Members;
}
