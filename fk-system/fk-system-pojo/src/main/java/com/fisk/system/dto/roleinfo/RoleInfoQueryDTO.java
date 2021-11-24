package com.fisk.system.dto.roleinfo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.filter.dto.FilterQueryDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class RoleInfoQueryDTO {
    /**
     * 筛选条件
     */
    public List<FilterQueryDTO> dto;
    /**
     * 返回结果
     */
    public Page<RoleInfoDTO> page;
}
