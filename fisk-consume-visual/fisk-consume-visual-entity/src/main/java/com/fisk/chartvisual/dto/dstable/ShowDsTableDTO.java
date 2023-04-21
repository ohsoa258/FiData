package com.fisk.chartvisual.dto.dstable;

import com.fisk.chartvisual.enums.DataSourceInfoTypeEnum;
import com.fisk.chartvisual.enums.isCheckedTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/3/17 17:42
 */
@NoArgsConstructor
@Data
public class ShowDsTableDTO {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "合计")
    private Integer count;

    @ApiModelProperty(value = "子类")
    private List<ShowDsTableDTO> children;

    @ApiModelProperty(value = "类型")
    private DataSourceInfoTypeEnum type;
    /**
     * 是否选中
     */
    @ApiModelProperty(value = "是否选中")
    private isCheckedTypeEnum isChecked;

    public ShowDsTableDTO(Integer id,String name, Integer count, DataSourceInfoTypeEnum type, isCheckedTypeEnum isChecked) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.type = type;
        this.isChecked = isChecked;
    }
}
