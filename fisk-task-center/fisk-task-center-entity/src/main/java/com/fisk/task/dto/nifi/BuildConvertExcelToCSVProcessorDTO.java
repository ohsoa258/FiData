package com.fisk.task.dto.nifi;

import lombok.Data;

/**
 * @author cfk
 */
@Data
public class BuildConvertExcelToCSVProcessorDTO extends BaseProcessorDTO {
    /*
     * 第几行开始
     * */
    public int numberOfRowsToSkip;
    /*
     * 是否用原始数值
     * */
    public boolean formatCellValues;
    /*
     * 数据格式
     * */
    public String csvFormat;

    /*
     * 第一行是否是表头
     * */
    public boolean includeHeaderLine;


}
