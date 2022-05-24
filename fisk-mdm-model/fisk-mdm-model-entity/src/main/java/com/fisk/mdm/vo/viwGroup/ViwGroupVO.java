package com.fisk.mdm.vo.viwGroup;

import com.fisk.mdm.dto.viwGroup.ViwGroupDetailsDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:43
 * @Version 1.0
 */
@Data
public class ViwGroupVO {

    private Integer id;
    private Integer entityId;
    private String name;
    private String details;
    private List<ViwGroupDetailsDTO> groupDetailsList;
}
