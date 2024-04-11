package com.fisk.datamanagement.dto.assetschangeanalysis;

import com.fisk.datamanagement.enums.EntityTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CategoryDetailChangesDTO {

    /**
     * 元数据类型编码
     */
    @ApiModelProperty(value = "元数据类型编码")
    private EntityTypeEnum type;

    /**
     * 元数据类型名称
     */
    @ApiModelProperty(value = "元数据类型名称")
    private String typeName;

    /**
     * 新增个数
     */
    @ApiModelProperty(value = "新增个数")
    private Integer addCount;

    /**
     * 修改个数
     */
    @ApiModelProperty(value = "修改个数")
    private Integer updateCount;

    /**
     * 删除个数
     */
    @ApiModelProperty(value = "删除个数")
    private Integer delCount;




}
