package com.fisk.datamanagement.dto.classification;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationDefContentDTO {
    @ApiModelProperty(value = "业务分类id")
    public String id;

    @ApiModelProperty(value = "业务分类父级id")
    public String pid;

    @ApiModelProperty(value = "业务分类id")
    public String guid;

    @ApiModelProperty(value = "分类名称,添加后不可编辑")
    public String name;

    @ApiModelProperty(value = "分类描述(添加时可选参数)")
    public String description;

    @ApiModelProperty(value = "创建时间(不传值,后台生成)")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "业务分类下级")
    public List<String> subTypes;

    @ApiModelProperty(value = "业务分类上级(添加时可选参数)")
    public List<String> superTypes;

    public List<ClassificationAttributeDefsDTO> attributeDefs;
}
