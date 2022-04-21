package com.fisk.mdm.vo.model;

import com.fisk.mdm.vo.entity.EntityVO;
import lombok.Data;

import java.util.List;

/**
 * @author ChenYa
 * @date 2022/04/21
 */
@Data
public class ModelInfoVO {
    private Integer id;
    private String name;
    private String displayName;
    private String desc;
    private Integer logRetentionDays;
    public String logoPath;

    List<EntityVO> entityVOList;
}
