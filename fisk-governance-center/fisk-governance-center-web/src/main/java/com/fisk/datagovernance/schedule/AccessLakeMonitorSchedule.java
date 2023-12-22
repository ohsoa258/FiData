package com.fisk.datagovernance.schedule;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.facebook.presto.jdbc.internal.guava.collect.Lists;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.vo.CDCAppNameAndTableVO;
import com.fisk.dataaccess.vo.TableDbNameAndNameVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: wangjian
 * @Date: 2023-12-21
 * @Description:
 */
@Component
@Slf4j
public class AccessLakeMonitorSchedule {
    
    private static DataAccessClient dataAccessClient;
    private static RedisUtil redisUtil;
    @Value("${presto.url}")
    private String prestoUrl;
    @Value("${presto.port}")
    private String prestoPort;

    @Autowired
    public void setDataAccessClient(DataAccessClient dataAccessClient) {
        AccessLakeMonitorSchedule.dataAccessClient = dataAccessClient;
    }
    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        AccessLakeMonitorSchedule.redisUtil = redisUtil;
    }
    @Scheduled(cron = "0 0 0/1 * * ? ") // cron表达式：每天凌晨 0点 执行
    public void doTask(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = simpleDateFormat.format(new Date());
        long startTime = System.currentTimeMillis();
        log.info("定时执行监控每个应用入湖kafka数据量:开始--"+start);
        ResultEntity<List<CDCAppNameAndTableVO>> cdcAppNameAndTables = dataAccessClient.getCDCAppNameAndTables(null);
        List<CDCAppNameAndTableVO> data = new ArrayList<>();
        if (cdcAppNameAndTables.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(cdcAppNameAndTables.getData())) {
            data = cdcAppNameAndTables.getData();
        } else {
            log.error("dataAccessClient无法查询到目标库的连接信息");
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        Map<String,List<String>> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(data)){
            List<String> selectSql = new ArrayList<>();
            for (CDCAppNameAndTableVO app : data) {
                List<TableDbNameAndNameVO> tableDbNameAndNameVO = app.getTableDbNameAndNameVO();
                if (CollectionUtils.isNotEmpty(tableDbNameAndNameVO)){
                    if (tableDbNameAndNameVO.size()>100){
                        List<List<TableDbNameAndNameVO>> partition = Lists.partition(tableDbNameAndNameVO, 100);
                        for (List<TableDbNameAndNameVO> tableDbNameAndNameVOS : partition) {
                            String sql = tableDbNameAndNameVOS.stream().map(i -> {
                                String str = "select '" + i.getDbName() + "' as dbName,'" + i.getTableName() + "' as tableName,count(1) as rowCount from " + i.getDbName() + "." + i.getTableName();
                                return str;
                            }).collect(Collectors.joining(" UNION ALL "));
                            selectSql.add(sql);
                        }

                    }else {
                        String sql = app.getTableDbNameAndNameVO().stream().map(i -> {
                            String str = "select '" + i.getDbName() + "' as dbName,'" + i.getTableName() + "' as tableName,count(1) as rowCount from " + i.getDbName() + "." + i.getTableName();
                            return str;
                        }).collect(Collectors.joining(" UNION ALL "));
                        selectSql.add(sql);
                    }
                }

                map.put(app.getId().toString(),selectSql);
            }
        }
        for (Map.Entry<String, List<String>> mapEntity : map.entrySet()) {
            List<String> selectSql = mapEntity.getValue();
            Integer rowTotal = 0;
            for (String sql : selectSql) {
                Integer rows = selectCount(sql);
                rowTotal+=rows;
            }
            redisUtil.set(RedisKeyEnum.MONITOR_ACCESSLAKE.getName()+":"+mapEntity.getKey(),rowTotal);
        }
        String end = simpleDateFormat.format(new Date());
        // 记录结束时间
        long endTime = System.currentTimeMillis();
        // 计算接口耗时，单位为毫秒
        long elapsedTime = endTime - startTime;

        log.info("定时执行监控每个应用入湖kafka数据量:结束--"+end+"--接口耗时：" + elapsedTime + " 毫秒");
    }

    private Integer selectCount( String selectSql){
        log.info("查询presto连接Kafka的执行sql:"+selectSql);
        String url = "jdbc:presto://"+prestoUrl+":"+prestoPort+"/kafka";
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DriverManager.getConnection(url, "root", null);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(selectSql);
            Integer rowTotal =  0;
            while (resultSet.next()) {
                // 处理查询结果
                Integer rowCount = Integer.valueOf(resultSet.getString("rowCount"));
                rowTotal+= rowCount;
            }
            return rowTotal;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        } finally {
            try {
                statement.close();
                connection.close();
                resultSet.close();
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new FkException(ResultEnum.ERROR);
            }
        }
    }
}
