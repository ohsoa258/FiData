package com.fisk.common.service.dbMetaData.dto;

import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 表元数据查询
 * @date 2022/7/6 15:11
 */
@Data
public class FiDataTableMetaDataReqDetailDTO {
    /**
     * 表ID
     */
    @ApiModelProperty(value = "表ID")
    public String tableUnique;

    /**
     * 表业务类型
     */
    @ApiModelProperty(value = "表业务类型")
    public TableBusinessTypeEnum tableBusinessType;
}
