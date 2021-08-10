package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.exception.FkException;
import com.fisk.common.redis.RedisKeyBuild;
import com.fisk.common.redis.RedisKeyEnum;
import com.fisk.common.redis.RedisUtil;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserInfo;
import com.fisk.dataservice.dto.ApiConfigureDTO;
import com.fisk.dataservice.dto.ConfigureUserDTO;
import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import com.fisk.dataservice.entity.ApiConfigurePO;
import com.fisk.dataservice.entity.ConfigureUserPO;
import com.fisk.dataservice.entity.MiddleConfigurePO;
import com.fisk.dataservice.map.ApiConfigureFieldMap;
import com.fisk.dataservice.map.ApiConfigureMap;
import com.fisk.dataservice.mapper.ApiConfigureFieldMapper;
import com.fisk.dataservice.mapper.ApiConfigureMapper;
import com.fisk.dataservice.mapper.ConfigureUserMapper;
import com.fisk.dataservice.mapper.MiddleConfigureMapper;
import com.fisk.dataservice.service.ApiFieldService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fisk.dataservice.enums.ConfigureFieldTypeEnum.*;
import static com.fisk.dataservice.utils.TokenUtils.analysisToken;
import static com.fisk.dataservice.utils.TokenUtils.createJwt;
import static java.util.stream.Collectors.joining;

/**
 * @author WangYan
 * @date 2021/7/8 9:58
 */
@Service
public class ApiFieldServiceImpl implements ApiFieldService {

    @Resource
    private ApiConfigureFieldMapper configureFieldMapper;

    @Resource
    private ApiConfigureMapper configureMapper;

    @Resource
    private ConfigureUserMapper userMapper;

    @Resource
    private MiddleConfigureMapper middleMapper;

    @Resource
    private RedisUtil redis;

    @Override
    public List<Map> queryField(String apiRoute, Integer currentPage, Integer pageSize, ConfigureUserDTO user) {
        // 校验用户信息
        Long userId = this.checkingToken(user);

        QueryWrapper<ApiConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ApiConfigurePO::getApiRoute, apiRoute);
        ApiConfigurePO apiConfigure = configureMapper.selectOne(queryWrapper);
        if (apiConfigure == null) {
            throw new FkException(ResultEnum.NOTFOUND);
        }

        // 验证用户权限能否访问该服务
        boolean checkingConfigure = checkingConfigure(userId, apiConfigure.getId());
        if (checkingConfigure == false){
            throw new FkException(ResultEnum.UNAUTHORIZED);
        }

        QueryWrapper<ApiConfigureFieldPO> query = new QueryWrapper<>();
        query.lambda().eq(ApiConfigureFieldPO::getConfigureId, apiConfigure.getId());
        List<ApiConfigureFieldPO> apiConfigureFieldList = configureFieldMapper.selectList(query);
        return this.filterData(apiConfigureFieldList, apiConfigure.getTableName(), currentPage, pageSize);
    }

    @Override
    public Page<ApiConfigureDTO> queryAll(Page<ApiConfigurePO> page,String apiName) {
        if (StringUtils.isNotBlank(apiName)){
            QueryWrapper<ApiConfigurePO> query = new QueryWrapper<>();
            query.lambda()
                    .like(ApiConfigurePO::getApiName,apiName);
            return ApiConfigureMap.INSTANCES.poToDtoPage(configureMapper.selectPage(page, query));
        }

        return  ApiConfigureMap.INSTANCES.poToDtoPage(configureMapper.selectPage(page, null));
    }

    @Override
    public ResultEnum updateApiConfigure(ApiConfigureDTO dto) {
        ApiConfigurePO apiConfigure = configureMapper.selectById(dto.id);
        if (apiConfigure == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        ApiConfigurePO apiConfigurePO = ApiConfigureMap.INSTANCES.dtoToPo(dto);
        return configureMapper.updateById(apiConfigurePO)> 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteApiById(Integer id) {
        ApiConfigurePO apiConfigure = configureMapper.selectById(id);
        if (apiConfigure == null){
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 删除ApiConfigure表中的数据
        if (configureMapper.deleteById(id) <= 0){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 删除ApiConfigureField表中的数据
        QueryWrapper<ApiConfigureFieldPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(ApiConfigureFieldPO::getConfigureId, id)
                .select(ApiConfigureFieldPO::getId);
        List<ApiConfigureFieldPO> selectList = configureFieldMapper.selectList(query);
        if (CollectionUtils.isEmpty(selectList)){
            return ResultEnum.DATA_NOTEXISTS;
        }

        List<Long> ids = new ArrayList<>();
        for (ApiConfigureFieldPO apiConfigureField : selectList) {
            ids.add( apiConfigureField.getId());
        }
        return configureFieldMapper.deleteBatchIds(ids) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ApiConfigurePO getById(Integer id) {
        if (id == null){
            throw new FkException(ResultEnum.PARAMTER_NOTNULL);
        }

        ApiConfigurePO apiConfigure = configureMapper.selectById(id);
        if (apiConfigure == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }else {
            return apiConfigure;
        }
    }


    /**
     * 验证用户信息token
     * @param user 用户信息
     * @return
     */
    public Long checkingToken(ConfigureUserDTO user){
        if (user.getId() == null){
            throw new FkException(ResultEnum.PARAMTER_ERROR);
        }

        if (StringUtils.isNotBlank(user.getUserName())
                && StringUtils.isNotBlank(user.getPassword())){
            // 没有token,校验用户名密码
            QueryWrapper<ConfigureUserPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(ConfigureUserPO::getUserName, user.getUserName())
                    .eq(ConfigureUserPO::getPassword, user.getPassword());
            ConfigureUserPO selectOne = userMapper.selectOne(queryWrapper);
            if (selectOne == null) {
                throw new FkException(ResultEnum.USER_ACCOUNTPASSWORD_ERROR);
            }

            ConfigureUserPO configureUser = ApiConfigureFieldMap.INSTANCES.dtoToPo(user);
            String token = createJwt(configureUser);
            UserInfo userInfo = UserInfo.of(configureUser.getId(), configureUser.getUserName(), token);
            boolean res = redis.set(RedisKeyBuild.buildLoginUserInfo(userInfo.getId()), userInfo, RedisKeyEnum.AUTH_USERINFO.getValue());
            return user.getId();
        }else {
            // token存在,直接根据id从redis当中获取数据
            UserInfo userInfo = (UserInfo) redis.get(RedisKeyBuild.buildLoginUserInfo(user.getId()));
            if (userInfo == null){
                throw new FkException(ResultEnum.USER_ACCOUNTPASSWORD_ERROR);
            }

            ConfigureUserPO configureUser = analysisToken(userInfo.token);
            return configureUser.getId();
        }
    }

    /**
     * 验证用户权限能否访问该服务
     * @param userId 用户ID
     * @param configureId 访问服务的ID
     * @return
     */
    public boolean checkingConfigure(Long userId,Long configureId){
        QueryWrapper<MiddleConfigurePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(MiddleConfigurePO::getUserId, userId.intValue())
                .eq(MiddleConfigurePO::getConfigureId, configureId.intValue());
        MiddleConfigurePO configure = middleMapper.selectOne(queryWrapper);
        return configure != null ? true : false;
    }

    /**
     * 数据分组拼接
     * @param apiConfigureFieldList
     * @param tableName
     * @param currentPage
     * @param pageSize
     * @return
     */
    public List<Map> filterData(List<ApiConfigureFieldPO> apiConfigureFieldList,String tableName, Integer currentPage, Integer pageSize){
        String queryFieldList = apiConfigureFieldList.stream()
                .filter(e -> e.getFieldType().equals(QUERY))
                .map(e -> "`" +e.getField() +"`")
                .collect(joining(","));

        String groupingList = apiConfigureFieldList.stream()
                .filter(e -> e.getFieldType().equals(GROUPING))
                .map(e -> "`"+e.getField() +"`")
                .collect(joining(","));

        String aggregationList = apiConfigureFieldList.stream()
                .filter(e -> e.getFieldType().equals(AGGREGATION))
                .map(e -> e.getFieldConditionValue() + "("+ "`" + e.getField()+ "`"+ ")")
                .collect(joining(","));

        String conditionList = apiConfigureFieldList.stream()
                .filter(e -> e.getFieldType().equals(RESTRICT))
                .map(e -> "`" +e.getField() +"`" + e.getFieldConditionValue() + "'" + e.getFieldValue() + "'")
                .collect(joining("AND "));
        return this.splicingSql(queryFieldList,aggregationList,groupingList,conditionList,currentPage,pageSize, tableName);
    }


    /**
     * 拼接sql
     * @param aggregationList 聚合
     * @param groupingList    分组
     * @param conditionList   条件
     * @param currentPage    分页
     * @param pageSize
     * @param tableName  表名
     * @return
     */
    public List<Map> splicingSql(String queryFieldList,
                                 String aggregationList,
                                 String groupingList,
                                 String conditionList,
                                 Integer currentPage, Integer pageSize,
                                 String tableName){
        // sql
        String splitSql = this.splitSql(queryFieldList, aggregationList, groupingList, conditionList, tableName);

        // 添加分页
        if (currentPage == null){
            currentPage = 1;
        }
        if (pageSize == null){
            pageSize = 50;
        }
        List<Map> objects = configureMapper.queryData(splitSql, (currentPage-1)*pageSize, pageSize);
        return objects;
    }

    /**
     * 拼接sql主方法
     * @param queryFieldList   查询
     * @param aggregationList  聚合
     * @param groupingList     分组
     * @param conditionList    条件
     * @param tableName        表名
     * @return
     */
    public String splitSql(String queryFieldList, String aggregationList, String groupingList, String conditionList, String tableName){
        StringBuilder str = new StringBuilder();
        str.append("SELECT ");
        // select
        this.queryField(str,queryFieldList,aggregationList,groupingList);

        // from
        str.append(" FROM ").append("`" + tableName + "`").append(" ");
        // where
        if (StringUtils.isNotBlank(conditionList)){
            str.append("WHERE 1 = 1 AND " + conditionList);
        }

        // order by
        if (StringUtils.isNotBlank(queryFieldList)){
            str.append(" ORDER BY " + queryFieldList.substring(0,queryFieldList.indexOf(",")) +" DESC ");
        }

        // group
        if (StringUtils.isNotBlank(groupingList)){
            str.append("GROUP BY ");
            str.append(groupingList);
        }

        // groupBy
        this.aggregation(str,queryFieldList,aggregationList,groupingList);
        this.noAggregation(str,queryFieldList,aggregationList,groupingList);
        return str.toString();
    }

    /**
     * select
     * @param str  字符串拼接
     * @param queryFieldList   查询
     * @param aggregationList  聚合
     * @param groupingList     分组
     * @return
     */
    public void queryField(StringBuilder str,String queryFieldList, String aggregationList, String groupingList){
        if (StringUtils.isNotBlank(queryFieldList)){
            str.append(queryFieldList);
        }
        if (StringUtils.isNotBlank(queryFieldList) && StringUtils.isNotBlank(groupingList)){
            str.append(",");
        }
        if (StringUtils.isNotBlank(groupingList)){
            str.append(groupingList);
        }
        if (StringUtils.isNotBlank(groupingList) && StringUtils.isNotBlank(aggregationList)){
            str.append(",");
        }
        if (StringUtils.isNotBlank(aggregationList)){
            str.append(aggregationList);
        }
    }

    /**
     * 如果查询条件中有聚合，那么查询的字段必须分组
     * @param str  字符串拼接
     * @param queryFieldList   查询
     * @param aggregationList  聚合
     * @param groupingList     分组
     * @return
     */
    public void aggregation(StringBuilder str,String queryFieldList, String aggregationList, String groupingList){
        if (StringUtils.isNotBlank(aggregationList)){
            if (StringUtils.isNotBlank(groupingList) && StringUtils.isNotBlank(queryFieldList)){
                str.append(",");
            }

            if (StringUtils.isBlank(groupingList)){
                str.append("GROUP BY ");
            }

            if (StringUtils.isNotBlank(queryFieldList)){
                str.append(queryFieldList);
            }
        }
    }

    /**
     * 如果没有聚合字段,但是有分组的字段，那么查询的字段必须分组。
     * @param str  字符串拼接
     * @param queryFieldList   查询
     * @param aggregationList  聚合
     * @param groupingList     分组
     * @return
     */
    public void noAggregation(StringBuilder str,String queryFieldList, String aggregationList, String groupingList){
        if (StringUtils.isBlank(aggregationList) && StringUtils.isNotBlank(groupingList)){
            if (StringUtils.isNotBlank(groupingList) && StringUtils.isNotBlank(queryFieldList)){
                str.append(",");
            }

            if (StringUtils.isBlank(groupingList)){
                str.append("GROUP BY ");
            }

            if (StringUtils.isNotBlank(queryFieldList)){
                str.append(queryFieldList);
            }
        }
    }
}
