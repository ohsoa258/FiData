package com.fisk.datamanagement.dto.label;

import com.fisk.datamanagement.enums.DataAssetsTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.lang.reflect.Type;


/**
 * @author JinXingWang
 */
@Data
public class GlobalSearchDto {
    @ApiModelProperty(value = "pid")
    public int pid;
    @ApiModelProperty(value = "id")
    public int id;
    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "类型 1属性 2属性分类 3术语 4术语分类")
    public DataAssetsTypeEnum type;
    @ApiModelProperty(value = "路径")
    public String path;
    @ApiModelProperty(value = "属性分类code")
    public String categoryCode;
    @ApiModelProperty(value = "属性分类父级code")
    public String categoryParentCode;
}
