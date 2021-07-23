package com.fisk.datamodel.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.filter.dto.FilterQueryDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessQueryDTO {
    public String key;
    public List<FilterQueryDTO> dto;
    public Page<BusinessPageResultDTO> page;
}
