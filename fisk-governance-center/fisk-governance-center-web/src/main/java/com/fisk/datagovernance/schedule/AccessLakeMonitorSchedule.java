package com.fisk.datagovernance.schedule;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.vo.CDCAppDbNameVO;
import com.fisk.datagovernance.util.KafkaTopicUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

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

    private static String monitorUrl;

    private static String monitorPort;

    private static String conStr;
    private static String conAccount;
    private static String conPassword;

    private static String driver;

    @Autowired
    public void setDataAccessClient(DataAccessClient dataAccessClient) {
        AccessLakeMonitorSchedule.dataAccessClient = dataAccessClient;
    }
    @Autowired
    public void setRedisUtil(RedisUtil redisUtil) {
        AccessLakeMonitorSchedule.redisUtil = redisUtil;
    }
    @Value("${kafka-monitor.url}")
    public void setMonitorUrl(String monitorUrl){
        AccessLakeMonitorSchedule.monitorUrl = monitorUrl;
    }
    @Value("${kafka-monitor.port}")
    public void setMonitorPort(String monitorPort){
        AccessLakeMonitorSchedule.monitorPort = monitorPort;
    }
    @Value("${tableMappingTopicDatabase.conStr}")
    public void setConStr(String conStr){
        AccessLakeMonitorSchedule.conStr = conStr;
    }
    @Value("${tableMappingTopicDatabase.conAccount}")
    public void setConAccount(String conAccount){
        AccessLakeMonitorSchedule.conAccount = conAccount;
    }
    @Value("${tableMappingTopicDatabase.conPassword}")
    public void setConPassword(String conPassword){
        AccessLakeMonitorSchedule.conPassword = conPassword;
    }
    @Value("${tableMappingTopicDatabase.driver}")
    public void setDriver(String driver){
        AccessLakeMonitorSchedule.driver = driver;
    }

    @Scheduled(cron = "0 0 0 * * ? ") // cron表达式：每天凌晨 0点 执行
    public void doTask(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = simpleDateFormat.format(new Date());
        long startTime = System.currentTimeMillis();
        log.info("定时执行监控每个应用入湖kafka数据量:开始--"+start);
        ResultEntity<List<CDCAppDbNameVO>> cdcAppDbNames = dataAccessClient.getCDCAppDbName();
        List<CDCAppDbNameVO> data = new ArrayList<>();
        if (cdcAppDbNames.code == ResultEnum.SUCCESS.getCode() && CollectionUtils.isNotEmpty(cdcAppDbNames.getData())) {
            data = cdcAppDbNames.getData();
        } else {
            log.error("dataAccessClient无法查询到目标库的连接信息");
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        Map<String,String> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(data)){
            for (CDCAppDbNameVO app : data) {
                if (app != null){
                    String selectTopic = "select distinct sink_topic as topic from table_topic_mapping where db_name = '" + app.getDbName() + "';";
                    map.put(app.getId().toString(),selectTopic);
                }
            }
        }
        for (Map.Entry<String, String> mapEntity : map.entrySet()) {
            String selectTopic = mapEntity.getValue();
            List<String> topics = selectTopic(selectTopic);
            Integer rowTotal = 0;
            if (CollectionUtils.isNotEmpty(topics)){
                rowTotal = selectCount(topics);
            }
            redisUtil.set(RedisKeyEnum.MONITOR_ACCESSLAKE_KAFKA.getName()+":"+mapEntity.getKey(),rowTotal);
        }
        String end = simpleDateFormat.format(new Date());
        // 记录结束时间
        long endTime = System.currentTimeMillis();
        // 计算接口耗时，单位为毫秒
        long elapsedTime = endTime - startTime;

        log.info("定时执行监控每个应用入湖kafka数据量:结束--"+end+"--接口耗时：" + elapsedTime + " 毫秒");
    }

    private List<String> selectTopic( String selectTopic){
        List<String> topics = new ArrayList<>();
        Connection conn = null;
        Statement st = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(conStr, conAccount, conPassword);
            st = conn.createStatement();
            //无需判断ddl语句执行结果,因为如果执行失败会进catch
            log.info("开始执行脚本获取topic:{}", selectTopic);
            ResultSet rs = st.executeQuery(selectTopic);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    //获取sql查询数据集合
                    Object value = rs.getObject(columnName);
                    topics.add(value.toString());
                }
            }
            return topics;
        }catch (Exception e) {
            log.error("脚本执行失败"+e.getMessage());
            return null;
        }finally {
            try {
                assert st != null;
                st.close();
                conn.close();
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new FkException(ResultEnum.ERROR);
            }
        }
    }

    private Integer selectCount(List<String> topics){
        Integer rows = 0;
        // Kafka broker 地址
        String brokerList = monitorUrl+":"+monitorPort;
        for (String topic : topics) {
            Integer row = (int)KafkaTopicUtils.totalMessageCount(brokerList, topic);
            rows+=row;
        }
        return rows;
    }
}
