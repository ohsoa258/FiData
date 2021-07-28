package com.fisk.dataaccess.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.filter.dto.FilterQueryDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class AppRegistrationQueryDTO {
    /**
     * 查询具体值
     */
    public String key;
    public List<FilterQueryDTO> dto;
    /**
     * 分页,返回给前端的数据对象
     */
    public Page<AppRegistrationDTO> page;
}
