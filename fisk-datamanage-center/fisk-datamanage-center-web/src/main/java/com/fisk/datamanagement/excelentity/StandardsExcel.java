package com.fisk.datamanagement.excelentity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2024-02-21
 * @Description:
 */
@Data
public class StandardsExcel {
    @Excel(name = "属性名称")
    private String name;

    @Excel(name = "属性英文名称")
    private String englishName;

    @Excel(name = "属性描述")
    private String description;

    @Excel(name = "字段类型")
    private String fieldType;

    @Excel(name = "数据元编号")
    private String datametaCode;

    @Excel(name = "质量规则")
    private String qualityRule;
}
