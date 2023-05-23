package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.auth.client.AuthClient;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.common.core.constants.RedisTokenKey;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
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
import com.fisk.dataservice.dto.apiservice.RequstDTO;
import com.fisk.dataservice.dto.apiservice.TokenDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.ApiStateTypeEnum;
import com.fisk.dataservice.enums.ApiTypeEnum;
import com.fisk.dataservice.enums.LogLevelTypeEnum;
import com.fisk.dataservice.enums.LogTypeEnum;
import com.fisk.dataservice.map.ApiParmMap;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IApiServiceManageService;
import com.fisk.dataservice.vo.apiservice.ResponseVO;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private ApiFilterConditionMapper apiFilterConditionMapper;

    @Resource
    private LogsManageImpl logsManageImpl;

    @Resource
    private UserHelper userHelper;

    @Resource
    private AuthClient authClient;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public ResultEntity<Object> getToken(TokenDTO dto) {
        String token = null;
        // 第一步：验证账号密码是否有效
        byte[] base64Encrypt = EnCryptUtils.base64Encrypt(dto.appPassword);
        String pwd = new String(base64Encrypt);
        AppConfigPO byAppInfo = appRegisterMapper.getByAppInfo(dto.appAccount, pwd);
        if (byAppInfo == null)
            return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_API_APPINFO_EXISTS, token);
        Long uniqueId = byAppInfo.id + RedisTokenKey.DATA_SERVICE_TOKEN;
        // 第二步：获取缓存中的token，未过期则刷新过期时间
        UserInfo userInfo = (UserInfo) redisUtil.get(RedisKeyBuild.buildLoginUserInfo(uniqueId));
        if (userInfo != null && StringUtils.isNotEmpty(userInfo.getToken())) {
            boolean isRefresh = redisUtil.expire(RedisKeyBuild.buildLoginUserInfo(uniqueId), RedisKeyEnum.AUTH_USERINFO.getValue());
            if (isRefresh)
                return ResultEntityBuild.buildData(ResultEnum.REQUEST_SUCCESS, userInfo.getToken());
        }
        // 第二步：调用授权接口，根据账号密码生成token
        UserAuthDTO userAuthDTO = new UserAuthDTO();
        userAuthDTO.setUserAccount(byAppInfo.appAccount);
        userAuthDTO.setPassword(byAppInfo.appPassword);
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

        try {
            // 开始记录日志
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
            // 如果传参的分页大小不为空，判断是否大于100，如果大于100，返回提示：分页大小最大为100
            if (size != null) {
                if (size > 100) {
                    return ResultEntityBuild.build(ResultEnum.DS_DATA_SIZE_CANNOT_BE_GREATER_THAN_100);
                }
            }

            if (current == null && size == null) {
                // 未设置分页参数，默认查询第一页，查询数字的最大值
                current = 1;
                // 未设置分页大小（size），默认100
                size = 100;
            }
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

                paramList.forEach(e -> {
                    Map.Entry<String, Object> stringObjectEntry = dto.getParmList().entrySet().stream().filter(item -> item.getKey().equals(e.getParmName())).findFirst().orElse(null);
                    if (stringObjectEntry != null) {
                        e.setParmValue(String.valueOf(stringObjectEntry.getValue()));
                    } else {
                        e.setParmValue(null);
                    }
                });

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
                sql = SqlParmUtils.SqlParams(sqlParamsDto, apiInfo.getCreateSql(), "@", dataSourceConVO.getConType());
                countSql = SqlParmUtils.SqlParams(sqlParamsDto, apiInfo.getCreateCountSql(), "@", dataSourceConVO.getConType());
                log.info("数据服务【getData】自定义模式SQL参数【sql】：" + sql);
                log.info("数据服务【getData】自定义模式SQL参数【countSql】：" + countSql);
            }

            // 第十步：判断数据源类型，加载数据库驱动，执行SQL
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
        } catch (Exception e) {
            logPO.setLogLevel(LogLevelTypeEnum.ERROR.getName());
            logPO.setLogInfo(e.getMessage());
            resultEnum = ResultEnum.DS_APISERVICE_QUERY_ERROR;
            throw new FkException(ResultEnum.DS_APISERVICE_QUERY_ERROR, e.getMessage());
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
            if (resultEnum != ResultEnum.REQUEST_SUCCESS) {
                if (resultEnum == ResultEnum.DS_APISERVICE_QUERY_ERROR) {
                    logPO.setLogInfo(ResultEnum.DS_APISERVICE_QUERY_ERROR.getMsg() + "。错误信息：" + logPO.logInfo);
                } else {
                    logPO.setLogInfo(resultEnum.getMsg());
                }
            }
            try {
                log.info("开始记录数据服务调用日志");
                logsManageImpl.saveLog(logPO);
            } catch (Exception exs) {
                log.error("数据服务调用日志保存异常：" + exs);
            }
            // 通知gc进行资源释放
            // System.gc();
        }
        return ResultEntityBuild.buildData(resultEnum, responseVO);
    }
}
