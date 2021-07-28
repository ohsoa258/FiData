package com.fisk.common.constants;

/**
 * @author Lock
 * 过滤器获取表字段sql
 */
public class FilterSqlConstants {

    /**
     * 业务域
     */
    public static final String BUSINESS_AREA_SQL = " where Field not in ('id','del_flag','create_user','update_user')";
    /**
     *  应用注册
     */
    public static final String APP_REGISTRATION_SQL = " where Field in ('app_name','app_des','app_type','app_principal','create_time')";




}
