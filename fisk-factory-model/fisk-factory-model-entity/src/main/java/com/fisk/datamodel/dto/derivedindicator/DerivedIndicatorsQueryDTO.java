package com.fisk.datamodel.dto.derivedindicator;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DerivedIndicatorsQueryDTO {

    @ApiModelProperty(value = "事实Id")
    public long factId;

    /**
     * 返回类型
     */
    @ApiModelProperty(value = "返回类型")
   public Page<DerivedIndicatorsListDTO> dto;

}
