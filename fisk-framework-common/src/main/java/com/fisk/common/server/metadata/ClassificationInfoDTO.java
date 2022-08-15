package com.fisk.common.server.metadata;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class ClassificationInfoDTO {

    public String name;

    public String description;

    /**
     * 来源类型：1数据接入 2数仓建模
     */
    public int sourceType;

    /**
     * 是否删除
     */
    public boolean delete;

}
