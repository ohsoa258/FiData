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
    REQUEST_SUCCESS(200, "请求成功"),
    ACCEPTED(202, "接收成功"),
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
    NAME_EXISTS(1006, "用户账号不能重复"),
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
    SYSTEM_PARAMS_ISEMPTY(1021, "系统参数为空"),
    DATASOURCE_CONNECTERROR(1022, "数据源连接建立异常"),
    DATASOURCE_CONNECTCLOSEERROR(1022, "数据源连接关闭异常"),
    NO_MATCHING_DATA_TYPE(1023, "没有匹配的数据类型"),
    CRON_ERROR(1024, "cron格式错误"),
    DRUID_SQL_ERROR(1025, "druid解析sql失败"),
    DRUID_ERROR(1026, "druid异常"),
    UPLOAD_ERROR(1027, "文件上传失败"),
    SQL_PARSING(1028, "sql解析失败"),
    COPY_ERROR(1029, "拷贝失败"),
    USERNAME_EXISTS(1030, "用户名不能重复"),
    SYSTEM_TITLE_NULL(1031, "系统名称不能为空"),
    SYSTEM_LOGO_NULL(1032, "系统logo文件不能为空"),
    SYSTEM_LOGO_ERROR(1033, "系统logo获取出错"),
    DATA_SOURCE_NAME_ALREADY_EXISTS(1034, "数据源名称已存在"),
    SQL_PARAMETER_NOTNULL(1035, "sql参数不能为空"),
    SQL_Not_SUPPORTED_YET_DBTYPE(1036, "暂不支持该数据库类型"),
    TABLE_NOT_PUBLISHED(1037, "该表未发布"),
    LOG_NOT_EXISTS(1038, "当前选择的服务类型在当前日期没有日志"),
    SERVICE_NOT_EXISTS(1039, "当前所选的服务不存在"),
    SET_KEEP_NUMBER_ERROR(1040, "设置stg保存时间失败"),
    SAVE_ACCESS_DATA_SOURCE_ERROR(1041, "数据接入服务修改数据源失败"),
    GET_ACCESS_DATA_SOURCE_ERROR(1042, "根据SystemDataSourceId获取数据接入引用的数据源信息失败"),
    DATA_SOURCE_IS_USING(1043, "平台数据源删除失败！当前要删除的数据源仍有数据接入的app应用在引用，请移除引用后在删除！"),
    APP_IS_NOT_EXISTS(1044, "数据接入-app应用不存在"),
    DATA_SOURCE_ACCOUNT_ALREADY_EXISTS(1045, "RESTFULAPI的账号不允许重复!请您修改账号或使用已配置的数据源。"),
    DATA_SOURCE_ALREADY_EXISTS(1046, "相同类型,ip,库名的数据源已存在!请您使用已配置的数据源。"),
    DISPATCH_REMOTE_ERROR(1047, "删除表时-远程调用管道模块失败"),
    ACCESS_PHYTABLE_EXISTS_IN_DISPATCH(1048, "禁止删除该表-该表存在于以下管道:"),
    RUN_ONCE_ERROR(1049, "RUN_ONCE执行失败请检查组件是否正常"),
    RUN_ONCE_LOCK(1050, "RUN_ONCE执行中，请勿重复执行"),
    GET_SYSTEM_EMAIL_SERVER_ERROR(1051, "获取平台配置邮件服务器信息失败"),
    HUDI_CONNECT_ERROR(1052, "平台配置测试HUDI连接异常"),

    EMAIL_EXISTS(1053, "邮箱不能重复"),


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

    VISUAL_QUERY_ERROR_INVALID(2008, "数据无效"),


    /**
     * 授权中心
     */
    AUTH_CLIENTINFO_ERROR(3001, ""),
    AUTH_SECRET_ERROR(3002, "客户端的信息有误，secret错误"),
    AUTH_LOGIN_INFO_INVALID(3003, "登录信息无效或已过期，请重新登录"),
    AUTH_TOKEN_PARSER_ERROR(3005, "token解析失败"),
    AUTH_TOKEN_IS_NOTNULL(3006, "token为空"),
    SSO_AUTH_FAILURE(3007, "SSO单点登录获取票据失败！未获取到有效用户信息..."),
    SSO_REGISTER_FAILURE(3008, "SSO单点登录注册临时用户失败..."),
    SSO_ASSIGNMENT_FAILURE(3009, "SSO单点登录为临时用户分配角色失败..."),
    SSO_GET_TEMPORARY_USER_FAILURE(3010, "SSO单点登录获取刚插入的临时用户失败..."),
    SSO_GET_ROLE_ID_FAILURE(3010, "SSO单点登录获取角色id失败..."),
    SSO_LOGIN_FAILURE(3011, "单点登录失败"),


    /**
     * 用户中心
     */
    USER_ACCOUNTPASSWORD_ERROR(4001, "用户名或密码不正确"),
    ORIGINAL_PASSWORD_ERROR(4002, "用户原密码错误"),
    DATA_SOURCE_ERROR(4003, "获取数据源配置失败"),
    SYSTEM_DATA_SOURCE_NOT_OPERATION(4004, "系统数据源不允许此操作"),
    USER_NON_EXISTENT(4005, "用户信息获取失败"),

    /**
     * 数据接入模块
     */
    DATAACCESS_GETFIELD_ERROR(5001, "获取表字段失败"),
    DATAACCESS_GETTABLE_ERROR(5002, "获取表名失败"),
    DATAACCESS_SAVEFIELD_ERROR(5003, "添加表字段失败"),
    DATAACCESS_CONNECTDB_ERROR(5004, "连接失败,请检查参数"),
    DATAACCESS_CONNECTDB_WARN(5005, "当前驱动类型尚未开发"),
    DATAACCESS_APPNAME_SUCCESS(5006, "应用名称有效"),
    DATAACCESS_APPNAME_ERROR(5007, "应用名称已存在"),
    DATAACCESS_APPABBREVIATION_SUCCESS(5008, "应用简称有效"),
    DATAACCESS_APPABBREVIATION_ERROR(5009, "应用简称已存在"),
    TABLE_NOT_EXIST(5010, "当前物理表不存在or已删除"),
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
    API_NOT_EXIST(5030, "当前推送的api不存在"),
    STG_TO_ODS_ERROR(5031, "stg同步数据到ods报错"),
    DATASOURCE_INFORMATION_ISNULL(5032, "数据源信息不存在"),
    APICONFIG_ISNULL(5033, "api不存在"),
    DATASOURCE_ISNULL(5034, "当前api的身份验证信息已缺失,请检查当前api所属的应用参数"),
    GET_JWT_TOKEN_ERROR(5035, "当前api获取token失败,请检查api的配置信息"),
    COPY_API_ISNULL(5036, "当前复制的api不存在,请重新选择"),
    APINAME_ISEXIST(5037, "api名称重复,请重新设置"),
    EXECUTE_HTTP_REQUEST_ERROR(5038, "调用第三方接口报错,请重新检查api的配置信息"),
    SEND_POST_REQUEST_ERROR(5039, "发送post请求失败,请检查参数"),
    SEND_GET_REQUEST_ERROR(5040, "发送get请求失败,请检查参数"),
    ACCOUNT_CANNOT_OPERATION_API(5041, "该账号无权操作当前api,请检查账号"),
    API_APP_ISNULL(5042, "api的应用信息丢失,请重新检查api的应用信息"),
    FIELD_CKECK_NOPASS(5043, "本次同步的数据中,有字段校验不通过,详情请看data"),
    PUSH_DATA_SQL_ERROR(5044, "执行推送数据的sql异常,请联系管理员"),
    COPY_API_TABLE_ERROR(5045, "复制api下的表失败,表名已存在"),
    COPY_APINAME_ISEXIST(5046, "目标应用下已存在相同名称的api,请重新选择,或者给api重命名"),
    DATA_QUALITY_FEIGN_ERROR(5047, "数据质量的数据校验feign接口异常"),
    LOAD_DATASOURCE_META(5048, "重新加载所有数据源以及数据库、表数据报错"),
    LOAD_FIDATA_METADATA_ERROR(5049, "加载FaidataMetaData报错"),
    JSON_ROOTNODE_HANDLER_ERROR(5050, "pushData中json格式可能有误,解析失败,请自行检查"),
    FILE_NOT_SELECTED(5051, "请选择需要读取的文件"),
    DRIVETYPE_IS_NULL(5052, "请选择驱动类型"),
    GET_DATABASE_ERROR(5053, "获取数据库失败,请重新检查参数"),
    RETURN_RESULT_DEFINITION(5054, "返回结果定义必须选中获取键值"),
    API_EXPRESSION_ERROR(5055, "API表达式格式错误"),
    SCHEMA_ERROR(5056, "创建或删除schema失败"),
    UPLOAD_FLINK_ERROR(5057, "脚本上传flink失败"),
    PIPELINENAME_EXISTING(5058, "Job名称已存在"),
    UPLOADFILE_REMOTE_ERROR(5059, "文件上传远程失败"),
    SAVE_POINTS_UPDATE_ERROR(5060, "检查点保存失败"),
    CREATE_JOB_ERROR(5061, "创建Flink任务失败"),
    SCHEMA_TABLE_REPEAT(5062, "其他应用下已存在该表名"),
    SYSTEM_VARIABLES_ERROR(5063, "系统变量异常"),
    VERSION_PARAMS_SPLIT_ERROR(5064, "参数分割异常"),
    VERSION_TABLE_TYPE_ERROR(5065, "暂不支持的表类型"),
    VERSION_TABLE_NOT_EXISTS(5066, "表配置不存在"),
    VERSION_APP_NOT_EXISTS(5067, "应用配置不存在"),
    VERSION_TABLE_SYNC_NOT_EXISTS(5068, "表同步方式配置不存在"),
    VERSION_TABLE_SYNC_NOT_ALL(5069, "仅支持全量模式下设置版本"),
    VERSION_SAVE_DAY_ERROR(5070, "版本保留天数为0"),
    VERSION_CUSTOM_SQL_IS_NULL(5071, "版本自定义语句为空"),
    VERSION_CUSTOM_SQL_RESULT_IS_NULL(5072, "版本自定义语句查询结果为空"),
    VERSION_NOT_OPEN_SAVE_RULE(5073, "未启用版本保留规则"),
    DATAACCESS_GETSCHEMA_ERROR(5074, "获取schema失败"),
    DATAACCESS_GETTABLEANDFIELD_ERROR(5075, "获取表和字段信息失败"),
    SFTP_CONNECTION_ERROR(5075, "SFTP服务器连接登录失败，请检查连接参数是否正确，或者网络是否通畅"),
    SFTP_PREVIEW_ERROR(5076, "SFTP预览文件失败"),
    SFTP_FILE_INDEX_ERROR(5077, "SFTP文件索引错误"),
    SFTP_FILE_IS_NULL(5078, "SFTP文件不存在"),
    SFTP_FILE_COPY_FAIL(5079, "SFTP文件复制失败"),
    SFTP_RSA_IS_NULL(5080, "SFTP密钥文件路径不能为空"),
    SFTP_ACCOUNT_IS_NULL(5081, "SFTP账号密码不能为空"),
    SFTP_DIR_PATH_ERROR(5082, "SFTP目录格式错误"),
    DATASOURCE_TYPE_ERROR(5083, "已选择的数据源类型冲突"),
    DATAACCESS_GET_TABLE_ERROR(5084, "根据应用id获取物理表失败"),
    DATAACCESS_APP_EDIT_FAILURE(5085, "当前要删除的数据源正在使用，此次修改失败...事务已回滚...正在使用的表详情请查看报错日志..."),
    DATAACCESS_DATASOURCE_ERROR(5086, "当前发布表的数据源不存在或已删除，请联系系统管理员"),
    API_STATE_NOT_ALLOW_ERROR(5087, "当前调用的接口已被禁用，请联系系统管理员..."),
    STG_TO_ODS_ERROR_DETAIL(5088, "失败，stg同步数据到ods-执行sql报错，请排查：" +
            "1、字段类型、长度 " +
            "2、页面配置的主键个数是否与底表主键个数匹配 " +
            "3、若是业务主键覆盖方式，请检查on conflict - do update语句是否会二次更新列 " +
            "4、若涉及到时间类型，请联系系统管理员..."),
    ACCESS_PUBLISH_FAILURE(5089, "发布失败，请联系系统管理员"),
    ACCESS_MAINPAGE_SELECT_FAILURE(5090, "数据接入-首页展示查询失败"),
    SAPBW_EXECUATE_MDX_ERROR(5091, "SAPBW执行mdx语句报错"),
    SAPBW_GETCUBES_ERROR(5092, "SAPBW获取cubes报错"),
    SAPBW_GETVARS_ERROR(5093, "SAPBW获取cube参数报错"),
    SAPBW_GETDIMS_MES_ERROR(5094, "SAPBW获取cube维度和指标报错"),
    SAPBW_CONNECT_ERROR(5095, "创建SAPBW连接失败"),
    SAPBW_NIFI_SYNC_ERROR(5096, "SAPBW-NIFI同步报错"),
    WEBSERVICE_CODE_NULL_ERROR(5097, "推送的webServiceCode不可为空"),
    WEBSERVICE_NOT_EXIST(5098, "当前推送的webService不存在"),
    WEBSERVICE_TOKEN_NOT_EXIST(5099, "webService携带的token为空"),
    WEBSERVICE_GET_TOKEN_ERROR(5100, "webService获取token报错"),
    WEBSERVICE_PUSH_DATA_ERROR(5101, "webService推送数据报错"),
    DORIS_GET_CATALOG_ERROR(5102, "获取doris外部类目录失败"),
    REFRESH_REDIS_DORIS_CATALOG_ERROR(5103, "刷新dorsi外部目录的redis失败"),
    DATAACCESS_GETCATALOG_ERROR(5103, "获取数据库catalog名称失败"),
    API_NIFI_SYNC_ERROR(5104, "API-NIFI同步报错"),
    GET_ACCESS_TREE_FAILURE(5105, "获取数据接入应用-表树结构失败"),
    ACCESS_HUDI_SYNC_ERROR(5106, "获取数据-入仓配置同步表失败"),
    ACCESS_HUDI_RESYNC_ERROR(5107, "hudi-入仓配置重新同步单张表--异常"),
    RESYNC_NO_FIELD_WARNING(5108, "没有字段的表不允许重新同步"),
    APICONFIG_ENCRYPT_ISNULL(5109, "api密钥不存在"),
    GENERATE_AES_ERROR(5110, "生成AES密钥失败"),
    DATA_DECRYPTION_ERROR(5111,"数据解密失败，api已开启AES加密，请查看数据是否正确加密。"),
    EXCEL_EXPORT_ERROR(5112,"数据湖管理-导出excel失败"),
    FLINK_PUBLISH_ERROR(5113,"数据湖管理-Flink CDC表发布失败"),
    GET_FLINK_FIELD_ERROR(5114,"当前选择的表字段为空，请先同步字段!"),
    GET_FLINK_SCHEMA_ERROR(5115,"获取schema失败!"),
    FLINK_BUILD_JOB_ERROR(5116,"建立Flink任务失败!"),
    DELETE_FLINK_ERROR(5117,"删除Flink CDC表失败!"),
    TABLE_NOT_OPEN_CDC(5118,"表在中台内未开启CDC!请开启CDC后再发布!"),
    FLINK_STOP_JOB_ERROR(5119,"flink终止指定job失败"),
    FLINK_DUPLICATE_RELEASE_ERROR(5120,"flink job请勿重复发布!"),
    PBI_GET_GROUP_ERROR(5121,"PowerBI获取用户工作区失败！"),
    PBI_GET_DATASETS_ERROR(5122,"PowerBI获取工作区下的数据集失败！"),
    PBI_GET_ACCESSTOKEN_ERROR(5123,"获取powerbi accessToken失败！"),
    POWERBI_NIFI_SYNC_ERROR(5124, "PowerBI-NIFI同步报错"),
    ACCESS_TREE_GET_MONGO_DOCUMENT_ERROR(5125, "获取mongodb指定集合下的文档失败！"),

    /**
     * 后台任务模块
     */
    TASK_PUBLISH_ERROR(6001, "任务发布失败"),
    TASK_NIFI_BUILD_COMPONENTS_ERROR(6002, "Nifi组件创建失败"),
    TASK_NIFI_NO_COMPONENTS_FOUND(6003, "未找到组件"),
    TASK_NIFI_DISPATCH_ERROR(6004, "调度失败"),
    TASK_NIFI_EMPTY_ALL_CONNECTIONS_REQUESTS_ERROR(6005, "清空队列失败"),
    TASK_NIFI_CONTROLLER_SERVICES_RUN_STATUS_ERROR(6006, "禁用控制器服务失败"),
    TASK_NIFI_DELETE_FLOW(6007, "nifi删除失败"),
    TASK_TABLE_NOT_EXIST(6008, "表不存在"),
    TASK_TABLE_CREATE_FAIL(6009, "表创建失败"),

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
    FIELDS_ASSOCIATED(8003, "选中字段存在关联"),
    TABLE_ASSOCIATED(8004, "表中字段存在关联"),
    DIMENSION_EXIST(8005, "已存在该维度表"),
    NAME_REPEATED(8006, "名称重复"),
    FACT_EXIST(8007, "已存在该事实表"),
    BUSINESS_AREA_EXIST(8008, "业务域名称已存在"),
    ADD_TABLE_HISTORY(8009, "添加发布历史失败"),
    BUSINESS_AREA_EXISTS_ASSOCIATED(8010, "业务域中维度存在其他关联"),
    PUBLISH_NOTSUCCESS(8011, "请发布成功后再获取分析指标语句"),
    NOT_SUPPORT_FULL_JOIN(8012, "MySql暂不支持全连接"),
    FACT_FIELD_EXIST(8013, "当前设置的目标英文名称已存在,请重新设置"),
    QUERY_CONDITION_NOTNULL(8014, "表关联关系不能为空"),
    FACT_NAME_NOTNULL(8015, "操作的事实表名不允许为空"),
    DATA_MODEL_FIELD_NOT_EXISTS(8016, "数仓建模-发布表修改表结构失败，tb_task_pg_table_structure表无字段！"),
    DATA_MODEL_PUBLISH_ERROR(8017, "数仓建模-发布表失败，请检查：若修改了字段，请检查字段在目标库中字段是否可以修改！"),
    DATA_MODEL_GET_NIFI_LOG_ERROR(8018, "数仓建模获取nifi同步日志失败"),
    GET_MODEL_TREE_FAILURE(8019, "获取数仓建模业务域-表树结构失败"),
    GET_NIFI_LOG_ERROR(8020, "获取nifi同步日志失败"),
    GET_ACCESS_APP_ERROR(8021, "获取nifi同步日志-获取数据接入应用失败"),
    PUBLIC_DIM_PUBLISH_ERROR(8022, "公共域维度表发布失败，请回该表的初始创建业务域发布"),
    MODEL_MAIN_PAGE_COUNT_ERROR(8023, "获取dw主页数据量失败"),
    FACT_EXISTS_IN_DISPATCH(8024, "禁止移动该表-该表存在于以下管道:"),
    WRONG_TABLE_TYPE_ERROR(8025, "表类型只能为 0：维度表 1事实表"),

    MDM_GET_NIFI_LOG_ERROR(8026, "主数据获取nifi同步日志失败"),
    GET_CUSTOM_SQL_ERROR(8027, "获取数仓建模指定表的加载后语句失败！"),
    NIFI_EXCUTE_MODEL_CUSTOM_SQL_ERROR(8027, "获取数仓建模指定表的加载后语句失败！"),

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
    DISPATCHEMAIL_NOT_EXISTS(9010, "当前管道邮件参数为空"),
    UPDATE_WORK_STATUS_ERROR(9011, "管道工作状态更新失败"),

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
    DS_DATASOURCE_NOTEXISTS(10008, "数据源不存在，请刷新页面"),
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
    DS_DATASOURCE_EXISTS(10023, "数据源已存在"),
    DS_VIEW_THEME_EXISTS(10024, "视图主题已经存在"),
    DS_VIEW_THEME_ABBR_VALID(10025, "视图主题简称无效"),
    DS_VIEW_THEME_ACCOUNT_SAVE(10026, "视图主题关联账号保存失败"),
    DS_VIEW_THEME_ACCOUNT_ERROR(10027, "视图主题关联账号名称和密码不能为空"),
    DS_VIEW_THEME_NAME_EXIST(10028, "视图主题名称已存在"),
    DS_VIEW_THEME_ACCOUNT_EXIST(10029, "当前视图主题下存在重复的关联账号名称"),
    DA_VIEWTHEMEID_TABLENAME_ERROR(10030, "视图主题id或表名不能为空"),
    DA_VIEWTHEME_UPDATE_ACCOUNT_ERROR(10031, "更新视图主题关联账号时添加账号失败"),
    DS_DATA_VIEW_EXIST(10032, "当前视图主题下已存在该视图名称"),
    DS_DATA_STATUS_HAS_CHANGED(10033, "数据状态已变更，请刷新页面后再试"),
    DS_DATA_SOURCE_REFRESH_FAILED(10034, "数据源刷新失败"),
    DS_DATA_SOURCE_APPLIED(10035, "数据源已应用，请先删除该数据源下的表服务"),
    DS_DATA_PAGING_PARAMETERS_NOT_SET(10036, "未设置分页参数"),
    API_NOT_CONFIGURED_FOR_OUTPUT_CONFIGURATION(10037, "API未配置输出配置，请检查输出配置（SQL）"),
    DS_DATA_PAGE_SHOULD_BE_GREATER_THAN_0(10038, "页码（current）应为大于0的正整数"),
    DS_DATA_SIZE_SHOULD_BE_GREATER_THAN_0(10039, "页数（size）应为大于0的正整数"),
    DS_DATA_SIZE_CANNOT_BE_GREATER_THAN_100(10040, "最大只能返回100行数据"),
    DS_MISSING_APICODE_IN_URL(10041, "url中缺少apiCode"),
    DS_ILLEGAL_REQUEST(10042, "非法的请求"),
    DS_PROXY_FORWARDING_ERROR(10043, "代理转发错误"),
    DS_THE_MESSAGE_NOTIFICATION_METHOD_DOES_NOT_EXIST(10044, "消息通知方式配置不存在"),
    LOAD_FIDATA_DATA_SERVICE_DATA_ERROR(10045, "加载DataServiceData报错"),
    DS_APISERVICE__EXPIRATION(10046, "api已过期"),

    DS_APISERVICE_MENUID_NOT_EXIST(10047, "数据menuId未填写"),

    SERVICE_ENCRYPT_NOT_EXIST(10048, "未设置密钥"),
    SERVICE_ENCRYPT_ONLY_ONE(10049, "密钥只能有一个"),
    DS_APISERVICE_PARAMLIST_IS_NULL(10050, "请求参数中缺少必填参数"),

    /**
     * 数据质量
     */
    DATA_QUALITY_TEMPLATE_EXISTS(11001, "模板信息不存在"),
    DATA_QUALITY_DATASOURCE_NOT_EXISTS(11002, "数据源不存在，操作终止"),
    DATA_QUALITY_REQUESTSORT_ERROR(11003, "参数异常，执行顺序调整失败"),
    DATA_QUALITY_CREATESTATEMENT_ERROR(11004, "数据库连接建立失败"),
    DATA_QUALITY_CLOSESTATEMENT_ERROR(11005, "数据库连接关闭失败"),
    DATA_QUALITY_SCHEDULE_TASK_PARAMTER_ERROR(11006, "调度任务参数异常，创建失败"),
    DATA_QUALITY_SCHEDULE_TASK_FAIL(11007, "调度任务执行失败"),
    DATA_QUALITY_DATACHECK_RULE_ERROR(11008, "数据校验规则异常"),
    DATA_QUALITY_DATACHECK_RULE_EXEC_ERROR(11009, "数据校验规则执行异常"),
    DATA_QUALITY_DATACHECK_CHECK_NOPASS(11010, "校验不通过"),
    DATA_QUALITY_DATACHECK_CHECKRESULT_EXISTS(11011, "校验结果为空"),
    DATA_QUALITY_DATACHECK_REQUESTJSON_ERROR(11012, "待校验的JSON数据格式异常，未包含指定字段key"),
    DATA_QUALITY_SYNCCHECK_NOOPERATION(11013, "请求参数中操作型参数均为空，无需校验"),
    DATA_QUALITY_UPDATEDATA_ERROR(11014, "数据校验完成，修改表数据触发异常"),
    DATA_QUALITY_TABLECONFIGURATION_SENT_CHANGES(11015, "表配置信息在源已发生变更"),
    DATA_QUALITY_DATASOURCE_EXISTS(11016, "数据源已存在"),
    DATA_QUALITY_NOTICE_NOTEXISTS(11017, "告警通知信息不存在"),
    DATA_QUALITY_RULE_NOTEXISTS(11018, "规则信息不存在"),
    DATA_QUALITY_REDIS_NOTEXISTSTABLEFIELD(11019, "redis中未找到对应的表字段元数据"),
    DATA_QUALITY_UPDATE_PRIMARY_KEY_ISNOTSET(11020, "未设置更新主键"),
    DATA_QUALITY_BUSINESS_API_AUTH_FILTER_EXEC_ERROR(11021, "业务API清洗授权方法执行异常"),
    DATA_QUALITY_BUSINESS_API_FILTER_EXEC_ERROR(11022, "业务API清洗方法执行异常"),
    DATA_QUALITY_NOTICE_CONFIG_ISNULL(11023, "质量报告通知配置为空"),
    DATA_QUALITY_NOTICE_RECIPIENT_ISNULL(11024, "质量报告接收人为空"),
    DATA_QUALITY_THE_CLEANING_RULE_DOES_NOT_EXIST(11025, "业务清洗规则不存在"),
    DATA_QUALITY_NO_CONFIGURE_TRIGGER(11026, "未配置触发器"),
    DATA_QUALITY_NO_WORKSPACE_CONFIGURED(11027, "未配置工作区"),
    DATA_QUALITY_TO_OBTAIN_TABLE_INFORMATION(11028, "未获取到表信息"),
    DATA_QUALITY_DATASOURCE_NOT_EXISTS_STOP(11029, "数据源不存在，表状态字段变更操作终止"),
    DATA_QUALITY_CURRENTLY_ONLY_NON_INSPECTION_QUALITY_REPORTS_ARE_SUPPORTED(11029, "暂只支持非检查质量报告"),
    DATA_QUALITY_CHECK_CODE_ALREADY_EXISTS(11030, "规则名称已存在"),
    DATA_QUALITY_BUSINESS_RULE_ALREADY_EXISTS(11031, "清洗规则已存在"),
    DATA_SERVER_CACHE_TIME_ERROR(11032, "缓存时间只能是5~300内数字"),
    DATA_QUALITY_REPORT_RULE_COUNT_NOT_EQUAL_TO_LOG_COUNT(11033, "质量报告下规则数量与产生的规则检查日志数量不一致"),
    DATA_QUALITY_AFTER_SYNCHRONIZATION_PRE_VERIFICATION_TEMPLATE_DOES_NOT_EXIST(11034, "同步后预校验不通过，模板不存在"),
    DATA_QUALITY_AFTER_SYNCHRONIZATION_PRE_VERIFICATION_DATA_SOURCE_DOES_NOT_EXIST(11035, "同步后预校验不通过，数据源不存在"),
    DATA_QUALITY_AFTER_SYNCHRONIZATION_PRE_VERIFICATION_NOT_APPROVED(11036, "同步后预校验不通过，请检查规则配置"),
    DATA_QUALITY_AFTER_SYNCHRONIZATION_PRE_VERIFICATION_RULE_CONFIG_ISNULL(11037, "同步后预校验不通过，规则配置为空"),
    DATA_QUALITY_AFTER_REPORT_TO_WHICH_VERIFICATION_RULE_LOG_BELONGS_NOT_FOUND(11038, "未找到校验规则日志所属报告"),
    DATA_QUALITY_SERVER_TYPE_ADD_IS_EXIST(11039,"添加失败，当前类型已存在"),
    DATA_QUALITY_SERVER_TYPE_DELETE_IS_EXIST(11040,"删除失败，当前类型已有绑定服务"),

    DATASET_QUERY_PREVIEW(11041, "值对比执行sql应返回单个值请检查sql是否正确"),

    API_SUBSCRIBE_EXISTS(11042, "订阅已存在"),

    DS_APISERVICE_API_FIELD_EMPTY(11043, "API未配置字段信息，请检查字段配置"),

    DS_APISERVICE_FIELD_IS_NULL(11044,"配置参数为空请先配置参数"),

    DS_APISERVICE_FIELD_IS_NOT_EXISTS(11045,"传入参数与配置参数不一致请检查字段是否正确"),
    /**
     * 数据安全
     */
    FIELD_NAME_IS_SELECTED(12001, "当前字段已被设置,请重新选择"),
    ROW_SECURITYNAME_EXISTS(12002, "当前权限名称已存在"),
    USERGROUP_PERMISSION_ONLY(12003, "用户(组)已设置过权限,不允许设置两种,请检查参数"),
    CAN_NOT_DELETE_NAME_OR_CODE(12004, "无法删除“name”或“code”  "),
    INTELLIGENT_DISCOVERY_RULE_NAME_ALREADY_EXISTS(12005, "智能发现规则名称已存在"),
    INTELLIGENT_DISCOVERY_CONFIGURATION_DOES_NOT_EXIST(12006, "智能发现配置不存在"),
    INTELLIGENT_DISCOVERY_SCAN_CONFIGURATION_DOES_NOT_EXIST(12007, "智能发现扫描配置不存在"),
    INTELLIGENT_DISCOVERY_NO_RISK_FIELDS_FOUND(12008, "智能发现未发现风险信息"),
    THE_MAIL_SERVER_DOES_NOT_EXIST(12009, "邮件服务器不存在"),
    SMART_DISCOVERY_REPORT_FAILED_TO_GENERATE_ATTACHMENT(12010, "智能发现报告生成失败"),
    FILE_DOES_NOT_EXIST(12011, "文件不存在"),
    SMART_DISCOVERY_IS_DISABLED(12012, "智能发现已禁用"),

    /**
     * 元数据
     */
    BAD_REQUEST(13001, "请求错误"),
    NO_CONTENT(13002, "没有内容"),
    NOT_SUPPORT(13003, "暂不支持该类型数据查询"),
    DATA_SOURCE_CONFIG(13004, "获取元数据配置文件失败"),
    DETAULT_EDIT_ERROR(13005, "默认模板无法修改"),


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
    NO_DATA_TO_SUBMIT(14008, "暂无可提交数据"),
    DATA_SUBMIT_ERROR(14009, "数据提交失败"),
    PG_METADATA_READREDIS_EXISTS(14010, "redis中未找到pg元数据对应的key"),
    PG_METADATA_SETREDIS_ERROR(14011, "redis写入pg元数据信息异常"),
    PG_METADATA_GETREDIS_ERROR(14012, "redis读取pg元数据信息异常"),
    TABLE_DATA_SYNC_FAIL(14013, "数据同步失败"),
    SMART_PUBLISH_FAIL(14014, "智能发布失败"),
    SMART_PUBLISH_REMOTE_ACCESS_FAIL(14015, "数据接入-智能发布失败-调用数据接入模块接口报错"),
    SMART_PUBLISH_VARIABLES_MISSING_ERROR(14016, "数据接入-智能发布失败-调用数据接入模块接口报错"),
    SMART_PUBLISH_GET_APPINFO_FALIURE(14017, "智能发布-获取应用信息失败"),
    DATA_OPS_GET_TABLE_SCHEMA_ERROR(14018, "数据库运维获取数据库表列信息失败"),

    CHECK_STANDARDS_GROUP_ERROR(14019, "添加失败，该校验组名称已存在"),

    CHECK_STANDARDS_UPDATE_FAIL(14020,"更新数据元成功，数据校验规则同步失败，请手动同步校验规则"),

    CHECK_STANDARDS_DELETE_FAIL(14021,"删除数据元成功，数据校验规则同步删除失败，请手动删除校验规则"),

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
    CHECK_TEMPLATE_IMPORT_FAILURE(151020, "导入失败，请检查模板是否正确"),
    EMPTY_FORM(151021, "空白xlsx文件"),
    CODE_NOT_EXIST(151022, "编码列不存在"),
    ATTRIBUTE_GROUP_NOT_EXIST(151023, "属性组不存在"),
    DATA_REPLICATION_FAILED(151024, "数据复制失败"),
    CODE_EXIST(151025, "编码已存在"),
    MANDATE_TIMESTAMP_START(151026, "主数据定时任务开始执行"),
    MANDATE_TIMESTAMP_SUCCESS(151027, "主数据定时任务执行成功!"),
    UNCOMMITTED_CANNOT_COPIED(151028, "未提交不能复制!"),
    POSTULATES_NOT_ROLLBACK(151029, "发布状态不能回滚!"),
    AVERSION_NOT_DELETE(151030, "最后一个版本了,不能删除!"),
    VIEW_NO_EXIST_ATTRIBUTE(151031, "该视图下暂无属性"),
    FILE_NOT_FOUND_EXCEPTION(151032, "file流路径找不到"),
    FILE_IO_EXCEPTION(151033, "IO流异常"),
    FACT_ATTRIBUTE_FAILD(151034, "事实属性表更新失败!"),
    FORM_NO_VALID_DATA(151035, "表格暂无有效数据"),
    SAVE_PROCESS_NODE_ERROR(151036, "流程节点小于2个"),
    PROCESS_PERSON_NOT_NULL(151037, "节点人员不能为空"),
    VERIFY_PROCESS_APPLY_ERROR(151038, "流程校验异常"),

    SAVE_PROCESS_APPLY_ERROR(151039, "流程工单添加失败"),
    VERIFY_APPROVAL(151040, "应走流程"),
    VERIFY_NOT_APPROVAL(151041, "不需要走流程"),
    PROCESS_APPLY_EXIST(151042, "当前实体正在审批"),
    PROCESS_NOT_ROLLBACK(151043, "当前流程不可回滚"),
    EMAIL_NOT_SEND(151044, "email发送失败"),
    PROCESS_APPLY_OVER(151045, "流程审批已结束,请勿重复审批!"),

    /**
     * licence
     */
    LICENCE_DECRYPT_FAIL(160000, "licence解析异常"),
    MAC_DECRYPT_FAIL(160001, "Mac地址与当前计算机Mac不匹配"),
    LICENCE_EXPIRED(160002, "licence已过期"),
    CUSTOMER_ALREADY_EXISTS(160003, "客户已存在"),
    CUSTOMER_NOT_EXISTS(160004, "客户不存在"),
    NAME_IS_NULL(160005, "属性名称为空"),
    ENGLISHNAME_IS_NULL(160006, "属性英文名称为空"),
    FIELDTYPE_IS_NULL(160007, "字段类型为空"),
    DATAMETACODE_IS_NULL(160008, "数据元编号为空"),

    /**
     * 数据库类型 dmp_ods
     */
    TYPE_OF_DATABASE_DMP_ODS(17001, "dmp_ods"),

    /**
     * 数据资产模块
     */
    GET_GLOSSARY_ASSIGN_METAS_ERROR(18001, "获取业务术语关联的元数据报错"),
    CHECK_STANDARD_BE_CITED_EXITS(18002, "标准被引用"),


    ;

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
