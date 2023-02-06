package com.fisk.common.core.utils.Dto.Excel;

import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/8/17 14:19
 */
@Data
public class SheetDataDto {
    public List<String> columns;

    public List<List<String>> columnData;
}
