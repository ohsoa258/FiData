package com.fisk.mdm.dto.attribute;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.mdm.vo.attribute.AttributeVO;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author ChenYa
 */
public class AttributeQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<AttributeVO> page;
}
