package com.fisk.datamanagement.dto.label;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class LabelQueryDTO {

    @ApiModelProperty(value = "分类编号")
    public int categoryId;

    @ApiModelProperty(value = "dto")
    public Page<LabelDataDTO> dto;
}
