package com.fisk.datamodel.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessPageDTO {
    public String key;
    public List<BusinessQueryDTO> dto;
    public Page<BusinessPageResultDTO> page;
}
