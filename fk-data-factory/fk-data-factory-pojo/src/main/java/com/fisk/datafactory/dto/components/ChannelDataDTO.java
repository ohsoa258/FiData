package com.fisk.datafactory.dto.components;

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

    public List<ChannelDataChildDTO> list;
}
