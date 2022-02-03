package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.utils.Dto.SqlParmDto;
import com.fisk.common.utils.Dto.SqlWhereDto;
import com.fisk.common.utils.SqlParmUtils;
import com.fisk.dataservice.dto.apiservice.RequestDataDTO;
import com.fisk.dataservice.dto.apiservice.RequstDTO;
import com.fisk.dataservice.dto.apiservice.TokenDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.ApiStateTypeEnum;
import com.fisk.dataservice.enums.ApiTypeEnum;
import com.fisk.dataservice.enums.DataSourceTypeEnum;
import com.fisk.dataservice.map.ApiFilterConditionMap;
import com.fisk.dataservice.map.ApiParmMap;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IApiServiceManageService;

import com.fisk.dataservice.vo.api.FieldConfigVO;
import com.fisk.dataservice.vo.apiservice.ResponseVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.sql.*;
import java.util.ArrayList;
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
    private ApiFilterConditionMapper apiFilterConditionMapper;

    @Override
    public ResultEntity<Object> getToken(TokenDTO dto) {
        return null;
    }

    @Override
    public ResultEntity<ResponseVO> getData(RequstDTO dto) {
        ResponseVO responseVO = new ResponseVO();
        String appAccount = "";

        // 第一步：验证当前请求是否合法，解析token
        String token = "";
        if (token == null || token.isEmpty())
            return ResultEntityBuild.buildData(ResultEnum.AUTH_TOKEN_IS_NOTNULL, responseVO);
        appAccount = "DDS_lijiawen";
        if (appAccount == null || appAccount.isEmpty())
            return ResultEntityBuild.buildData(ResultEnum.AUTH_JWT_ERROR, responseVO);

        // 第二步：验证当前应用（下游系统）是否有效
        AppConfigPO appInfo = appRegisterMapper.getByAppAccount(appAccount);
        if (appInfo == null)
            return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_APP_EXISTS, responseVO);

        // 第三步：验证当前请求的API是否有效
        ApiConfigPO apiInfo = apiRegisterMapper.getByApiCode(dto.apiCode);
        if (apiInfo == null)
            return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_API_EXISTS, responseVO);

        // 第四步：验证当前请求的API是否具备访问权限
        AppApiPO subscribeBy = appApiMapper.getSubscribeBy(Math.toIntExact(appInfo.id), Math.toIntExact(apiInfo.id));
        if (subscribeBy == null)
            return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_APP_NOTSUB, responseVO);
        if (subscribeBy.apiState == ApiStateTypeEnum.Disable.getValue())
            return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_APP_NOTENABLE, responseVO);

        // 第五步：验证数据源是否有效
        DataSourceConPO dataSourceConPO = dataSourceConMapper.selectById(apiInfo.datasourceId);
        if (dataSourceConPO == null)
            return ResultEntityBuild.buildData(ResultEnum.DS_APISERVICE_DATASOURCE_EXISTS, responseVO);

        String sql = apiInfo.createSql;
        // 第六步：查询参数信息，如果参数设置为内置参数，则以内置参数为准，反之则以传递的参数为准，如果没设置内置参数&参数列表中未传递，则读取后台配置的参数值
        List<ParmConfigPO> parmList = apiParmMapper.getListByApiId(Math.toIntExact(apiInfo.id));
        if (CollectionUtils.isNotEmpty(parmList)) {
            if (CollectionUtils.isNotEmpty(dto.parmList)) {
                parmList.forEach(e -> {
                    Optional<Map.Entry<String, Object>> entryStream = dto.parmList.entrySet().stream().filter(item -> item.getKey().equals(e.getParmName())).findFirst();
                    if (entryStream.isPresent()) {
                        Map.Entry<String, Object> stringObjectEntry = entryStream.get();
                        if (stringObjectEntry != null)
                            e.setParmValue(String.valueOf(stringObjectEntry.getValue()));
                    }
                });
            }
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
        if (apiInfo.apiType == ApiTypeEnum.SQL.getValue()
                && CollectionUtils.isNotEmpty(filterConditionConfigPOList)) {
            sql = String.format("SELECT %s FROM %s WHERE 1=1 ", sql, apiInfo.getTableName());
            List<SqlWhereDto> sqlWhereDtos = ApiFilterConditionMap.INSTANCES.listPoToSqlWhereDto(filterConditionConfigPOList);
            String s1 = SqlParmUtils.SqlWhere(sqlWhereDtos);
            if (s1 != null && s1.length() > 0)
                sql += s1;
        }

        // 第八步：替换SQL中的参数
        List<SqlParmDto> sqlParmDtos = ApiParmMap.INSTANCES.listPoToSqlParmDto(parmList);
        String s = SqlParmUtils.SqlParm(sqlParmDtos, sql, "@");
        if (s != null && s.length() > 0)
            sql = s;

        // 第九步：判断数据源类型，加载数据库驱动，执行查询SQL
        try {
            Statement st = null;
            Connection conn = null;
            if (dataSourceConPO.getConType() == DataSourceTypeEnum.MYSQL.getValue()) {
                conn = getStatement(DataSourceTypeEnum.MYSQL.getDriverName(), dataSourceConPO.conStr, dataSourceConPO.conAccount, dataSourceConPO.conPassword);
            } else if (dataSourceConPO.getConType() == DataSourceTypeEnum.SQLSERVER.getValue()) {
                conn = getStatement(DataSourceTypeEnum.SQLSERVER.getDriverName(), dataSourceConPO.conStr, dataSourceConPO.conAccount, dataSourceConPO.conPassword);
            }
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            assert st != null;
            ResultSet rs = st.executeQuery(sql);

            JSONArray array = new JSONArray();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<FieldConfigVO> fieldConfigVOS = new ArrayList<>();
            while (rs.next()) {
                JSONObject jsonObj = new JSONObject();
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    //获取sql查询数据集合
                    String value = rs.getString(columnName);
                    jsonObj.put(columnName, value);
                }
                array.add(jsonObj);
            }
            rs.close();
            responseVO.dataArray = array;
        } catch (Exception e) {
            throw new FkException(ResultEnum.DS_APISERVICE_QUERY_ERROR);
        }
        return ResultEntityBuild.buildData(ResultEnum.SUCCESS, responseVO);
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
