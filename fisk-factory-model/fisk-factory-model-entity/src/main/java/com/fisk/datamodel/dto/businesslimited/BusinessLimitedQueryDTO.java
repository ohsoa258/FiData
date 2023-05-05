package com.fisk.datamodel.dto.businesslimited;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * @author cfk
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessLimitedQueryDTO {
    /**
     * 事实表id
     */
    @ApiModelProperty(value = "事实id")
    public String factId;

    @ApiModelProperty(value = "id")
    public String id;

    @ApiModelProperty(value = "分页")
    public Page<BusinessLimitedDTO> page;
}
