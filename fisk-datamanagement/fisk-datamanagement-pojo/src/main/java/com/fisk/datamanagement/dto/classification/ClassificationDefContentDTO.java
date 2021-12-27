package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationDefContentDTO {
    public String guid;
    @ApiModelProperty(value = "分类名称,添加后不可编辑",required = true)
    public String name;
    @ApiModelProperty(value = "分类描述")
    public String description;
    @ApiModelProperty(value = "创建时间(不传值,后台生成)")
    public long createTime;
    public List<ClassificationAttributeDefsDTO> attributeDefs;
}
