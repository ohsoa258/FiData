package com.fisk.datamodel.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessPage {
    public String where;
    public Page<BusinessPageResultDTO> page;
}
