package com.fisk.datafactory.dto.components;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class ChannelDataDTO {
    /**
     * 应用id or 业务域id
     */
    public long id;
    /**
     * 应用名 or 业务域名
     */
    public String businessName;

    @ApiModelProperty(value = "数据湖类型")
    public String type;

    public List<ChannelDataChildDTO> list;
}
