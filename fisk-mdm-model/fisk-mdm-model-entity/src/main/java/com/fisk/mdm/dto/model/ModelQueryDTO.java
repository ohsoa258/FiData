package com.fisk.mdm.dto.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.mdm.vo.model.ModelVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author ChenYa
 */
@Data
public class ModelQueryDTO {
    /**
     * 搜索条件
     */
    @ApiModelProperty(value = "搜索条件")
    public String keyword;

    /**
     * 分页对象
     */
    @ApiModelProperty(value = "分页对象")
    public Page<ModelVO> page;
}
