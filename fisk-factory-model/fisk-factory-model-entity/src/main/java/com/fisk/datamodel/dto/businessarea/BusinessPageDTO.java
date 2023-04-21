package com.fisk.datamodel.dto.businessarea;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessPageDTO {
    @ApiModelProperty(value = "where")
    public String where;
    @ApiModelProperty(value = "分页")
    public Page<BusinessPageResultDTO> page;
}
