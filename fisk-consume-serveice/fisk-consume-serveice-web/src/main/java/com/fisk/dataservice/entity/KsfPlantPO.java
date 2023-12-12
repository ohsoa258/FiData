package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangjian
 * @date 2023-12-11 14:22:15
 */
@TableName("tb_ksf_plant")
@Data
public class KsfPlantPO extends BasePO {

    @ApiModelProperty(value = "仓库代码")
    private String lgpla;

    @ApiModelProperty(value = "仓库名称")
    private String name;

    @ApiModelProperty(value = "源系统id")
    private String sourcesys;
}
