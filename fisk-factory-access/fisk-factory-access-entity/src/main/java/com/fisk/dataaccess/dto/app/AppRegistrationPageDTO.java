package com.fisk.dataaccess.dto.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class AppRegistrationPageDTO {
    @ApiModelProperty(value = "where")
    public String where;
    @ApiModelProperty(value = "页")
    public Page<AppRegistrationVO> page;
}
