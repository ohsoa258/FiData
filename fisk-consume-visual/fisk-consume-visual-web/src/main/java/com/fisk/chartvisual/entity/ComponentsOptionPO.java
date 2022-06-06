package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/2/9 14:50
 */
@Data
@TableName("tb_components_option")
public class ComponentsOptionPO extends BasePO {

    private Integer componentId;
    private String description;
    private String version;
    private String fileName;
    private String path;
}
