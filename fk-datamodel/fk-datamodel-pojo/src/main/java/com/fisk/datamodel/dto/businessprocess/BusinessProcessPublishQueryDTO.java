package com.fisk.datamodel.dto.businessprocess;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessProcessPublishQueryDTO {
    public int businessAreaId;
    public List<Integer> factIds;
    /**
     * 发布备注
     */
    public String remark;
    /**
     * 增量配置
     */
    public int syncMode;
}
