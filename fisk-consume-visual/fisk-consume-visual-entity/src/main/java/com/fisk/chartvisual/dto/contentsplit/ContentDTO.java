package com.fisk.chartvisual.dto.contentsplit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/16 13:56
 */
@Data
public class ContentDTO {
    @ApiModelProperty(value = "背景颜色")
    @JsonProperty("bdColor")
    private String bdColor;
    @ApiModelProperty(value = "高度")
    private Integer height;
    @ApiModelProperty(value = "宽度")
    private Integer width;
    @ApiModelProperty(value = "自适应宽度")
    @JsonProperty("selfAdaptWidth")
    private Boolean selfAdaptWidth;

    @ApiModelProperty(value = "自适应高度")
    @JsonProperty("selfAdaptHeight")
    private Boolean selfAdaptHeight;

    @ApiModelProperty(value = "大id")
    private Integer bigid;

    @ApiModelProperty(value = "列表切片")
    @JsonProperty("listSlicer")
    private List<ListSlicer> listSlicer;
    @JsonProperty("大元组")
    @ApiModelProperty(value = "id")
    private Integer bigTyple;
}
