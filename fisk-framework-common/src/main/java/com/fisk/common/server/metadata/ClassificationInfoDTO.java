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
     * 来源类型：1数据接入 2数仓建模 3API网关, 4 视图服务 5 数据库同步服务
     */
    public int sourceType;

    /**
     * 应用类型：来源类型数据接入时 (0:实时应用  1:非实时应用) ，
     */
    public Integer appType;
    /**
     * 是否删除
     */
    public boolean delete;

}
