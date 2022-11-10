package com.fisk.common.core.utils.Dto.Excel;

import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 行信息
 * @date 2022/8/15 16:23
 */
@Data
public class RowDto {
    public int rowIndex;

    public List<String> columns;
}
