package com.fisk.datamodel.dto.businesslimited;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    public String factId;
    public String id;

    public Page<BusinessLimitedDTO> page;
}
