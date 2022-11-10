package com.fisk.dataaccess.dto.v3;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TbTableAccessQueryDTO {

    @ApiModelProperty(value = "表名")
    public String tableName;

    @ApiModelProperty(value = "同步类型")
    public Integer syncMode;

    @ApiModelProperty(value = "发布状态")
    public Integer publish;

    @ApiModelProperty(value = "应用id")
    public Integer appId;

    public Page<TbTableAccessDTO> page;

}
