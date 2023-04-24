package com.fisk.datamodel.dto.businessarea;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "key")
    public String key;
    @ApiModelProperty(value = "dto")
    public List<FilterQueryDTO> dto;
    /**
     * 分页,返回给前端的数据对象
     */
    @ApiModelProperty(value = "分页")
    public Page<BusinessPageResultDTO> page;
}
