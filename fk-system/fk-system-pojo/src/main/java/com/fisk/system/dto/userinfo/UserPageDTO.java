package com.fisk.system.dto.userinfo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class UserPageDTO {
    public String where;
    public Page<UserDTO> page;
}
