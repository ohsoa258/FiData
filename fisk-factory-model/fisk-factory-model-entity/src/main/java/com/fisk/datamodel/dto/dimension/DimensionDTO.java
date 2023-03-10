package com.fisk.datamodel.dto.dimension;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionDTO {
    public int id;
    /**
     * 维度文件夹id
     */
    public int dimensionFolderId;
    public int businessId;
    public String dimensionCnName;
    public String dimensionTabName;
    public String dimensionDesc;
    public boolean share;
    /**
     * 发布状态：1:未发布、2：发布成功、3：发布失败
     */
    public int isPublish;
    /**
     * 是否生成时间表
     */
    public boolean timeTable;
    /**
     * 开始时间
     */
    public String startTime;
    /**
     * 结束时间
     */
    public String endTime;

    /*
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;


}
