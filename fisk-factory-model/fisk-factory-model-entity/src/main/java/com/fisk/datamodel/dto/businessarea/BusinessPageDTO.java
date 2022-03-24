package com.fisk.datamodel.dto.businessarea;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessPageDTO {
    public String where;
    public Page<BusinessPageResultDTO> page;
}
