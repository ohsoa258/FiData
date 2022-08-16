package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.auth.client.AuthClient;
import com.fisk.auth.dto.UserAuthDTO;
import com.fisk.common.core.constants.RedisTokenKey;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.core.utils.Dto.SqlParmDto;
import com.fisk.common.core.utils.Dto.SqlWhereDto;
import com.fisk.common.core.utils.EnCryptUtils;
import com.fisk.common.core.utils.SqlParmUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.dto.apiservice.RequstDTO;
import com.fisk.dataservice.dto.apiservice.TokenDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.*;
import com.fisk.dataservice.map.ApiFilterConditionMap;
import com.fisk.dataservice.map.ApiParmMap;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IApiServiceManageService;
import com.fisk.dataservice.vo.apiservice.ResponseVO;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
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
public class ApiServiceManageImpl implements IApiServiceManageService {

    @Resource
    private AppApiMapper appApiMapper;

    @Resource
    private ApiRegisterMapper apiRegisterMapper;

    @Resource
    private AppRegisterMapper appRegisterMapper;

    @Resource
    private ApiParmMapper apiParmMapper;

    @Resource
    private ApiBuiltinParmMapper apiBuiltinParmMapper;

    @Resource
    private DataSourceConMapper dataSourceConMapper;

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

    @Override
    public ResultEntity<Object> getToken(TokenDTO dto) {
        String token = null;
        // 第一步：验证账号密码是否有效
        byte[] base64Encrypt = EnCryptUtils.base64Encrypt(dto.appPassword);
        String pwd = new String(base64Encrypt);
        AppConfigPO byAppInfo = appRegisterMapper.getByAppInfo(dto.appAccount, pwd);
        if (byAppInfo == null)
            return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_API_APPINFO_EXISTS, token);
        // 第二步：调用授权接口，根据账号密码生成token
        Long uniqueId = byAppInfo.id + RedisTokenKey.DATA_SERVICE_TOKEN;
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
            appAccount = userInfo.username;
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
            }

            // 第四步：验证当前请求的API是否具备访问权限
            AppApiPO subscribeBy = appApiMapper.getSubscribeBy(Math.toIntExact(appInfo.id), Math.toIntExact(apiInfo.id));
            if (subscribeBy == null) {
                resultEnum = ResultEnum.DS_APISERVICE_APP_NOTSUB;
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_APP_NOTSUB, responseVO);
            }
            if (subscribeBy.apiState == ApiStateTypeEnum.Disable.getValue()) {
                resultEnum = ResultEnum.DS_APISERVICE_APP_NOTENABLE;
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_APP_NOTENABLE, responseVO);
            }

            // 第五步：验证数据源是否有效
            DataSourceConVO dataSourceConVO = dataSourceConManageImpl.getAllDataSource()
                    .stream().filter(t -> t.getId() == apiInfo.datasourceId).findFirst().orElse(null);
            if (dataSourceConVO == null) {
                resultEnum = ResultEnum.DS_APISERVICE_DATASOURCE_EXISTS;
                return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_DATASOURCE_EXISTS, responseVO);
            }

            String sql = apiInfo.createSql;
            // 第六步：查询参数信息，如果参数设置为内置参数，则以内置参数为准，反之则以传递的参数为准，如果没设置内置参数&参数列表中未传递，默认为空//则读取后台配置的参数值
            List<ParmConfigPO> parmList = apiParmMapper.getListByApiId(Math.toIntExact(apiInfo.id));
            if (CollectionUtils.isNotEmpty(parmList)) {
                if (!CollectionUtils.isNotEmpty(dto.parmList)) {
                    dto.parmList = new HashMap<>();
                }
                parmList.forEach(e -> {
                    Optional<Map.Entry<String, Object>> entryStream = dto.parmList.entrySet().stream().filter(item -> item.getKey().equals(e.getParmName())).findFirst();
                    if (entryStream.isPresent()) {
                        Map.Entry<String, Object> stringObjectEntry = entryStream.get();
                        if (stringObjectEntry != null)
                            e.setParmValue(String.valueOf(stringObjectEntry.getValue()));
                    } else {
                        e.setParmValue(null);
                    }
                });

                List<Long> collect = parmList.stream().map(ParmConfigPO::getId).collect(Collectors.toList());
                List<BuiltinParmPO> builtinParmList = apiBuiltinParmMapper.getListBy(Math.toIntExact(appInfo.id), Math.toIntExact(apiInfo.id), collect);
                if (CollectionUtils.isNotEmpty(builtinParmList)) {
                    parmList.forEach(e -> {
                        Optional<BuiltinParmPO> builtinParmOptional = builtinParmList.stream().filter(item -> item.getParmId() == e.id).findFirst();
                        if (builtinParmOptional.isPresent()) {
                            BuiltinParmPO builtinParmPO = builtinParmOptional.get();
                            if (builtinParmPO != null)
                                e.setParmValue(builtinParmPO.parmValue);
                        }
                    });
                }
            }

            // 第七步：拼接过滤条件
            List<FilterConditionConfigPO> filterConditionConfigPOList = apiFilterConditionMapper.getListByApiId(Math.toIntExact(apiInfo.id));
            if (apiInfo.apiType == ApiTypeEnum.SQL.getValue()) {
                sql = String.format("SELECT %s FROM %s WHERE 1=1 ", sql, apiInfo.getTableName());
                if (CollectionUtils.isNotEmpty(filterConditionConfigPOList)) {
                    List<SqlWhereDto> sqlWhereDtos = ApiFilterConditionMap.INSTANCES.listPoToSqlWhereDto(filterConditionConfigPOList);
                    String s1 = SqlParmUtils.SqlWhere(sqlWhereDtos);
                    if (s1 != null && s1.length() > 0)
                        sql += s1;
                }
            }

            // 第八步：替换SQL中的参数
            List<SqlParmDto> sqlParmDtos = ApiParmMap.INSTANCES.listPoToSqlParmDto(parmList);
            String s = SqlParmUtils.SqlParm(sqlParmDtos, sql, "@");
            if (s != null && s.length() > 0)
                sql = s;

            // 第九步：判断数据源类型，加载数据库驱动，执行查询SQL
            Statement st = null;
            Connection conn = null;
            DataSourceTypeEnum typeEnum = DataSourceTypeEnum.MYSQL;
            if (dataSourceConVO.getConType() == DataSourceTypeEnum.MYSQL) {
                conn = getStatement(DataSourceTypeEnum.MYSQL.getDriverName(), dataSourceConVO.conStr, dataSourceConVO.conAccount, dataSourceConVO.conPassword);
            } else if (dataSourceConVO.getConType() == DataSourceTypeEnum.SQLSERVER) {
                conn = getStatement(DataSourceTypeEnum.SQLSERVER.getDriverName(), dataSourceConVO.conStr, dataSourceConVO.conAccount, dataSourceConVO.conPassword);
                typeEnum = DataSourceTypeEnum.SQLSERVER;
            } else if (dataSourceConVO.getConType() == DataSourceTypeEnum.POSTGRESQL) {
                conn = getStatement(DataSourceTypeEnum.POSTGRESQL.getDriverName(), dataSourceConVO.conStr, dataSourceConVO.conAccount, dataSourceConVO.conPassword);
                typeEnum = DataSourceTypeEnum.POSTGRESQL;
            }
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);
            JSONArray array = new JSONArray();
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
                array.add(jsonObj);
            }
            rs.close();
            List<Object> collect = array;
            if (dto.current != null && dto.size != null) {
                int rowsCount = array.stream().toArray().length;
                responseVO.current = dto.current;
                responseVO.size = dto.size;
                responseVO.total = rowsCount;
                responseVO.page = (int) Math.ceil(1.0 * rowsCount / dto.size);
                dto.current = dto.current - 1;
                collect = array.stream().skip((dto.current - 1 + 1) * dto.size).limit(dto.size).collect(Collectors.toList());
            }
            responseVO.dataArray = collect;
            logPO.setLogResponseInfo(String.valueOf(collect.stream().count()));
            logPO.setBusinessState("成功");
        } catch (Exception e) {
            logPO.setLogLevel(LogLevelTypeEnum.ERROR.getName());
            logPO.setLogInfo(e.getMessage());
            resultEnum = ResultEnum.DS_APISERVICE_QUERY_ERROR;
            throw new FkException(ResultEnum.DS_APISERVICE_QUERY_ERROR, e.getMessage());
        } finally {
            if (resultEnum != ResultEnum.REQUEST_SUCCESS) {
                if (resultEnum == ResultEnum.DS_APISERVICE_QUERY_ERROR) {
                    logPO.setLogInfo(ResultEnum.DS_APISERVICE_QUERY_ERROR.getMsg() + "。错误信息：" + logPO.logInfo);
                } else {
                    logPO.setLogInfo(resultEnum.getMsg());
                }
            }
            logPO.setCreateUser("system");
            logsManageImpl.saveLog(logPO);
        }
        return ResultEntityBuild.buildData(resultEnum, responseVO);
    }

    /**
     * 连接数据库
     *
     * @param driver   driver
     * @param url      url
     * @param username username
     * @param password password
     * @return statement
     */
    private Connection getStatement(String driver, String url, String username, String password) {
        Connection conn;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new FkException(ResultEnum.DS_API_PV_QUERY_ERROR);
        }
        return conn;
    }
}
