package com.fisk.chartvisual.dto.dstable;

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

    private Integer id;
    private String name;
    private String targetName;
    private String type;
    private List<DsFiledDTO> children;

    public DsFiledDTO(Integer id, String name, String targetName, String type) {
        this.id = id;
        this.name = name;
        this.targetName = targetName;
        this.type = type;
    }
}
