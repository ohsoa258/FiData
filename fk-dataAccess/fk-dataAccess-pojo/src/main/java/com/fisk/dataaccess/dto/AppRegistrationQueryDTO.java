package com.fisk.dataaccess.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.filter.dto.FilterQueryDTO;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class AppRegistrationQueryDTO {

    /**
     * 查询字段值
     */
    @ApiModelProperty(value = "模糊查询的字段值", required = true)
    public String key;
    public List<FilterQueryDTO> dto;
    /**
     * 分页,返回给前端的数据对象
     */
    public Page<AppRegistrationVO> page;
}
