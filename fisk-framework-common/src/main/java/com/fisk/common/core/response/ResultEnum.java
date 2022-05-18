package com.fisk.common.core.response;

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
    REQUEST_SUCCESS(200,"请求成功"),
    UNAUTHENTICATE(401, "认证失败"),
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
    TABLE_NAME_EXISTS(1012, "表名已存在"),
    NOTFOUND_REQUEST_ATTR(1013, "错误的请求头"),
    NIFI_NOT_FIND_DATA(1014, "nifi查不到数据"),
    LOGIN_ACCOUNT_DISABLED(1015, "该登录账号已被禁用"),
    TOKEN_EXCEPTION(1016, "该登录账号仅用于推送数据,无权访问其他服务"),
    GET_TOKEN_ERROR(1017, "获取token失败,请联系管理员"),
    API_ISEMPTY(1018, "获取api对象失败"),
    CLIENT_ISEMPTY(1019, "客户端已删除"),
    FILENAME_EXISTS(1020, "文件名称已存在"),

    /**
     * 报表可视化服务，错误码从2000开始
     */
    VISUAL_CONNECTION_ERROR(2001, "数据源连接失败"),
    VISUAL_LOADDRIVER_ERROR(2002, "数据库驱动加载失败"),
    VISUAL_PARAMTER_ERROR(2003, "缺少参数"),
    VISUAL_QUERY_ERROR(2004, "查询失败"),
    VISUAL_IMAGE_ERROR(2005, "图片格式不正确！，请使用.jpg/.png/.bpm/.jpeg后缀的图片"),
    VISUAL_FOLDER_ERROR(2006, "压缩包格式不正确！，请使用.zip后缀的压缩包"),
    VISUAL_CREATE_ERROR(2007, "创建表失败"),

    /**
     * 授权中心
     */
    AUTH_CLIENTINFO_ERROR(3001, ""),
    AUTH_SECRET_ERROR(3002, "客户端的信息有误，secret错误"),
    AUTH_LOGIN_INFO_INVALID(3003, "登录信息无效或已过期，请重新登录"),
    AUTH_TOKEN_PARSER_ERROR(3005, "token解析失败"),
    AUTH_TOKEN_IS_NOTNULL(3006, "token为空"),

    /**
     * 用户中心
     */
    USER_ACCOUNTPASSWORD_ERROR(4001, "用户名或密码不正确"),
    ORIGINAL_PASSWORD_ERROR(4002, "用户原密码错误"),

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
    STG_TO_ODS_ERROR(5031, "stg同步数据到ods报错"),
    DATASOURCE_INFORMATION_ISNULL(5032, "数据源信息不存在"),
    APICONFIG_ISNULL(5033, "api不存在"),
    DATASOURCE_ISNULL(5034, "当前api的身份验证信息已缺失,请检查当前api所属的应用参数"),
    GET_JWT_TOKEN_ERROR(5035, "当前api,获取token失败,请检查api的配置信息"),
    COPY_API_ISNULL(5036, "当前复制的api不存在,请重新选择"),
    APINAME_ISEXIST(5037, "api名称重复,请重新设置"),


    /**
     * 后台任务模块
     */
    TASK_PUBLISH_ERROR(6001, "任务发布失败"),
    TASK_NIFI_BUILD_COMPONENTS_ERROR(6002, "Nifi组件创建失败"),
    TASK_NIFI_NO_COMPONENTS_FOUND(6003, "未找到组件"),
    TASK_NIFI_DISPATCH_ERROR(6004, "调度失败"),
    TASK_NIFI_EMPTY_ALL_CONNECTIONS_REQUESTS_ERROR(6005, "清空队列失败"),
    TASK_NIFI_CONTROLLER_SERVICES_RUN_STATUS_ERROR(6006,"禁用控制器服务失败"),
    TASK_NIFI_DELETE_FLOW(6007,"nifi删除失败"),
    TASK_TABLE_NOT_EXIST(6008,"表不存在"),
    TASK_TABLE_CREATE_FAIL(6009,"表创建失败"),

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
    PUBLISH_NOTSUCCESS(8011, "请发布成功后再获取分析指标语句"),
    NOT_SUPPORT_FULL_JOIN(8012,"MySql暂不支持全连接"),

    /**
     * 数据工厂
     */
    TASK_SCHEDULE_CRONEXPRESSION_ERROR(9001, "添加成功,暂无法解析此表达式"),
    WORKFLOWNAME_EXISTS(9002, "管道名称已存在"),
    DELETE_TASK_GRUOP_ERROR(9003, "删除任务组失败"),
    SCHEDULE_PARAME_NULL(9004, "请设置[开始]组件的调度参数"),
    DATA_FACTORY_FEIGN_EXCEPTION(9005, "数据工厂feign接口异常"),
    CUSTOMWORKFLOW_NOT_EXISTS(9006, "当前管道已删除,请检查参数"),
    CUSTOMWORKFLOWDETAIL_NOT_EXISTS(9007, "当前管道下不存在组件,请检查参数"),
    FLOW_TABLE_NOT_EXISTS(9008, "当前管道下不存在组件,请检查参数"),
    COMPONENT_NOT_EXISTS(9009, "当前组件不存在请检查参数"),

    /**
     * 数据服务
     */
    DS_APP_NAME_EXISTS(10000, "应用名称已存在"),
    DS_APP_ACCOUNT_EXISTS(10001, "账号已存在"),
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
    DS_APP_SUBAPI_ENABLE(10020, "取消勾选的API含已启用的API，请先禁用"),
    DS_APISERVICE_API_APPINFO_EXISTS(10021, "应用账号/密码错误，请核对"),
    DS_DATASOURCE_READ_ERROR(10022, "数据源信息读取失败"),

    /**
     * 数据质量
     */
    DATA_QUALITY_TEMPLATE_EXISTS(11001, "模板信息不存在"),
    DATA_QUALITY_DATASOURCE_EXISTS(11002, "数据源不存在"),
    DATA_QUALITY_REQUESTSORT_ERROR(11003, "参数异常，执行顺序调整失败"),
    DATA_QUALITY_CREATESTATEMENT_ERROR(11004, "数据库连接建立失败"),
    DATA_QUALITY_CLOSESTATEMENT_ERROR(11005, "数据库连接关闭失败"),
    DATA_QUALITY_SCHEDULE_TASK_PARAMTER_ERROR(11006, "调度任务参数异常，创建失败"),
    DATA_QUALITY_SCHEDULE_TASK_FAIL(11007, "调度任务执行失败"),
    DATA_QUALITY_DATACHECK_RULE_ERROR(11008, "数据校验规则异常"),
    DATA_QUALITY_DATACHECK_RULE_EXEC_ERROR(11009, "数据校验规则执行异常"),

    /**
     * 数据安全
     */
    FIELD_NAME_IS_SELECTED(12001, "当前字段已被设置,请重新选择"),
    ROW_SECURITYNAME_EXISTS(12002, "当前权限名称已存在"),
    USERGROUP_PERMISSION_ONLY(12003, "用户(组)已设置过权限,不允许设置两种,请检查参数"),
    CAN_NOT_DELETE_NAME_OR_CODE(12003,"无法删除“name”或“code”"),

    /**
     * 元数据
     */
    BAD_REQUEST(13001, "请求错误"),
    NO_CONTENT(13002, "没有内容"),
    NOT_SUPPORT(13003, "暂不支持该类型数据查询"),
    DATA_SOURCE_CONFIG(13004, "获取元数据配置文件失败"),

    /**
     * 数据运维
     */
    DATA_OPS_CONFIG_EXISTS(14001, "数据源配置不存在"),
    PG_CONNECT_ERROR(14002, "Postgres数据库连接异常"),
    PG_READ_TABLE_ERROR(14003, "Postgres读取表信息异常"),
    PG_READ_FIELD_ERROR(14004, "Postgres读取字段信息异常"),
    DATA_OPS_SQL_EXECUTE_ERROR(14005, "SQL执行异常"),
    DATA_OPS_CLOSESTATEMENT_ERROR(14006, "数据库连接关闭失败"),
    DATA_OPS_CREATELOG_ERROR(14007, "执行日志保存失败"),
    NO_DATA_TO_SUBMIT(14008,"暂无可提交数据"),
    DATA_SUBMIT_ERROR(14009,"数据提交失败"),
    PG_METADATA_READREDIS_EXISTS(14010,"redis中未找到pg元数据对应的key"),
    PG_METADATA_SETREDIS_ERROR(14011,"redis写入pg元数据信息异常"),
    PG_METADATA_GETREDIS_ERROR(14012,"redis读取pg元数据信息异常"),

    /**
     * mdm
     */
    CREATE_STG_TABLE(15001, "Stg表创建失败"),
    CREATE_MDM_TABLE(15002, "Mdm表创建失败"),
    CREATE_VIW_TABLE(15003, "Viw视图创建失败"),
    UPDATE_MDM_TABLE(15004, "Mdm表修改失败"),
    DROP_STG_TABLE(15005, "Stg表删除失败"),
    DROP_VIW_TABLE(15006, "Viw视图删除失败"),
    CREATE_ATTRIBUTE_LOG_TABLE_ERROR(15007, "创建属性日志表失败"),
    CREATE_TABLE_ERROR(15008, "后台任务创建表失败"),
    EXIST_INVALID_COLUMN(15009, "存在无效列"),
    REQUIRED_FIELDS(15010, "必填项"),
    DATA_TYPE_DISAGREE(15011, "与数据类型不一致"),
    EXIST_ERROR_DATA(15012, "存在错误数据"),
    KEY_DATA_NOT_FOUND(151013, "数据不存在或已过期"),
    FILE_NAME_ERROR(151014, "文件格式不正确,只支持xlsx文件"),
    DATA_SYNCHRONIZATION_FAILED(151015, "stg表数据同步失败!"),
    DATA_SYNCHRONIZATION_SUCCESS(151016, "stg表数据同步成功!"),
    ATTRIBUTE_NOT_EXIST(151017, "该实体不存在属性"),
    EXISTS_INCORRECT_DATA(151018, "数据格式有问题，请修改数据后重新提交"),
    SUBMIT_FAILURE(151019, "提交失败"),
    CHECK_TEMPLATE_IMPORT_FAILURE(151020, "导入失败，请检查模板是否正确");

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
