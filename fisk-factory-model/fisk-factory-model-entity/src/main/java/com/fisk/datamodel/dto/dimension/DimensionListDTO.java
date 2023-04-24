package com.fisk.datamodel.dto.dimension;

import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDataDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionListDTO {

    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 维度中文名称
     */
    @ApiModelProperty(value = "维度中文名称")
    public String dimensionCnName;
    /**
     * 维度应用表名称
     */
    @ApiModelProperty(value = "维度应用表名称")
    public String dimensionTabName;
    /**
     * 维度临时表名称
     */
    @ApiModelProperty(value = "维度临时表名称")
    public String prefixTempName;
    /**
     * 发布状态：0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
     */
    @ApiModelProperty(value = "发布状态：0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布")
    public int isPublish;
    /**
     * 是否为时间维度表
     */
    @ApiModelProperty(value = "是否为时间维度表")
    public boolean timeTable;
    /**
     * 维度字段列表
     */
    @ApiModelProperty(value = "维度字段列表")
    public List<DimensionAttributeDataDTO> attributeList;

}
