package com.fisk.datamodel.dto.businessarea;

import com.fisk.common.dto.BaseDTO;
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
    public long id;

    /**
     * 业务域名称
     */
    public String businessName;

}
