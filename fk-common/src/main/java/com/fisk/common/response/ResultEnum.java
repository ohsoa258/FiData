package com.fisk.common.response;

/**
 * 请求返回结果类型
 *
 * @author gy
 */

public enum ResultEnum {

    /**
     * 返回码
     */
    SUCCESS(0, "成功"),
    REQUEST_SUCCESS(200, "请求成功"),
    UNAUTHORIZED(401, "未授权"),
    NOTFOUND(404, "未找到资源"),
    ERROR(500, "系统报错"),
    TIMEOUT(504, "请求超时"),
    UNKNOWN(999, "未知类型"),

    /**
     * 平台级错误码，1000开始
     */
    SAVE_DATA_ERROR(1001, "数据保存失败"),
    SAVE_VERIFY_ERROR(1002, "数据验证失败"),
    DATA_NOTEXISTS(1003, "数据不存在"),
    ENUM_TYPE_ERROR(1004, "错误的枚举类型"),
    UPDATE_DATA_ERROR(1005, "数据更新失败"),
    NAME_EXISTS(1006, "名称已存在"),
    PARAMTER_ERROR(1007, "请求参数有误"),
    DATA_EXISTS(1008, "数据已存在"),
    REMOTE_SERVICE_CALLFAILED(1009, "远程服务调用失败"),
    SERVER_FUSE(1010, "服务熔断"),
    PARAMTER_NOTNULL(1011, "参数不能为空"),
    Table_NAME_EXISTS(1012, "表名已存在"),
    NOTFOUND_REQUEST(1013, "未找到请求"),
    NIFI_NOT_FIND_DATA(1014, "nifi查不到数据"),
    LOGIN_ACCOUNT_DISABLED(1015,"该登录账号已被禁用"),
    TOKEN_EXCEPTION(1016,"该登录账号仅用于推送数据,无权访问其他服务"),
    GET_TOKEN_ERROR(1017,"获取token失败,请联系管理员"),
    API_ISEMPTY(1018,"获取api对象失败"),

    /**
     * 报表可视化服务，错误码从2000开始
     */
    VISUAL_CONNECTION_ERROR(2001, "数据源连接失败"),
    VISUAL_LOADDRIVER_ERROR(2002, "数据库驱动加载失败"),
    VISUAL_PARAMTER_ERROR(2003, "缺少参数"),
    VISUAL_QUERY_ERROR(2004, "查询失败"),
    VISUAL_IMAGE_ERROR(2005, "图片格式不正确！，请使用.jpg/.png/.bpm/.jpeg后缀的图片"),
    VISUAL_FOLDER_ERROR(2006, "压缩包格式不正确！，请使用.zip后缀的压缩包"),

    /**
     * 授权中心
     */
    AUTH_CLIENTINFO_ERROR(3001, ""),
    AUTH_SECRET_ERROR(3002, "客户端的信息有误,secret错误"),
    AUTH_JWT_ERROR(3003, "JWT无效或已过期"),
    AUTH_USER_NOTLOGIN(3004, "用户未登录"),
    AUTH_TOKEN_PARSER_ERROR(3005, "token解析失败"),
    AUTH_TOKEN_IS_NOTNULL(3006, "token不能为空"),

    /**
     * 用户中心
     */
    USER_ACCOUNTPASSWORD_ERROR(4001, "用户名或密码不正确"),
    ORIGINAL_PASSWORD_ERROR(4002,"用户原密码错误"),

    /**
     * 数据接入模块
     */
    DATAACCESS_GETFIELD_ERROR(5001, "获取表字段失败"),
    DATAACCESS_GETTABLE_ERROR(5002, "获取表名失败"),
    DATAACCESS_SAVEFIELD_ERROR(5003, "添加表字段失败"),
    DATAACCESS_CONNECTDB_ERROR(5004, "连接失败"),
    DATAACCESS_CONNECTDB_WARN(5005, "当前驱动类型尚未开发"),
    DATAACCESS_APPNAME_SUCCESS(5006, "应用名称有效"),
    DATAACCESS_APPNAME_ERROR(5007, "应用名称已存在"),
    DATAACCESS_APPABBREVIATION_SUCCESS(5008, "应用简称有效"),
    DATAACCESS_APPABBREVIATION_ERROR(5009, "应用简称已存在"),
    TABLE_NOT_EXIST(5010, "当前物理表已删除"),
    TABLE_IS_EXIST(5011, "物理表名已存在"),
    TASK_EXEC_FAILURE(5012, "task模块publishBuildAtlasTableTask方法执行失败"),
    SQL_EXCEPT_CLEAR(5013, "当前SQL异常清除"),
    FTP_CONNECTION_ERROR(5014, "FTP服务器连接登录失败，请检查连接参数是否正确，或者网络是否通畅"),
    FTP_CONNECTION_INVALID(5015, "当前ftp数据源信息已无效,请重新配置"),
    LOAD_FTP_FILESYSTEM_ERROR(5016, "加载ftp数据源文件系统失败"),
    LOAD_VIEW_NAME_ERROR(5017, "获取视图名称失败"),
    LOAD_VIEW_STRUCTURE_ERROR(5018, "获取视图结构失败"),
    READ_CSV_CONTENT_ERROR(5019, "读取csv内容失败"),
    READ_EXCEL_CONTENT_ERROR(5020, "读取excel内容失败"),
    PARSE_JSONSCHEMA_ERROR(5021, "解析Json的schema信息失败"),
    RECEIVE_DATA_NULL(5022, "本次并未接收到数据"),
    PUSH_DATA_ERROR(5023, "推送数据失败,请联系管理员"),
    REALTIME_ACCOUNT_OR_PWD_ERROR(5024, "请输入正确的账号或密码"),
    REALTIME_ACCOUNT_ISEXIST(5025, "当前账号已存在,请重新输入"),
    GENERATE_PDF_ERROR(5026, "生成PDF文档失败"),
    PUSH_TABLEID_NULL(5027, "推送的api_id不可为空"),
    APP_NOT_EXIST(5028, "当前API所属应用已删除"),
    CREATE_PG_CONNECTION(5029, "创建pgsql连接驱动失败"),
    API_NOT_EXIST(5030, "创建pgsql连接驱动失败"),


    /**
     * 后台任务模块
     */
    TASK_PUBLISH_ERROR(6001, "任务发布失败"),
    TASK_NIFI_BUILD_COMPONENTS_ERROR(6002, "Nifi组件创建失败"),
    TASK_NIFI_NO_COMPONENTS_FOUND(6003, "未找到组件"),
    TASK_NIFI_DISPATCH_ERROR(6004,"调度失败"),
    TASK_NIFI_EMPTY_ALL_CONNECTIONS_REQUESTS_ERROR(6005,"清空队列失败"),
    TASK_NIFI_CONTROLLER_SERVICES_RUN_STATUS_ERROR(6006,"禁用控制器服务失败"),
    TASK_NIFI_DELETE_FLOW(6007,"nifi删除失败"),
    TASK_TABLE_NOT_EXIST(6008,"表不存在"),
    /**
     * 数据域接入模块
     *
     * @param code
     * @param msg
     */
    API_DELETE_ERROR(7001, "请先删除用户下的Api接口"),
    DELETE_ERROR(7002, "删除失败"),
    API_FIELD_ERROR(7003, "api没有sql执行!"),
    SQL_ERROR(7004, "执行sql语法错误!"),
    SQL_ANALYSIS(7005, "数据解析失败!"),

    /**
     * 数据建模模块
     */
    DARAMODEL_INPUT_REPEAT(8001, "输入数据存在重复值"),
    PUBLISH_FAILURE(8002, "发布失败"),
    FIELDS_ASSOCIATED(8003,"选中字段存在关联"),
    TABLE_ASSOCIATED(8004,"表中字段存在关联"),
    DIMENSION_EXIST(8005,"已存在该维度表"),
    NAME_REPEATED(8006,"名称重复"),
    FACT_EXIST(8007,"已存在该事实表"),
    BUSINESS_AREA_EXIST(8008,"业务域名称已存在"),
    ADD_TABLE_HISTORY(8009,"添加发布历史失败"),
    BUSINESS_AREA_EXISTS_ASSOCIATED(8010,"业务域中维度存在其他关联"),

    /**
     * 数据工厂
     */
    TASK_SCHEDULE_CRONEXPRESSION_ERROR(9001, "添加成功,暂无法解析此表达式"),
    WORKFLOWNAME_EXISTS(9002,"管道名称已存在"),
    DELETE_TASK_GRUOP_ERROR(9003,"删除任务组失败"),
    SCHEDULE_PARAME_NULL(9004,"请设置[开始]组件的调度参数"),
    DATA_FACTORY_FEIGN_EXCEPTION(9005,"数据工厂feign接口异常"),
    CUSTOMWORKFLOW_NOT_EXISTS(9006,"当前管道已删除,请检查参数"),
    CUSTOMWORKFLOWDETAIL_NOT_EXISTS(9007,"当前管道下不存在组件,请检查参数"),
    FLOW_TABLE_NOT_EXISTS(9008,"当前管道下不存在组件,请检查参数"),


    /**
     * 元数据
     */
    BAD_REQUEST(400,"错误请求"),
    NO_CONTENT(204,"没有内容"),
    NOT_SUPPORT(205,"暂不支持该类型数据查询"),

    /**
     * 数据服务
     */
    DS_APP_NAME_EXISTS(10000,"应用名称已存在"),
    DS_APP_ACCOUNT_EXISTS(10001,"账号已存在"),
    DS_APP_API_EXISTS(10002, "请先禁用应用下的API接口"),
    DS_APP_EXISTS(10003, "应用不存在"),
    DS_API_EXISTS(10004, "API不存在"),
    DS_APP_PWD_NOTNULL(10005, "应用密码不能为null"),
    DS_DATASOURCE_CON_WARN(10006, "当前驱动类型尚未开发"),
    DS_DATASOURCE_CON_ERROR(10007, "连接失败"),
    DS_DATASOURCE_EXISTS(10008, "数据源不存在，请刷新页面"),
    DS_API_PV_QUERY_ERROR(10009, "查询失败"),
    DS_APISERVICE_APP_EXISTS(10010, "当前下游系统已失效，请联系相关人员"),
    DS_APISERVICE_API_EXISTS(10011, "API不存在，请检查APICODE"),
    DS_APISERVICE_APP_NOTSUB(10012, "未订阅此API"),
    DS_APISERVICE_APP_NOTENABLE(10013, "未启用此API"),
    DS_APISERVICE_DATASOURCE_EXISTS(10014, "API数据源不存在"),
    DS_APISERVICE_QUERY_ERROR(10015, "查询失败"),
    DS_APPAPIDOC_EXISTS(10016, "请先引用API"),
    DS_APPAPIDOC_DISABLE(10017, "请先启用API"),
    DS_API_FIELD_EXISTS(10018, "字段信息为空，请检查API配置"),
    DS_APPAPIDOC_ERROR(10019, "pdf文档生成失败"),
    DS_APP_SUBAPI_ENABLE(10020, "取消勾选的API含已订阅的API，请先解除订阅"),
    DS_APISERVICE_API_APPINFO_EXISTS(10021, "应用账号/密码错误，请核对");

    ResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private final int code;
    private final String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static ResultEnum getEnum(int code) {
        for (ResultEnum enums : ResultEnum.values()) {
            if (enums.getCode() == code) {
                return enums;
            }
        }
        return ResultEnum.UNKNOWN;
    }
}
