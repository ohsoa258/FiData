package com.fisk.dataaccess.dto.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class AppRegistrationPageDTO {
    public String where;
    public Page<AppRegistrationVO> page;
}
