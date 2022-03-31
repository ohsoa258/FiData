package com.fisk.system.dto.roleinfo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class RolePageDTO {
    public String where;
    public Page<RoleInfoDTO> page;
}
