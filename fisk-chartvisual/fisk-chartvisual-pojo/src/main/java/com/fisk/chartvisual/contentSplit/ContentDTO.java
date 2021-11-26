package com.fisk.chartvisual.contentSplit;

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
    private int height;
    private int width;
    @JsonProperty("selfAdaptWidth")
    private boolean selfAdaptWidth;
    @JsonProperty("selfAdaptHeight")
    private boolean selfAdaptHeight;
    private int bigid;
    @JsonProperty("listSlicer")
    private List<ListSlicer> listSlicer;
    @JsonProperty("bigTyple")
    private int bigTyple;
}
