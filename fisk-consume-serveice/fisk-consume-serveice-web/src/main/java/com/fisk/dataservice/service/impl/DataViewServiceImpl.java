package com.fisk.dataservice.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbMetaData.utils.MysqlConUtils;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.dataservice.dto.dataanalysisview.DataViewDTO;
import com.fisk.dataservice.dto.dataanalysisview.DataSourceViewDTO;
import com.fisk.dataservice.entity.DataViewPO;
import com.fisk.dataservice.map.DataViewMap;
import com.fisk.dataservice.mapper.DataViewMapper;
import com.fisk.dataservice.mapper.DataViewThemeMapper;
import com.fisk.dataservice.service.IDataViewService;
import com.fisk.dataservice.util.DbConnectionHelper;
import com.fisk.dataservice.util.SqlServerPlusUtils;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Service
@Slf4j
public class DataViewServiceImpl
        extends ServiceImpl<DataViewMapper, DataViewPO>
        implements IDataViewService {

    @Resource
    private DataViewMapper baseMapper;

    @Resource
    private DataViewThemeMapper dataViewThemeMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public PageDTO<DataViewDTO> getViewList(Integer viewThemeId, Integer pageNum, Integer pageSize) {
        Page<DataViewPO> poPage = new Page<>(pageNum, pageSize);


        // 查询数据
        QueryWrapper<DataViewPO> qw = new QueryWrapper<>();
        qw.eq("view_theme_id", viewThemeId).eq("del_flag", DelFlagEnum.NORMAL_FLAG.getValue()).orderByDesc("create_time");
        baseMapper.selectPage(poPage, qw);

        List<DataViewPO> records = poPage.getRecords();
        PageDTO<DataViewDTO> pageDto = new PageDTO<>();
        pageDto.setTotal(poPage.getTotal());
        pageDto.setTotalPage(poPage.getPages());
        log.info(JSON.toJSONString(records));
        pageDto.setItems(DataViewMap.INSTANCES.dataViewPoToDto(records));
        return pageDto;
    }

    @Override
    public DataSourceViewDTO getDataSourceMeta(Integer viewThemeId) {
        // 查询targetDbId
        Integer targetDbId = dataViewThemeMapper.selectDbId(viewThemeId);
        if (Objects.isNull(targetDbId)){
            throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
        }
        ResultEntity<List<DataSourceDTO>> result;
        try{
            result = userClient.getAllFiDataDataSource();
            if (result.getCode() != ResultEnum.SUCCESS.getCode()){
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
        }catch (Exception e){
            log.error("数据分析视图调用userClient失败", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }

        List<DataSourceDTO> dsList = result.getData();
        DataSourceDTO dataSourceDTO = dsList.stream().filter(item -> item.id.equals(targetDbId)).findFirst().orElse(null);
        log.info("数据视图主题目标数据源，[{}]", JSON.toJSONString(dataSourceDTO));

        if (Objects.isNull(dataSourceDTO)){
            throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
        }

        // 查询缓存里有没有redis的数据 TODO 该处测试语句需要删除
        redisUtil.del(RedisKeyBuild.buildViewThemeDataSourceKey(viewThemeId));
        boolean flag = redisUtil.hasKey(RedisKeyBuild.buildViewThemeDataSourceKey(viewThemeId));
        DataSourceViewDTO dataSourceViewDTO = null;
        if (!flag) {
            //将表和视图的结构存入redis
            setDataSourceMeta(viewThemeId, dataSourceDTO);
        }
        try{
            String datasourceMetaJson = redisUtil.get(RedisKeyBuild.buildViewThemeDataSourceKey(viewThemeId)).toString();
            if (!StringUtils.isEmpty(datasourceMetaJson)){
                dataSourceViewDTO = JSON.parseObject(datasourceMetaJson, DataSourceViewDTO.class);
            }
        }catch (Exception e){
            log.error("数据视图主题获取redis中数据表结构失败,", e);
        }
        return dataSourceViewDTO;
    }

    private void setDataSourceMeta(Integer viewThemeId, DataSourceDTO dsDTO){
        log.info("数据视图主题开始设置数据表信息");
        try {
            DataSourceViewDTO dto = new DataSourceViewDTO();

            if (dsDTO == null) {
                log.error(viewThemeId + ":" + JSON.toJSONString(ResultEnum.DATASOURCE_INFORMATION_ISNULL));
                return;
            }
            dto.appName = dsDTO.conDbname;
            dto.driveType = dsDTO.conType.toString();
            if (DataSourceTypeEnum.MYSQL.getName().equalsIgnoreCase(dsDTO.conType.toString())) {
//                MysqlConUtils mysqlConUtils = new MysqlConUtils();
//                // 表结构
//                Connection connection = DbConnectionHelper.connection(dsDTO.conStr, dsDTO.conAccount,
//                        dsDTO.conPassword, DataSourceTypeEnum.MYSQL);
//                dto.tableDtoList = mysqlConUtils.getViewTableNameAndColumns(dsDTO.conStr, dsDTO.conAccount,
//                        dsDTO.conPassword, "mysql");
                //视图结构
//                dataSource.viewDtoList = mysqlConUtils.loadViewDetails(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL));
            }else if (DataSourceTypeEnum.SQLSERVER.getName().equalsIgnoreCase(dsDTO.conType.toString())) {
                SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                // 表结构
                dto.tableDtoList = sqlServerPlusUtils.getTableNameAndColumnsPlus(DbConnectionHelper.connection(dsDTO.conStr, dsDTO.conAccount,
                        dsDTO.conPassword, DataSourceTypeEnum.SQLSERVER), dsDTO.conDbname);
                // 视图结构
//                dataSource.viewDtoList = sqlServerPlusUtils.loadViewDetails(DbConnectionHelper.connection(po.connectStr, po.connectAccount,
//                        po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER));
            }
            /*
            else if (DataSourceTypeEnum.ORACLE.getName().equalsIgnoreCase(dataSource.driveType)) {
                dataSource.appName = po.serviceName;
                OracleUtils oracleUtils = new OracleUtils();
                // 表结构
                dataSource.tableDtoList = oracleUtils.getTableNameList(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE), po.dbName);
                //视图结构
                ////dataSource.viewDtoList = oracleUtils.loadViewDetails(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE), po.connectAccount);
            }

            else if (DataSourceTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(dataSource.driveType)) {
                PgsqlUtils pgsqlUtils = new PgsqlUtils();
                // 表结构
                dataSource.tableDtoList = pgsqlUtils.getTableNameAndColumnsPlus(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.POSTGRESQL));
                //视图结构
                dataSource.viewDtoList = new ArrayList<>();

            } else if (DataSourceTypeEnum.ORACLE_CDC.getName().equalsIgnoreCase(dataSource.driveType)) {
                OracleUtils oracleUtils = new OracleUtils();
                // 表结构
                dataSource.tableDtoList = oracleUtils.getTableNameList(DbConnectionHelper.connection(po.connectStr, po.connectAccount, po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.ORACLE), po.dbName);
            }
             */

            if (CollectionUtils.isNotEmpty(dto.tableDtoList)) {
                redisUtil.set(RedisKeyBuild.buildViewThemeDataSourceKey(viewThemeId), JSON.toJSONString(dto));
                log.info("数据视图主题设置数据结束，[{}]", JSON.toJSONString(dto));
            }
        } catch (Exception e) {
            log.error(viewThemeId + ":" + JSON.toJSONString(ResultEnum.DATASOURCE_INFORMATION_ISNULL));
        }
    }
}
