package com.fisk.datamodel.dto.businessarea;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.filter.dto.FilterQueryDTO;
import com.fisk.datamodel.dto.businessarea.BusinessPageResultDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessQueryDTO {
    /**
     * 查询具体值
     */
    public String key;
    public List<FilterQueryDTO> dto;
    /**
     * 分页,返回给前端的数据对象
     */
    public Page<BusinessPageResultDTO> page;
}
