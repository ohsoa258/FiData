package com.fisk.chartvisual.dto.chartvisual;

import com.fisk.chartvisual.dto.dstable.FieldInfoDTO;
import com.fisk.chartvisual.enums.isExistTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/3/7 16:42
 */
@Data
public class TableInfoDTO {

    /**
     * 表名
     */
    @ApiModelProperty(value = "表名")
    private String tableName;
    /**
     * 字段信息配置
     */
    @ApiModelProperty(value = "字段信息配置")
    private List<FieldInfoDTO> dtoList;
    /**
     * 表是否存在库里
     */
    @ApiModelProperty(value = "表是否存在库里")
    private isExistTypeEnum isExist;
}
