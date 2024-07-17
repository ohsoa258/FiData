package com.fisk.datamanagement.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.datagovernance.client.DataGovernanceClient;
import com.fisk.datamanagement.dto.datalogging.DataLoggingDTO;
import com.fisk.datamanagement.dto.datalogging.DataTotalDTO;
import com.fisk.datamanagement.dto.datalogging.PipelTotalDTO;
import com.fisk.datamanagement.dto.datalogging.PipelWeekDTO;
import com.fisk.datamanagement.enums.DataLoggingEnum;
import com.fisk.datamanagement.mapper.BusinessTargetinfoMapper;
import com.fisk.datamanagement.mapper.GlossaryMapper;
import com.fisk.datamanagement.mapper.MetadataEntityMapper;
import com.fisk.datamanagement.mapper.StandardsMapper;
import com.fisk.datamanagement.service.DataLoggingService;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.mdm.client.MdmClient;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.dispatchlog.PipelTaskLogVO;
import com.fisk.task.enums.DispatchLogEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Resource
    DataModelClient dataModelClient;

    @Resource
    MdmClient mdmClient;

    @Resource
    DataGovernanceClient dataGovernanceClient;

    @Resource
    MetadataEntityMapper metadataEntityMapper;

    @Resource
    GlossaryMapper glossaryMapper;

    @Resource
    StandardsMapper standardsMapper;

    @Resource
    BusinessTargetinfoMapper businessTargetinfoMapper;

    @Resource
    PublishTaskClient taskClient;
//    @Override
//    public DataLoggingDTO getDataTableRows() {
//        DataLoggingDTO dataLoggingDTO = new DataLoggingDTO();
//        if(redisUtil.hasKey(DataLoggingEnum.TOTAL_NUMBER_OF_RECORDS.getName())){
//            //拿到最新数据用来计算日增
//            Integer todayTotal=getDataODSTableRows();
//            //日增
//            dataLoggingDTO.setDailyGain(todayTotal-(int)redisUtil.get(DataLoggingEnum.TOTAL_NUMBER_OF_RECORDS.getName()));
//            //总记录数
//            dataLoggingDTO.setTotalNumberOfRecords(todayTotal);
//            return dataLoggingDTO;
//        }
//        //获取ODS所有表的总记录数
//        dataLoggingDTO.setTotalNumberOfRecords(getDataODSTableRows());
//        redisUtil.set(DataLoggingEnum.TOTAL_NUMBER_OF_RECORDS.getName(),dataLoggingDTO.getTotalNumberOfRecords());
//        return dataLoggingDTO;
//    }
@Override
public DataLoggingDTO getDataTableRows() {
    DataLoggingDTO dataLoggingDTO = new DataLoggingDTO();
//    if(redisUtil.hasKey(DataLoggingEnum.TOTAL_NUMBER_OF_RECORDS.getName())){
//        //拿到最新数据用来计算日增
//        Integer todayTotal=getDataODSTableRows();
//        //日增
//        dataLoggingDTO.setDailyGain(todayTotal-(int)redisUtil.get(DataLoggingEnum.TOTAL_NUMBER_OF_RECORDS.getName()));
//        //总记录数
//        dataLoggingDTO.setTotalNumberOfRecords(todayTotal);
//        return dataLoggingDTO;
//    }
//    //获取ODS所有表的总记录数
//    dataLoggingDTO.setTotalNumberOfRecords(getDataODSTableRows());
//    redisUtil.set(DataLoggingEnum.TOTAL_NUMBER_OF_RECORDS.getName(),dataLoggingDTO.getTotalNumberOfRecords());

    Integer totalNum = metadataEntityMapper.getTotalNum();
    dataLoggingDTO.setTotalNumberOfRecords(totalNum);
    return dataLoggingDTO;
}

    /**
     * 获取数据总量的DTO（数据传输对象）。
     * 该方法用于聚合各种数据的总量，并返回一个包含这些总量的DTO对象。
     * @return DataTotalDTO 包含各种数据总量的DTO。
     */
    @Override
    public DataTotalDTO getDataTotals() {
        // 初始化数据总量DTO对象
        DataTotalDTO dataTotalDTO = new DataTotalDTO();

        // 获取业务总量
        ResultEntity<Object> businessTotal = dataModelClient.getBusinessTotal();
        // 如果获取成功，则设置业务总量到DTO
        if (businessTotal.code == ResultEnum.SUCCESS.getCode()){
            dataTotalDTO.setBusinessTotal((int)businessTotal.data);
        }

        // 获取业务表总量
        ResultEntity<Object> businessTableTotal = dataModelClient.getBusinessTableTotal();
        // 如果获取成功，则设置业务表总量到DTO
        if (businessTableTotal.code == ResultEnum.SUCCESS.getCode()){
            dataTotalDTO.setBusinessTableTotal((int)businessTableTotal.data);
        }

        // 获取MDM模型总量
        ResultEntity<Object> mdmModelTotal = mdmClient.getModelTotal();
        // 如果获取成功，则设置MDM模型总量到DTO
        if (mdmModelTotal.code == ResultEnum.SUCCESS.getCode()){
            dataTotalDTO.setMdmModelTotal((int)mdmModelTotal.data);
        }

        // 获取MDM实体总量
        ResultEntity<Object> mdmEntityTotal = mdmClient.getEntityTotal();
        // 如果获取成功，则设置MDM实体总量到DTO
        if (mdmEntityTotal.code == ResultEnum.SUCCESS.getCode()){
            dataTotalDTO.setMdmEntityTotal((int)mdmEntityTotal.data);
        }

        // 获取数据治理角色总量
        ResultEntity<Object> dataCheckRoleTotal = dataGovernanceClient.getDataCheckRoleTotal();
        // 如果获取成功，则设置数据治理角色总量到DTO
        if (dataCheckRoleTotal.code == ResultEnum.SUCCESS.getCode()){
            dataTotalDTO.setDataCheckTotal((int)dataCheckRoleTotal.data);
        }

        // 获取标准总量
        Integer standardsTotal = standardsMapper.getStandardTotal();
        dataTotalDTO.setStandardsTotal(standardsTotal);

        // 获取业务目标总量
        Integer businesstargetinfoTotal = businessTargetinfoMapper.getBusinessTargetinfoTotal();
        dataTotalDTO.setBusinesstargetinfoTotal(businesstargetinfoTotal);

        // 获取词汇表总量
        Integer glossaryTotal = glossaryMapper.getGlossaryTotal();
        dataTotalDTO.setGlossaryTotal(glossaryTotal);

        // 返回包含所有数据总量的DTO
        return dataTotalDTO;
    }

    @Override
    public PipelTotalDTO getPipelTotals() {
        PipelTotalDTO pipelTotalDTO;
        ResultEntity<Object> pipelTotals = taskClient.getPipelTotals();
        if (pipelTotals.code == ResultEnum.SUCCESS.getCode() && pipelTotals.data != null) {
            String json = JSONObject.toJSONString(pipelTotals.data);
            pipelTotalDTO = JSONObject.parseObject(json,PipelTotalDTO.class);
        } else {
            log.error("远程调用失败，方法名：【data-service:sendEmail】");
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        return pipelTotalDTO;
    }

    @Override
    public List<PipelWeekDTO> getPipelWeek() {
        List<PipelWeekDTO> pipelWeekDTO;
        ResultEntity<Object> pipelWeek = taskClient.getPipelWeek();
        if (pipelWeek.code == ResultEnum.SUCCESS.getCode() && pipelWeek.data != null) {
            String json = JSONObject.toJSONString(pipelWeek.data);
            pipelWeekDTO = JSONObject.parseArray(json,PipelWeekDTO.class);
        } else {
            log.error("远程调用失败，方法名：【data-service:sendEmail】");
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        return pipelWeekDTO;
    }

    //每天凌晨12点执行一次
    @Scheduled(cron = "0 0 12 * * ?")
    public void SetTotalNumberOfRecords(){
        redisUtil.set(DataLoggingEnum.TOTAL_NUMBER_OF_RECORDS.getName(),getDataODSTableRows());
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
