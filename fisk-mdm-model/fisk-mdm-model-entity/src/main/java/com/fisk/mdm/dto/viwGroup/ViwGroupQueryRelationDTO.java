package com.fisk.mdm.dto.viwGroup;

import com.fisk.mdm.dto.entity.EntityQueryDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/31 10:32
 * @Version 1.0
 */
@Data
public class ViwGroupQueryRelationDTO {

    private List<EntityQueryDTO> relationList;
    private List<ViwGroupCheckDTO> checkedArr;
}
