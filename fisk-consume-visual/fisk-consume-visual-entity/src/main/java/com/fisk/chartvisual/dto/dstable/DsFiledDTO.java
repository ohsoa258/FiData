package com.fisk.chartvisual.dto.dstable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/3/9 17:27
 */
@Data
@NoArgsConstructor
public class DsFiledDTO {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "目标名称")
    private String targetName;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "子类")
    private List<DsFiledDTO> children;

    public DsFiledDTO(Integer id, String name, String targetName, String type) {
        this.id = id;
        this.name = name;
        this.targetName = targetName;
        this.type = type;
    }
}
