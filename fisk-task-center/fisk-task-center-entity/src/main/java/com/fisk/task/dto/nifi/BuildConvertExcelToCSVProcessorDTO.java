package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author cfk
 */
@Data
public class BuildConvertExcelToCSVProcessorDTO extends BaseProcessorDTO {
    /*
     * 第几行开始
     * */
    @ApiModelProperty(value = "第几行开始")
    public int numberOfRowsToSkip;
    /*
     * 是否用原始数值
     * */
    @ApiModelProperty(value = "是否用原始数值")
    public boolean formatCellValues;
    /*
     * 数据格式
     * */
    @ApiModelProperty(value = "数据格式")
    public String csvFormat;

    /*
     * 第一行是否是表头
     * */
    @ApiModelProperty(value = "第一行是否是表头")
    public boolean includeHeaderLine;

    /**
     * 第一个sheet页名称, nifi真实名 extract-sheets
     */
    @ApiModelProperty(value = "第一个sheet页名称, nifi真实名 extract-sheets")
    public String sheetName;

    /**
     * excel 开始读取数据行数 nifi真实名 excel-extract-first-row
     */
    @ApiModelProperty(value = "excel 开始读取数据行数 nifi真实名 excel-extract-first-row")
    public String startLine;

}
