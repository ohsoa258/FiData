package com.fisk.dataaccess.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class AppRegistrationPageDTO {
    public String where;
    public Page<AppRegistrationDTO> page;
}
