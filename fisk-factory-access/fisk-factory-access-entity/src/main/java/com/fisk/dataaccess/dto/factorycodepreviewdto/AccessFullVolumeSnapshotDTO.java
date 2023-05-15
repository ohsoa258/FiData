package com.fisk.dataaccess.dto.factorycodepreviewdto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lishiji
 * @createtime 2023-05-08 11:39
 */
@Data
public class AccessFullVolumeSnapshotDTO {

    /**
     * 全量覆盖方式：是否开启快照
     *  1是     0否
     */
    @ApiModelProperty(value = "全量覆盖方式：是否开启快照:0否 1是")
    public int ifEnableSnapshot;

    /**
     * 快照时间范围
     */
    @ApiModelProperty(value = "全量覆盖方式：是否开启快照:0否 1是")
    public int dateRange;

    /**
     * 快照时间单位：
     *  年:YEAR
     *  季:QUARTER
     *  月:MONTH
     *  周:WEEK
     *  日:DAY
     */
    @ApiModelProperty(value = "快照时间单位")
    public String dateUnit;

    /**
     * 版本号生成逻辑：
     *  0:当前年/季/月/周/日
     *  1：自定义
     */
    @ApiModelProperty(value = "版本号生成逻辑: 0、当前年/季/月/周/日  1、自定义")
    public int logicType;

    /**
     * 自定义版本号sql
     */
    @ApiModelProperty(value = "自定义版本号sql")
    public String snapshotCostumeSql;

}
