package com.fisk.chartvisual.dto.dstable;

import com.fisk.chartvisual.enums.DataSourceInfoTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/3/4 15:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DsTableDTO {

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "计数")
    private Integer count;

    @ApiModelProperty(value = "子类")
    private List<DsTableDTO> children;

    @ApiModelProperty(value = "类型")
    private DataSourceInfoTypeEnum type;

    public DsTableDTO(String name, Integer count,DataSourceInfoTypeEnum type) {
        this.name = name;
        this.count = count;
        this.type = type;
    }
}
