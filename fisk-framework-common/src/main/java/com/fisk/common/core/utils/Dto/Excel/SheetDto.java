package com.fisk.common.core.utils.Dto.Excel;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description Sheet
 * @date 2022/8/15 16:22
 */
@Data
public class SheetDto {
    public String sheetName;

    // 标识行
    public List<RowDto> singRows;

    // 标识字段
    public List<String> singFields;

    // 数据行
    public List<List<String>> dataRows;
}
