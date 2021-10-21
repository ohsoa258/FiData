package com.fisk.chartvisual.entity;

import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BaseChartProperty extends BasePO {
    public Long fid;
    public String name;
    public String content;
    public String details;
    public byte[] image;
}
