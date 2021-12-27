package com.fisk.dataaccess.dto.ftp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 1.0
 * @description ftp数据源 excel对象
 * @date 2021/12/27 16:43
 */
@Data
public class ExcelDTO {

    @ApiModelProperty(value = "excel预览内容")
    public List<List<String>> excelContent;

    @ApiModelProperty(value = "excel字段列表")
    public List<String> excelField;

    @ApiModelProperty(value = "sheet名称")
    public String sheetName;
}
