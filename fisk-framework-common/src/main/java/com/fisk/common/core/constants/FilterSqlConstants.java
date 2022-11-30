package com.fisk.common.core.constants;

/**
 * @author Lock
 * 筛选器获取表字段sql
 */
public class FilterSqlConstants {

    /**
     * 业务域
     */
    public static final String BUSINESS_AREA_SQL = " where Field not in ('id','del_flag','create_user','update_user')";
    /**
     * 应用注册
     */
    public static final String APP_REGISTRATION_SQL = " where Field in ('app_name','app_des','app_type','app_principal','create_time')";
    /**
     * 物理表
     */
    public static final String TABLE_ACCESS_SQL = " where Field in ('table_name','table_des','update_time')";
    /**
     * 同步方式
     */
    public static final String TABLE_SYNCMODE_SQL = " where Field in ('sync_field')";
    /**
     * 调度中心-管道服务
     */
    public static final String CUSTOM_WORKFLOW_SQL = " where Field in ('workflow_name','status','create_time','create_user','update_time','update_user')";
    /**
     * 系统管理--用户关联
     */
    public static final String USER_INFO_SQL = " where Field not in('id','del_flag','create_user','update_user','password','error_number','error_time')";
    /**
     * 数据服务--应用注册
     */
    public static final String DS_APP_REGISTRATION_SQL = " where Field in('app_name','app_principal','app_desc','create_time','create_user')";
    /**
     * 授权中心--客户端注册
     */
    public static final String TB_CLIENT_REGISTER_SQL = " where Field in('client_name','valid','token_des','create_time','create_user')";
    /**
     * 数据目标
     */
    public static final String DATA_TARGET = " where Field in('name','principal','description','host','port','api_address')";
    /**
     * 数据目标应用
     */
    public static final String DATA_TARGET_APP = " where Field in('name','principal','email','description','create_time')";
    /**
     * 平台管理--数据源
     */
    public static final String PLATFORM_DATASOURCE_SQL = " where Field in('name','con_dbname','con_type','principal')";
    /**
     * 数据质量--质量报告
     */
    public static final String DG_REPORT_SQL = " where Field in('report_name','report_type_name','report_principal')";

}
