package com.fisk.chartvisual.dto.dstable;

import com.fisk.chartvisual.enums.DataSourceInfoTypeEnum;
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

    private String name;
    private Integer count;
    private List<DsTableDTO> children;
    private DataSourceInfoTypeEnum type;

    public DsTableDTO(String name, Integer count,DataSourceInfoTypeEnum type) {
        this.name = name;
        this.count = count;
        this.type = type;
    }
}
