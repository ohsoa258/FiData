package com.fisk.chartvisual.dto.stringinterception;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/3/1 15:45'
 * 报表json拆分2.0
 */
@Data
public class ContextDTO {

    @ApiModelProperty(value = "acceptCanvasConfigInfo")
    private AcceptCanvasConfigInfo acceptCanvasConfigInfo;

    @ApiModelProperty(value = "curSelectDataSourceType")
    private CurSelectDataSourceType curSelectDataSourceType;
    public void setAcceptCanvasConfigInfo(AcceptCanvasConfigInfo acceptCanvasConfigInfo) {
        this.acceptCanvasConfigInfo = acceptCanvasConfigInfo;
    }
    public AcceptCanvasConfigInfo getAcceptCanvasConfigInfo() {
        return acceptCanvasConfigInfo;
    }

    public void setCurSelectDataSourceType(CurSelectDataSourceType curSelectDataSourceType) {
        this.curSelectDataSourceType = curSelectDataSourceType;
    }
    public CurSelectDataSourceType getCurSelectDataSourceType() {
        return curSelectDataSourceType;
    }

}