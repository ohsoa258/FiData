package com.fisk.common.service.pageFilter.dto;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetaDataConfigDTO {

    /**
     * 连接数据库url
     */
    public String url;

    /**
     * 登录账号
     */
    public String userName;

    /**
     * 登录密码
     */
    public String password;

    /**
     * 查询表名
     */
    public String tableName;

    /**
     * 查询表字段别名(用于多表联查)
     */
    public String tableAlias;

    /**
     * 过滤字段sql
     */
    public String filterSql;


}
