package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fisk.auth.client.AuthClient;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.common.core.constants.RedisTokenKey;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.core.utils.Dto.SqlParmDto;
import com.fisk.common.core.utils.EnCryptUtils;
import com.fisk.common.core.utils.RegexUtils;
import com.fisk.common.core.utils.SqlParmUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.dataservice.BuildDataServiceHelper;
import com.fisk.common.service.dbBEBuild.dataservice.IBuildDataServiceSqlCommand;
import com.fisk.datamanagement.dto.labelcategory.LabelCategoryDataDTO;
import com.fisk.dataservice.dto.api.FieldEncryptConfigDTO;
import com.fisk.dataservice.dto.api.FieldEncryptDTO;
import com.fisk.dataservice.dto.apiservice.RequestEncryptDTO;
import com.fisk.dataservice.dto.apiservice.RequstDTO;
import com.fisk.dataservice.dto.apiservice.TokenDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.*;
import com.fisk.dataservice.map.ApiParmMap;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.redisdata.RedisData;
import com.fisk.dataservice.service.ApiEncryptConfigService;
import com.fisk.dataservice.service.IApiRegisterManageService;
import com.fisk.dataservice.service.IApiServiceManageService;
import com.fisk.dataservice.vo.api.ApiProxyMsgVO;
import com.fisk.dataservice.vo.apiservice.ResponseVO;
import com.fisk.dataservice.vo.app.AppWhiteListVO;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * api服务接口实现类
 *
 * @author dick
 */
@Service
@Slf4j
public class ApiServiceManageImpl implements IApiServiceManageService {

    @Resource
    private AppServiceConfigMapper appApiMapper;

    @Resource
    private ApiRegisterMapper apiRegisterMapper;

    @Resource
    private AppRegisterMapper appRegisterMapper;

    @Resource
    private ApiParmMapper apiParmMapper;

    @Resource
    private ApiBuiltinParmMapper apiBuiltinParmMapper;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    @Resource
    private LogsManageImpl logsManageImpl;

    @Resource
    private UserHelper userHelper;

    @Resource
    private AuthClient authClient;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private IApiRegisterManageService apiRegisterManageService;

    @Resource
    private ApiEncryptConfigService apiEncryptConfigService;

    @Override
    public ResultEntity<Object> getToken(TokenDTO dto) {
        String token = null;
        // 第一步：验证账号密码是否有效
        byte[] base64Encrypt = EnCryptUtils.base64Encrypt(dto.appPassword);
        String pwd = new String(base64Encrypt);
        Integer id = (Integer)redisUtil.get(RedisKeyEnum.DATA_SERVER_APP_ID  +":"+  dto.getAppAccount() + pwd);
        Long uniqueId = null;
        if (id != null){
            Long appId = Long.valueOf(id);
            uniqueId = appId + RedisTokenKey.DATA_SERVICE_TOKEN;
        }else {
            AppConfigPO byAppInfo = appRegisterMapper.getByAppInfo(dto.appAccount, pwd);
            if (byAppInfo == null)
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_API_APPINFO_EXISTS, token);
            redisUtil.set(RedisKeyEnum.DATA_SERVER_APP_ID +":"+ dto.getAppAccount() + pwd,byAppInfo.id, RedisKeyEnum.AUTH_USERINFO.getValue());
            uniqueId = byAppInfo.id + RedisTokenKey.DATA_SERVICE_TOKEN;
        }
        // 第二步：获取缓存中的token，未过期则刷新过期时间
        UserInfo userInfo = (UserInfo) redisUtil.get(RedisKeyBuild.buildLoginUserInfo(uniqueId));
        if (userInfo != null && StringUtils.isNotEmpty(userInfo.getToken())) {
            boolean isRefresh = redisUtil.expire(RedisKeyBuild.buildLoginUserInfo(uniqueId), RedisKeyEnum.AUTH_USERINFO.getValue());
            if (isRefresh)
                return ResultEntityBuild.buildData(ResultEnum.REQUEST_SUCCESS, userInfo.getToken());
        }
        // 第二步：调用授权接口，根据账号密码生成token
        UserAuthDTO userAuthDTO = new UserAuthDTO();
        userAuthDTO.setUserAccount(dto.appAccount);
        userAuthDTO.setPassword(dto.appPassword);
        userAuthDTO.setTemporaryId(uniqueId);
        ResultEntity<String> tokenEntity = authClient.getToken(userAuthDTO);
        if (tokenEntity.code == 0 && !tokenEntity.data.isEmpty())
            token = tokenEntity.data;
        return ResultEntityBuild.buildData(ResultEnum.REQUEST_SUCCESS, token);
    }

    @Override
    public ResultEntity<Object> getData(RequstDTO dto) {
        ResponseVO responseVO = new ResponseVO();
        String appAccount = "";
        ResultEnum resultEnum = ResultEnum.REQUEST_SUCCESS;
        // 日志实体类
        LogPO logPO = new LogPO();
        Statement st = null;
        Connection conn = null;

        //有缓存则直接返回并记录日志
        RedisKeyEnum.DATA_SERVER_API_DATA.getName();
        String parmKey = null;
        if (CollectionUtils.isNotEmpty(dto.parmList)){
            parmKey = dto.parmList.entrySet().stream()
                    .map(entry -> entry.getKey() + "_" + entry.getValue())
                    .collect(Collectors.joining("_"));
        }
        String redisKey = RedisKeyEnum.DATA_SERVER_API_DATA.getName()+":"+dto.apiCode+"_"+parmKey+"_"+dto.current+"_"+dto.size;
        RedisData redisResult = (RedisData) redisUtil.get(redisKey);
        if (redisResult != null){
            logPO = redisResult.logPO;
            logPO.setLogInfo("(本次查询为缓存数据)");
            responseVO = redisResult.getResponseVO();
            return ResultEntityBuild.buildData(resultEnum, responseVO);
        }
        try {
            // 开始记录日志
            logPO.setRequestStartDate(DateTimeUtils.getNow());
            logPO.setLogLevel(LogLevelTypeEnum.INFO.getName());
            logPO.setLogRequest(JSON.toJSONString(dto));
            logPO.setLogType(LogTypeEnum.API.getValue());
            logPO.setBusinessState("失败");

            UserInfo userInfo = userHelper.getLoginUserInfo();
            if (userInfo == null) {
                resultEnum = ResultEnum.AUTH_LOGIN_INFO_INVALID;
                return ResultEntityBuild.buildData(ResultEnum.AUTH_LOGIN_INFO_INVALID, responseVO);
            }

            // 第一步：验证是否已进行授权认证
            appAccount = userInfo.getUsername();
            if (appAccount == null || appAccount.isEmpty()) {
                resultEnum = ResultEnum.AUTH_LOGIN_INFO_INVALID;
                return ResultEntityBuild.buildData(ResultEnum.AUTH_LOGIN_INFO_INVALID, responseVO);
            }

            // 第二步：验证当前应用（下游系统）是否有效
            AppConfigPO appInfo = appRegisterMapper.getByAppAccount(appAccount);
            if (appInfo == null) {
                resultEnum = ResultEnum.DS_APISERVICE_APP_EXISTS;
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_APP_EXISTS, responseVO);
            } else {
                logPO.setAppId(Math.toIntExact(appInfo.getId()));
            }

            // 第三步：验证当前请求的API是否有效
            ApiConfigPO apiInfo = apiRegisterMapper.getByApiCode(dto.apiCode);
            if (apiInfo == null) {
                resultEnum = ResultEnum.DS_APISERVICE_API_EXISTS;
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_API_EXISTS, responseVO);
            } else {
                logPO.setApiId(Math.toIntExact(apiInfo.getId()));
                logPO.setCreateUser(appInfo.getAppAccount());
            }
            //验证api是否在有效期内
            if (apiInfo.getExpirationType() == 2){
                LocalDateTime currentDateTime = LocalDateTime.now();
                if (currentDateTime.isAfter(apiInfo.getExpirationTime())){
                    resultEnum = ResultEnum.DS_APISERVICE__EXPIRATION;
                    return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE__EXPIRATION, responseVO);
                }
            }

            // 第四步：验证API配置完整
            if (StringUtils.isEmpty(apiInfo.getCreateSql())) {
                return ResultEntityBuild.buildData(ResultEnum.API_NOT_CONFIGURED_FOR_OUTPUT_CONFIGURATION, responseVO);
            }

            // 第五步：验证当前请求的API是否具备访问权限
            AppServiceConfigPO subscribeBy = appApiMapper.getSubscribeBy(Math.toIntExact(appInfo.id), Math.toIntExact(apiInfo.id));
            if (subscribeBy == null) {
                resultEnum = ResultEnum.DS_APISERVICE_APP_NOTSUB;
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_APP_NOTSUB, responseVO);
            }
            if (subscribeBy.apiState == ApiStateTypeEnum.Disable.getValue()) {
                resultEnum = ResultEnum.DS_APISERVICE_APP_NOTENABLE;
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_APP_NOTENABLE, responseVO);
            }

            // 第六步：验证数据源是否有效
            DataSourceConVO dataSourceConVO = dataSourceConManageImpl.getAllDataSource()
                    .stream().filter(t -> t.getId() == apiInfo.datasourceId).findFirst().orElse(null);
            if (dataSourceConVO == null) {
                resultEnum = ResultEnum.DS_APISERVICE_DATASOURCE_EXISTS;
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_DATASOURCE_EXISTS, responseVO);
            }

            // 第七步：获取请求参数中的分页信息
            Integer current = dto.getCurrent();
            Integer size = dto.getSize();
            // 未设置分页页数，默认查询第一页
            if (current == null) {
                current = 1;
                dto.setCurrent(current);
            }
            //判断是否有最大条数限制
            if (apiInfo.getMaxSizeType() == 1){
                if (size == null || size > apiInfo.getMaxSize()) {
                    size = apiInfo.getMaxSize();
                    dto.setSize(size);
                }
            }else if (size == null){
                dto.setSize(500);
            }
//            // 第七步：获取请求参数中的分页信息 限制100条  2023-06-01 李世纪：暂时恢复原状
//            Integer current = dto.getCurrent();
//            Integer size = dto.getSize();
//            // 如果传参的分页大小不为空，判断是否大于100，如果大于100，返回提示：分页大小最大为100
//            if (size != null) {
//                if (size > 100) {
//                    return ResultEntityBuild.build(ResultEnum.DS_DATA_SIZE_CANNOT_BE_GREATER_THAN_100);
//                }
//            }
//            if (current == null && size == null) {
//                // 未设置分页参数，默认查询第一页，查询数字的最大值
//                current = 1;
//                // 未设置分页大小（size），默认100
//                size = 100;
//            }
            log.info("数据服务【getData】分页参数【current】：" + current);
            log.info("数据服务【getData】分页参数【size】：" + size);
            if (current == null || current <= 0) {
                return ResultEntityBuild.buildData(ResultEnum.DS_DATA_PAGE_SHOULD_BE_GREATER_THAN_0, responseVO);
            } else if (size == null || size <= 0) {
                return ResultEntityBuild.buildData(ResultEnum.DS_DATA_SIZE_SHOULD_BE_GREATER_THAN_0, responseVO);
            }

            // 第八步：查询参数信息，如果参数设置为内置参数，则以内置参数为准，反之则以传递的参数为准，如果没设置内置参数&参数列表中未传递，默认为空//则读取后台配置的参数值
            List<ParmConfigPO> paramList = apiParmMapper.getListByApiId(Math.toIntExact(apiInfo.getId()));
            if (CollectionUtils.isNotEmpty(paramList)) {
                if (!CollectionUtils.isNotEmpty(dto.getParmList())) {
                    dto.parmList = new HashMap<>();
                }

                if (apiInfo.getApiType() == ApiTypeEnum.SQL.getValue()) {
                    // 移除分页参数
                    paramList = paramList.stream().filter(t -> !t.getParmName().equals("current") && !t.getParmName().equals("size")).collect(Collectors.toList());
                } else if (apiInfo.getApiType() == ApiTypeEnum.CUSTOM_SQL.getValue()) {
                    // 追加分页参数到请求参数，用于赋值给PO中的分页参数
                    dto.parmList.put("current", current);
                    dto.parmList.put("size", size);
                }
                //如果是强生分支则将for (ParmConfigPO parmConfigPO : paramList) 注释掉并放开下面的 paramList.forEach(e -> {
                for (ParmConfigPO parmConfigPO : paramList) {
                    Map.Entry<String, Object> stringObjectEntry = dto.getParmList().entrySet().stream().filter(item -> item.getKey().equals(parmConfigPO.getParmName())).findFirst().orElse(null);
                    if (stringObjectEntry != null) {
                        parmConfigPO.setParmValue(String.valueOf(stringObjectEntry.getValue()));
                    } else {
                        return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_PARAMLIST_IS_NULL, responseVO);
                    }
                }

//                paramList.forEach(e -> {
//                    Map.Entry<String, Object> stringObjectEntry = dto.getParmList().entrySet().stream().filter(item -> item.getKey().equals(e.getParmName())).findFirst().orElse(null);
//                    if (stringObjectEntry != null) {
//                        e.setParmValue(String.valueOf(stringObjectEntry.getValue()));
//                    } else {
//                        e.setParmValue(null);
//                    }
//                });

                List<Long> collect = paramList.stream().map(ParmConfigPO::getId).collect(Collectors.toList());
                List<BuiltinParmPO> builtinParamList = apiBuiltinParmMapper.getListBy(Math.toIntExact(appInfo.id), Math.toIntExact(apiInfo.id), collect);
                if (CollectionUtils.isNotEmpty(builtinParamList)) {
                    paramList.forEach(e -> {
                        Optional<BuiltinParmPO> builtinParamOptional = builtinParamList.stream().filter(item -> item.getParmId() == e.id).findFirst();
                        if (builtinParamOptional.isPresent()) {
                            BuiltinParmPO builtinParmPO = builtinParamOptional.get();
                            if (builtinParmPO != null)
                                e.setParmValue(builtinParmPO.parmValue);
                        }
                    });
                }
            }

            // 第九步：拼接最终执行的SQL
            String sql = "";
            String countSql = "";
            IBuildDataServiceSqlCommand dbCommand = BuildDataServiceHelper.getDBCommand(dataSourceConVO.getConType());
            if (apiInfo.getApiType() == ApiTypeEnum.SQL.getValue()) {
                // 获取分页条件
                String fields = apiInfo.getCreateSql();
                String orderBy = fields.split(",")[0];
                List<SqlParmDto> sqlParamsDto = ApiParmMap.INSTANCES.listPoToSqlParmDto(paramList);
                String sql_Where = SqlParmUtils.SqlParams(sqlParamsDto, "@");
                if (StringUtils.isNotEmpty(sql_Where)) {
                    sql_Where = SqlParmUtils.SqlParams(sqlParamsDto, sql_Where, "@", dataSourceConVO.getConType());
                }
                // 获取分页sql语句
                sql = dbCommand.buildPagingSql(apiInfo.getTableName(), fields, orderBy, current, size, sql_Where);
                countSql = dbCommand.buildQueryCountSql(apiInfo.getTableName(), sql_Where);
                log.info("数据服务【getData】普通模式SQL参数【sql】：" + sql);
                log.info("数据服务【getData】普通模式SQL参数【countSql】：" + countSql);
            } else if (apiInfo.getApiType() == ApiTypeEnum.CUSTOM_SQL.getValue()) {
                List<SqlParmDto> sqlParamsDto = ApiParmMap.INSTANCES.listPoToSqlParmDto(paramList);
                if (dataSourceConVO.getConType() == DataSourceTypeEnum.DORIS
                        || dataSourceConVO.getConType() ==DataSourceTypeEnum.MYSQL
                        || dataSourceConVO.getConType() ==DataSourceTypeEnum.DM8){
                    List<SqlParmDto> pageNo = sqlParamsDto.stream().filter(i -> i.parmName == "@start" || i.parmName == "@end").collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(pageNo)){
                        SqlParmDto sqlParmStart = new SqlParmDto();
                        sqlParmStart.parmName = "start";
                        sqlParmStart.parmValue = String.valueOf((current-1) * size);
                        SqlParmDto sqlParmEnd = new SqlParmDto();
                        sqlParmEnd.parmName = "end";
                        sqlParmEnd.parmValue = String.valueOf(size);
                        sqlParamsDto.add(sqlParmStart);
                        sqlParamsDto.add(sqlParmEnd);
                    }
                }
                sql = SqlParmUtils.SqlParams(sqlParamsDto, apiInfo.getCreateSql(), "@", dataSourceConVO.getConType());
                countSql = SqlParmUtils.SqlParams(sqlParamsDto, apiInfo.getCreateCountSql(), "@", dataSourceConVO.getConType());
                log.info("数据服务【getData】自定义模式SQL参数【sql】：" + sql);
                log.info("数据服务【getData】自定义模式SQL参数【countSql】：" + countSql);
            }

            //判断是否需要加密字段
            Boolean encrypt = false;
            Boolean desensitization = false;
            List<String> fieldName = new ArrayList<>();

            Map<String, FieldEncryptDTO> fieldDesensitization = new HashMap<>();
            String encryptKey = null;
            String[] encryptedFields = null;
            FieldEncryptConfigDTO fieldEncryptAll = apiRegisterManageService.getFieldEncryptAll((int) apiInfo.id);
            if (fieldEncryptAll != null){
                Integer type = fieldEncryptAll.getType();
                if (type !=null){
                    if (type == 1){
                        if (StringUtils.isNotEmpty(fieldEncryptAll.getEncryptKey())){
                            encrypt = true;
                            encryptKey = fieldEncryptAll.getEncryptKey();
                        }
                        List<FieldEncryptDTO> fieldEncryptDTOS = fieldEncryptAll.getFieldEncryptDTOS();
                        if (CollectionUtils.isNotEmpty(fieldEncryptDTOS)){
                            fieldName = fieldEncryptDTOS.stream().filter(i->i.getEncrypt() == 1).map(FieldEncryptDTO::getFieldName).collect(Collectors.toList());
                            encryptedFields = fieldName.toArray(new String[0]);
                        }
                    }else if (type == 2){
                        desensitization = true;
                        List<FieldEncryptDTO> fieldEncryptDTOS = fieldEncryptAll.getFieldEncryptDTOS();
                        if (CollectionUtils.isNotEmpty(fieldEncryptDTOS)){
                            fieldDesensitization = fieldEncryptDTOS.stream().filter(i -> i.getDesensitization() != 0).collect(Collectors.toMap(FieldEncryptDTO::getFieldName, i -> i));
                            List<String> names = fieldDesensitization.values().stream().map(FieldEncryptDTO::getFieldName).collect(Collectors.toList());
                            encryptedFields = names.toArray(new String[0]);
                        }
                    }
                }

            }
            responseVO.setEncryptedFields(encryptedFields);
            // 第十步：判断数据源类型，加载数据库驱动，执行SQL
            logPO.setParamCheckDate(DateTimeUtils.getNow());
            conn = dataSourceConManageImpl.getStatement(dataSourceConVO.getConType(), dataSourceConVO.getConStr(), dataSourceConVO.getConAccount(), dataSourceConVO.getConPassword());
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);
            JSONArray dataArray = new JSONArray();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                JSONObject jsonObj = new JSONObject();
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    //获取sql查询数据集合
                    Object value = rs.getObject(columnName);

                    //加密字段
                    if (encrypt){
                        if (fieldName.contains(columnName)){
                            if (!ObjectUtils.isEmpty(value)) {
                                value = encryptField(value.toString(), encryptKey);
                            }
                        }
                    }else if (desensitization){
                        if (fieldDesensitization.containsKey(columnName)){
                            if (!ObjectUtils.isEmpty(value)) {
                                FieldEncryptDTO fieldEncryptDTO = fieldDesensitization.get(columnName);
                                String val = value.toString();
                                switch (fieldEncryptDTO.getDesensitization()){
                                    case 1:
                                        if (val.length() > 1) {
                                            // 取出第一个字符
                                            char firstChar = val.charAt(0);
                                            // 拼接结果
                                            value = firstChar + maskString(val.length()-1);
                                        }
                                        break;
                                    case 2:
                                        if (val.length() >= 7) {
                                            // 保留前三位和后四位，中间的内容用星号展示
                                            String firstThree = val.substring(0, 3);
                                            String lastFour = val.substring(val.length() - 4);
                                            value = firstThree + maskString(val.length() - 7) + lastFour;
                                        }else {
                                            value = maskString(val.length());
                                        }
                                        break;
                                    case 3:
                                        if (val.length() >= 6) {
                                            // 保留前三位和后四位，中间的内容用星号展示
                                            String firstThree = val.substring(0, 3);
                                            String lastFour = val.substring(val.length() - 3);
                                            value = firstThree + maskString(val.length() - 6) + lastFour;
                                        }else {
                                            value = maskString(val.length());
                                        }
                                        break;
                                    case 4:
                                        // 全部用星号展示补全
                                        value = maskString(val.length());
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }
                    jsonObj.put(columnName, value);
                }
                dataArray.add(jsonObj);
            }
            rs.close();

            int totalCount = 0;
            if (StringUtils.isNotEmpty(countSql)) {
                ResultSet countRs = st.executeQuery(countSql);
                if (countRs.next()) {
                    Object count = countRs.getObject(1);
                    if (count != null && RegexUtils.isNumeric(count) != null)
                        totalCount = RegexUtils.isNumeric(count);
                }
                countRs.close();
            }
//            if (encrypt){
//                responseVO.setEncryptKey(encryptKey);
//            }
            if (dto.getCurrent() != null && dto.getSize() != null) {
                responseVO.setCurrent(current);
                responseVO.setSize(size);
                responseVO.setPage((int) Math.ceil(1.0 * totalCount / size));
            }
            responseVO.setTotal(totalCount);
            responseVO.setDataArray(dataArray);
            // 数组类型资源释放，将其设置为null
            dataArray = null;
            logPO.setLogResponseInfo(String.valueOf(totalCount));
            logPO.setBusinessState("成功");
            logPO.setResponseStatus(HttpStatus.OK.getReasonPhrase());
            logPO.setNumber(totalCount);
            logPO.setImportantInterface(apiInfo.getImportantInterface());
            if (apiInfo.getEnableCache() == 1){
                //缓存本次查询至redis
                RedisData redisData = new RedisData();
                redisData.setResponseVO(responseVO);
                redisData.setLogPO(logPO);
                redisUtil.set(redisKey,redisData,apiInfo.getCacheTime());
            }
        } catch (Exception e) {
            logPO.setLogLevel(LogLevelTypeEnum.ERROR.getName());
            logPO.setLogInfo(e.toString());
            logPO.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            resultEnum = ResultEnum.DS_APISERVICE_QUERY_ERROR;
            throw new FkException(ResultEnum.DS_APISERVICE_QUERY_ERROR, e);
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
            if (resultEnum != ResultEnum.REQUEST_SUCCESS) {
                if (resultEnum == ResultEnum.DS_APISERVICE_QUERY_ERROR) {
                    logPO.setLogInfo(ResultEnum.DS_APISERVICE_QUERY_ERROR.getMsg() + "。错误信息：" + logPO.getLogInfo());
                } else {
                    logPO.setLogInfo(resultEnum.getMsg());
                }
            }
            try {
                log.info("开始记录数据服务调用日志");
                logPO.setRequestEndDate(DateTimeUtils.getNow());
                logsManageImpl.saveLog(logPO);
            } catch (Exception exs) {
                log.error("数据服务调用日志保存异常：" + exs);
            }
            // 通知gc进行资源释放
            // System.gc();
        }
        return ResultEntityBuild.buildData(resultEnum, responseVO);
    }
    // 封装生成星号部分的方法
    public static String maskString(Integer number) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < number; i++) {
            stars.append('*');
        }
        return stars.toString();
    }

    @Override
    public ResultEntity<Object> getEncryptKey(RequestEncryptDTO dto) {
        String appAccount = "";
        ResultEnum resultEnum = ResultEnum.REQUEST_SUCCESS;
        UserInfo userInfo = userHelper.getLoginUserInfo();
        if (userInfo == null) {
            resultEnum = ResultEnum.AUTH_LOGIN_INFO_INVALID;
            return ResultEntityBuild.buildData(ResultEnum.AUTH_LOGIN_INFO_INVALID, null);
        }

        // 第一步：验证是否已进行授权认证
        appAccount = userInfo.getUsername();
        if (appAccount == null || appAccount.isEmpty()) {
            resultEnum = ResultEnum.AUTH_LOGIN_INFO_INVALID;
            return ResultEntityBuild.buildData(ResultEnum.AUTH_LOGIN_INFO_INVALID, null);
        }

        // 第二步：验证当前应用（下游系统）是否有效
        AppConfigPO appInfo = appRegisterMapper.getByAppAccount(appAccount);
        if (appInfo == null) {
            resultEnum = ResultEnum.DS_APISERVICE_APP_EXISTS;
            return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_APP_EXISTS, null);
        }

        // 第三步：验证当前请求的API是否有效
        ApiConfigPO apiInfo = apiRegisterMapper.getByApiCode(dto.apiCode);
        if (apiInfo == null) {
            resultEnum = ResultEnum.DS_APISERVICE_API_EXISTS;
            return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_API_EXISTS, null);
        }
        //验证api是否在有效期内
        if (apiInfo.getExpirationType() == 2){
            LocalDateTime currentDateTime = LocalDateTime.now();
            if (currentDateTime.isAfter(apiInfo.getExpirationTime())){
                resultEnum = ResultEnum.DS_APISERVICE__EXPIRATION;
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE__EXPIRATION, null);
            }
        }
        // 第五步：验证当前请求的API是否具备访问权限
        AppServiceConfigPO subscribeBy = appApiMapper.getSubscribeBy(Math.toIntExact(appInfo.id), Math.toIntExact(apiInfo.id));
        if (subscribeBy == null) {
            resultEnum = ResultEnum.DS_APISERVICE_APP_NOTSUB;
            return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_APP_NOTSUB, null);
        }
        if (subscribeBy.apiState == ApiStateTypeEnum.Disable.getValue()) {
            resultEnum = ResultEnum.DS_APISERVICE_APP_NOTENABLE;
            return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_APP_NOTENABLE, null);
        }

        String encryptKey = null;
        ApiConfigPO byApiCode = apiRegisterMapper.getByApiCode(dto.apiCode);
        if (byApiCode == null){
            resultEnum = ResultEnum.NOTFOUND;
        }else {
            LambdaQueryWrapper<ApiEncryptConfigPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ApiEncryptConfigPO::getApiId,byApiCode.id);
            ApiEncryptConfigPO encrypt = apiEncryptConfigService.getOne(queryWrapper);
            if (encrypt == null){
                resultEnum = ResultEnum.NOTFOUND;
            }else {
                encryptKey = encrypt.getEncryptKey();
            }
        }
        return ResultEntityBuild.buildData(resultEnum, encryptKey);
    }

    private static String encryptField(String fieldValue, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(fieldValue.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @Override
    public void proxy(HttpServletRequest request, HttpServletResponse response) {
        // 开始记录日志
        LogPO logPO = new LogPO();
        logPO.setRequestStartDate(DateTimeUtils.getNow());
        logPO.setLogType(LogTypeEnum.AGENT_API.getValue());
        logPO.setLogLevel(LogLevelTypeEnum.INFO.getName());
        logPO.setBusinessState("失败");
        ResultEnum resultEnum = ResultEnum.REQUEST_SUCCESS;

        String scheme = request.getScheme(); // 获取协议 (http 或 https)
        String serverName = request.getServerName(); // 获取服务器名
        int serverPort = request.getServerPort(); // 获取服务器端口号
        String contextPath = request.getContextPath(); // 获取应用上下文路径
        String servletPath = request.getServletPath(); // 获取Servlet路径
        String queryString = request.getQueryString(); // 获取请求参数
        StringBuilder fullURL = new StringBuilder();
        fullURL.append(scheme).append("://").append(serverName);
        if (serverPort != 80 && serverPort != 443) {
            fullURL.append(":").append(serverPort);
        }
        fullURL.append(contextPath).append(servletPath);
        if (queryString != null) {
            fullURL.append("?").append(queryString);
        }
        String logInfo = "代理转发接口地址：" + fullURL + ",\n";
        try {
            String apiCode = request.getRequestURI().replace("/proxy/", "");
            // 验证是否携带apiCode
            if (StringUtils.isEmpty(apiCode)) {
                resultEnum = ResultEnum.DS_MISSING_APICODE_IN_URL;
                doSetResponse(resultEnum, response);
                return;
            }
            // 验证api是否存在
            ApiConfigPO apiConfigPO = apiRegisterMapper.getByApiCode(apiCode);
            if (apiConfigPO == null) {
                resultEnum = ResultEnum.DS_API_EXISTS;
                doSetResponse(resultEnum, response);
                return;
            }
            //验证api是否在有效期内
            if (apiConfigPO.getExpirationType() == 2){
                LocalDateTime currentDateTime = LocalDateTime.now();
                if (currentDateTime.isAfter(apiConfigPO.getExpirationTime())){
                    resultEnum = ResultEnum.DS_APISERVICE__EXPIRATION;
                    doSetResponse(resultEnum, response);
                    return;
                }
            }
            logPO.setApiId(Math.toIntExact(apiConfigPO.getId()));
            // 验证是否已订阅该api
            List<AppWhiteListVO> appWhiteList = appApiMapper.getAppWhiteListByServiceId(apiConfigPO.getId());
            if (CollectionUtils.isEmpty(appWhiteList)) {
                resultEnum = ResultEnum.DS_APISERVICE_APP_NOTSUB;
                doSetResponse(resultEnum, response);
                return;
            }
            // 验证订阅的api是否启用
            appWhiteList = appWhiteList.stream().filter(t -> t.getApiState() == ApiStateTypeEnum.Enable.getValue()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(appWhiteList)) {
                resultEnum = ResultEnum.DS_APISERVICE_APP_NOTENABLE;
                doSetResponse(resultEnum, response);
                return;
            }
            // 验证客户端ip是否存在于白名单中
            String clientIp = request.getRemoteAddr();
            appWhiteList = appWhiteList.stream().filter(t -> t.getAppWhiteListState() == 2 || t.getAppWhiteList().contains(clientIp)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(appWhiteList)) {
                resultEnum = ResultEnum.DS_ILLEGAL_REQUEST;
                doSetResponse(resultEnum, response);
                return;
            }
            //验证是否需要授权认证
            List<AppWhiteListVO> auth = appWhiteList.stream().filter(i->i.getProxyAuthorizationSwitch() == 1).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(auth)){
                //验证是否跳过授权认证
                UserInfo userInfo = userHelper.getLoginUserInfo();
                if (userInfo == null) {
                    resultEnum = ResultEnum.AUTH_LOGIN_INFO_INVALID;
                    doSetResponse(resultEnum, response);
                    return;
                }

                // 验证是否已进行授权认证
                String appAccount = userInfo.getUsername();
                if (appAccount == null || appAccount.isEmpty()) {
                    resultEnum = ResultEnum.AUTH_LOGIN_INFO_INVALID;
                    doSetResponse(resultEnum, response);
                    return;
                }
                // 验证当前应用（下游系统）是否有效
                AppConfigPO appInfo = appRegisterMapper.getByAppAccount(appAccount);
                if (appInfo == null) {
                    resultEnum = ResultEnum.DS_APISERVICE_APP_EXISTS;
                    doSetResponse(resultEnum, response);
                    return;
                } else {
                    logPO.setAppId(Math.toIntExact(appInfo.getId()));
                }
            }
            //验证api是否
            List<Integer> appIds = appWhiteList.stream().map(AppWhiteListVO::getAppId).collect(Collectors.toList());
            logPO.setAppIds(Joiner.on(",").join(appIds));
            logPO.setAppId(appIds.get(0));

            logPO.setParamCheckDate(DateTimeUtils.getNow());
            String query = request.getQueryString();
            // 构建目标 URL
            String url = apiConfigPO.getApiProxyUrl();
            if (url.contains("?")) {
                url += StringUtils.isNotEmpty(query) ? "?" + query.replace("?", "&") : "";
            } else {
                url += StringUtils.isNotEmpty(query) ? "?" + query : "";
            }
            logInfo += "代理转发目标接口地址：" + url + ",\n";
            URI targetUri = new URI(url);

            String methodName = request.getMethod();
            logInfo += "代理转发目标接口类型：" + methodName + ",\n";
            HttpMethod httpMethod = HttpMethod.resolve(methodName);
            if (httpMethod == null) {
                return;
            }
            ClientHttpRequest delegate = new SimpleClientHttpRequestFactory().createRequest(targetUri, httpMethod);
            Enumeration<String> headerNames = request.getHeaderNames();
            // 设置请求头
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> v = request.getHeaders(headerName);
                List<String> arr = new ArrayList<>();
                while (v.hasMoreElements()) {
                    arr.add(v.nextElement());
                }
                delegate.getHeaders().addAll(headerName, arr);
            }
            //StreamUtils.copy(request.getInputStream(), delegate.getBody());
            try (InputStream inputStream = request.getInputStream();
                 OutputStream outputStream = delegate.getBody()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
            logPO.setLogRequest("代理转发请求数据流大小：" + request.getContentLength() + "bytes");
            // 执行远程调用
            ClientHttpResponse clientHttpResponse = delegate.execute();
            // 设置响应状态码
            response.setStatus(clientHttpResponse.getStatusCode().value());
            logPO.setResponseStatus(clientHttpResponse.getStatusCode().getReasonPhrase());
            // 设置响应头
            clientHttpResponse.getHeaders().forEach((key, value) -> value.forEach(it -> {
                response.setHeader(key, it);
            }));
            /*
             流式写入数据，避免数据量过大一次性加载到内容导致内存溢出
             StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
                        try (InputStream inputStream = clientHttpResponse.getBody();
                             OutputStream outputStream = response.getOutputStream()) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            outputStream.flush();
                        }
            */
            try (InputStream inputStream = clientHttpResponse.getBody();
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                int totalBytesRead = 0; // 总字节数

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }

                outputStream.flush();

                byte[] responseData = byteArrayOutputStream.toByteArray();
                int responseDataSize = responseData.length;

                logPO.setLogResponseInfo("代理转发响应数据流大小：" + totalBytesRead + "bytes,\n代理转发响应数据量大小：" + responseDataSize + "bytes");
            }
            logPO.setBusinessState("成功");
        } catch (Exception ex) {
            log.error("代理转发异常：" + ex);
            logInfo += "代理转发异常：" + ex + ",\n";
            logPO.setResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            resultEnum = ResultEnum.DS_PROXY_FORWARDING_ERROR;
            doSetResponse(resultEnum, response);
        } finally {
            logInfo += "代理转发接口执行结果：" + resultEnum + "";
            logPO.setLogInfo(logInfo);
            logPO.setRequestEndDate(DateTimeUtils.getNow());
            try {
                logsManageImpl.saveLog(logPO);
            } catch (Exception exs) {
                log.error("代理服务转发请求-日志保存异常：" + exs);
            }
        }
    }

    private void doSetResponse(ResultEnum resultEnum, HttpServletResponse response) {
        try {
            ApiProxyMsgVO resultEntity = new ApiProxyMsgVO();
            resultEntity.setCode(resultEnum.getCode());
            resultEntity.setMsg(resultEnum.getMsg());

            // 将对象转换为JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(resultEntity);

            // 设置响应的Content-Type为"application/json"
            response.setContentType("application/json");

            // 将JSON字符串写入HttpServletResponse对象
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(json.getBytes("UTF-8"));

            // 刷新输出流并关闭资源
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {
            log.error("doSetResponse ex：" + ex);
        }
    }
}
