package com.fisk.datamodel.dto.businessarea;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Lock
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BusinessNameDTO extends BaseDTO {

    /**
     * 业务域id
     */
    @ApiModelProperty(value = "id")
    public long id;

    /**
     * 业务域名称
     */
    @ApiModelProperty(value = "业务名称")
    public String businessName;

}
