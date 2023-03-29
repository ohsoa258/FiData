package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.datamanagement.dto.datalogging.DataLoggingDTO;
import com.fisk.datamanagement.enums.DataLoggingEnum;
import com.fisk.datamanagement.service.DataLoggingService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-28 16:06
 * @description
 */
@Service
@Slf4j
public class DataLoggingServiceImpl implements DataLoggingService {
    @Resource
    RedisUtil redisUtil;

    @Resource
    UserClient userClient;

    @Override
    public DataLoggingDTO getDataTableRows() {
        DataLoggingDTO dataLoggingDTO = new DataLoggingDTO();
        if(redisUtil.hasKey(DataLoggingEnum.TOTAL_NUMBER_OF_RECORDS.getName())){
            //拿到最新数据用来计算日增
            Integer todayTotal=getDataODSTableRows();
            //日增
            dataLoggingDTO.setDailyGain(todayTotal-(int)redisUtil.get(DataLoggingEnum.TOTAL_NUMBER_OF_RECORDS.getName()));
            //总记录数
            dataLoggingDTO.setTotalNumberOfRecords(todayTotal);
            return dataLoggingDTO;
        }
        //获取ODS所有表的总记录数
        dataLoggingDTO.setTotalNumberOfRecords(getDataODSTableRows());
        redisUtil.set(DataLoggingEnum.TOTAL_NUMBER_OF_RECORDS.getName(),dataLoggingDTO.getTotalNumberOfRecords());
        return dataLoggingDTO;
    }

    //每天凌晨12点执行一次
    @Scheduled(cron = "0 0 12 * * ?")
    public void SetTotalNumberOfRecords(){
        redisUtil.set(DataLoggingEnum.TOTAL_NUMBER_OF_RECORDS.getName(),getDataTableRows());
    }

    //在SQLserver中获取ods库的总行数
    public Integer getDataODSTableRows() {
        Connection conn = null;
        Statement st = null;
        Integer rowCount = 0;
        try {
            //获取账号密码
            ResultEntity<List<DataSourceDTO>> allFiDataDataSource = userClient.getAllFiDataDataSource();
            log.debug("获取账号密码 END");
            if (allFiDataDataSource.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }
            log.debug("get datasource START");
            Optional<DataSourceDTO> first = allFiDataDataSource.data
                    .stream()
                    .filter(e -> ResultEnum.TYPE_OF_DATABASE_DMP_ODS.getMsg().equals(e.conDbname))
                    .findFirst();
            log.debug("get datasource END"+ JSON.toJSONString(first));
            if (!first.isPresent()) {
                throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
            }
            log.debug("数据源信息constr{"+first.get().conStr+"}conip{"+first.get().conIp+"},conport{"+first.get().conPort+"},ConPWD{"+first.get().getConPassword()+"},conAccount{"+first.get().conAccount+"}");
            //连接数据源
            log.debug("========连接数据源START========");
            conn = getConnection(first.get());

            conn.setAutoCommit(false);
            log.debug("con commit");
            log.debug("========连接数据源END========");

            //获取总条数
            log.debug("=====获取总条数START======");
            String getTotalSql="SELECT SUM(row_count) as totalRows FROM sys.dm_db_partition_stats where 1=1 AND object_id IN (SELECT object_id FROM sys.tables)";
            log.debug("=====获取总条数SQL语句======"+getTotalSql);
            log.debug("==conn.createStatement() START==");
            st = conn.createStatement();
            log.debug("==conn.createStatement() END==");
            ResultSet rSet = st.executeQuery(getTotalSql);
            log.debug("=====获取总条数END===");
            if (rSet.next()) {
                rowCount = rSet.getInt("totalRows");
            }
            rSet.close();
            log.debug("close connection success");
        } catch (Exception e) {
            log.debug("数据资产,查询表数据失败:"+e.getMessage());
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR_INVALID, e);
        } finally {
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        return rowCount;
    }

    private Connection getConnection(DataSourceDTO dto) {
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        return commonDbHelper.connection(dto.conStr, dto.conAccount, dto.conPassword, dto.conType);
    }
}
