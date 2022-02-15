package com.fisk.datamodel.dto.dimensionfolder;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionFolderPublishQueryDTO {
    public int businessAreaId;
    public List<Integer> dimensionIds;
    /**
     * 发布备注
     */
    public String remark;
    /**
     * 增量配置
     */
    public int syncMode;
    /**
     * 是否同步
     */
    public boolean openTransmission;
}
