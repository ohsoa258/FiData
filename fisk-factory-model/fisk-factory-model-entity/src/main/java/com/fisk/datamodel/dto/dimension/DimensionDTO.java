package com.fisk.datamodel.dto.dimension;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionDTO {
    @ApiModelProperty(value = "Id")
    public int id;
    /**
     * 维度文件夹id
     */
    @ApiModelProperty(value = "维度文件夹id")
    public int dimensionFolderId;
    @ApiModelProperty(value = "业务Id")
    public int businessId;
    @ApiModelProperty(value = "维度中文名称")
    public String dimensionCnName;
    @ApiModelProperty(value = "维度表名称")
    public String dimensionTabName;
    @ApiModelProperty(value = "维度详细信息")
    public String dimensionDesc;
    @ApiModelProperty(value = "共享")
    public boolean share;

    /**
     * 发布状态：1:未发布、2：发布成功、3：发布失败
     */
    @ApiModelProperty(value = "发布状态：1:未发布、2：发布成功、3：发布失败")
    public int isPublish;
    /**
     * 是否生成时间表
     */
    @ApiModelProperty(value = "是否生成时间表")
    public boolean timeTable;
    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    public String startTime;
    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    public String endTime;

    /*
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;


}
