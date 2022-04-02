package com.fisk.chartvisual.dto.contentsplit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/11/16 13:56
 */
@Data
public class ContentDTO {
    @JsonProperty("bdColor")
    private String bdColor;
    private Integer height;
    private Integer width;
    @JsonProperty("selfAdaptWidth")
    private Boolean selfAdaptWidth;
    @JsonProperty("selfAdaptHeight")
    private Boolean selfAdaptHeight;
    private Integer bigid;
    @JsonProperty("listSlicer")
    private List<ListSlicer> listSlicer;
    @JsonProperty("bigTyple")
    private Integer bigTyple;
}
