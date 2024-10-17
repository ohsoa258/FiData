package com.fisk.common.core.utils.office.easyExcel;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;

import lombok.Data;

/**
 * 数据接入-数据湖管理-导出配置库数据
 *
 * 应用名称 库名  架构名 表名 字段名 中文字段名 英文字段名 字段描述 字段类型  是否主键
 *
 * @author sjl
 **/
@Data
public class AccessCDCExcelDTO implements Serializable {

    @ExcelProperty(value = "应用名称")
    private String appName;

    @ExcelProperty(value = "库名")
    private String dbName;

    @ExcelProperty(value = "架构名")
    private String schemaName;

    @ExcelProperty(value = "表名")
    private String tableName;

    @ExcelProperty(value = "字段名")
    private String columnName;

    @ExcelProperty(value = "中文字段名")
    private String columnNameCn;

    @ExcelProperty(value = "英文字段名")
    private String columnNameEn;

    @ExcelProperty(value = "字段描述")
    private String columnDesc;

    @ExcelProperty(value = "字段类型")
    private String columnType;

    @ExcelProperty(value = "是否主键")
    private Integer isPrimaryKey;

}