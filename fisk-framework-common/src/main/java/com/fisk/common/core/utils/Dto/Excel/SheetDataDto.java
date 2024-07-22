package com.fisk.common.core.utils.Dto.Excel;

import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description SheetData
 * @date 2022/8/17 14:19
 */
@Data
public class SheetDataDto
{
    // 列头
    public List<String> columns;

    // 行数据
    public List<List<String>> columnData;
}
