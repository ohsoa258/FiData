package com.fisk.datamanagement.dto.label;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class LabelDTO {
    @ApiModelProperty(value = "id")
    public int id;
    @ApiModelProperty(value = "标签中午名")
    public String labelCnName;
    @ApiModelProperty(value = "分类编号")
    public int categoryId;
    @ApiModelProperty(value = "分类名")
    public String categoryName;
    @ApiModelProperty(value = "标签英文名")
    public String labelEnName;
    @ApiModelProperty(value = "标签DES")
    public String labelDes;

    /**
     * 应用模块id集合
     */
    @ApiModelProperty(value = "应用模块id集合")
    public List<String> moduleIds;

    @ApiModelProperty(value = "applicationModule")
    public String applicationModule;
}
