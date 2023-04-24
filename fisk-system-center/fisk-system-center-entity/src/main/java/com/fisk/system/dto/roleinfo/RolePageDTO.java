package com.fisk.system.dto.roleinfo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class RolePageDTO {
    @ApiModelProperty(value = "where")
    public String where;
    @ApiModelProperty(value = "分页")
    public Page<RoleInfoDTO> page;
}
