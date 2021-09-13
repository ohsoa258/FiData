package com.fisk.datamodel.dto.dimension;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class


DimensionDTO {
    public int id;
    public int businessId;
    /**
     * 应用id
     */
    public int appId;
    public int tableSourceId;
    public String dimensionCnName;
    public String dimensionEnName;
    public String dimensionTabName;
    public String dimensionDesc;
    public boolean share;
    /**
     * 发布状态：1:未发布、2：发布成功、3：发布失败
     */
    public int isPublish;
}
