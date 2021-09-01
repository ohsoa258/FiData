package com.fisk.datamodel.dto.DataDomain;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/16 15:06
 * 事实表 三级
 */
@Data
public class FactNameDTO {
    public Long factId;
    public String factTableEnName;
    /**
     * 1数据接入  2数据建模
     */
    public Integer flag;
}
