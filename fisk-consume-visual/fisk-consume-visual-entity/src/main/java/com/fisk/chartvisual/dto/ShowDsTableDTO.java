package com.fisk.chartvisual.dto;

import com.fisk.chartvisual.enums.DataSourceInfoTypeEnum;
import com.fisk.chartvisual.enums.isCheckedTypeEnum;
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

    private Integer id;
    private String name;
    private Integer count;
    private List<ShowDsTableDTO> children;
    private DataSourceInfoTypeEnum type;
    /**
     * 是否选中
     */
    private isCheckedTypeEnum isChecked;

    public ShowDsTableDTO(Integer id,String name, Integer count, DataSourceInfoTypeEnum type, isCheckedTypeEnum isChecked) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.type = type;
        this.isChecked = isChecked;
    }
}
