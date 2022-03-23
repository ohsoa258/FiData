package com.fisk.datamanagement.dto.glossary;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class GlossaryAnchorDTO {
    @ApiModelProperty(value = "术语库guid(添加类别/术语时必填)")
    public String glossaryGuid;
    @ApiModelProperty(value = "术语库名称(添加类别/术语时必填)")
    public String displayText;
    @ApiModelProperty(value = "返回类别/术语详情时参数")
    public String relationGuid;

}
