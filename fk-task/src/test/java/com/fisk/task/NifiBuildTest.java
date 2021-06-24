package com.fisk.task;

import com.alibaba.fastjson.JSON;
import com.davis.client.model.*;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.enums.task.nifi.StatementSqlTypeEnum;
import com.fisk.task.entity.dto.nifi.*;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.service.INifiFlowBuild;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class NifiBuildTest {

    private final String groupPid = "017a1105-82a2-134f-66e1-bb374545c2c1";
    private final String dbConId = "017a10c0-82a2-134f-e980-dfd547c8f73e";

    @Resource
    INifiComponentsBuild service;

    @Test
    public void buildGroup() {
        PositionDTO position = new PositionDTO();
        position.setX(300.00);
        position.setY(300.00);
        BuildProcessGroupDTO dto = new BuildProcessGroupDTO() {{
            pid = groupPid;
            name = "test";
            details = "";
            positionDTO = position;
        }};
        BusinessResult<ProcessGroupEntity> res = service.buildProcessGroup(dto);
        System.out.println(res);
    }

    @Test
    public void getGroup() {
        BusinessResult<ProcessGroupEntity> res = service.getProcessGroupById("017a10ae-82a2-134f-e9d1-3e45c0e5249b");
        System.out.println(JSON.toJSONString(res));
    }

    @Test
    public void buildConnectionPool() {
        BuildDbControllerServiceDTO dto = new BuildDbControllerServiceDTO() {{
            groupId = groupPid;
            name = "source-mysql-db-pool";
            details = "Source Mysql DB Pool";
            conUrl = "jdbc:mysql://192.168.11.130:3306/dmp_chartvisual_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
            driverName = "com.mysql.jdbc.Driver";
            driverLocation = "/opt/nifi/nifi-current/jdbcdriver/mysql-connector-java-8.0.25.jar";
            user = "root";
            pwd = "root123";
            enabled = false;
        }};
        BusinessResult<ControllerServiceEntity> res = service.buildDbControllerService(dto);
        System.out.println(JSON.toJSONString(res));
    }

    @Test
    public void buildConnectionPoolAndStart() {
        BuildDbControllerServiceDTO dto = new BuildDbControllerServiceDTO() {{
            groupId = groupPid;
            name = "source-mysql-db-pool";
            details = "Source Mysql DB Pool";
            conUrl = "jdbc:mysql://192.168.11.130:3306/dmp_chartvisual_db?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false";
            driverName = "com.mysql.jdbc.Driver";
            driverLocation = "/opt/nifi/nifi-current/jdbcdriver/mysql-connector-java-8.0.25.jar";
            user = "root";
            pwd = "root123";
            enabled = true;
        }};
        BusinessResult<ControllerServiceEntity> res = service.buildDbControllerService(dto);
        System.out.println(JSON.toJSONString(res));
    }

    @Test
    public void getConnectionPool() {
        ControllerServiceEntity res = service.getDbControllerService("017a1111-82a2-134f-1998-a9e4aa19b5cf");
        System.out.println(JSON.toJSONString(res));
    }

    @Test
    public void enableConnectionPool() {
        BusinessResult<ControllerServiceEntity> res = service.updateDbControllerServiceState("017a10c0-82a2-134f-e980-dfd547c8f73e");
        System.out.println(JSON.toJSONString(res));
    }

    @Test
    public void buildExecSqlProcessor() {
        BuildExecuteSqlProcessorDTO dto = new BuildExecuteSqlProcessorDTO() {{
            groupId = groupPid;
            dbConnectionId = dbConId;
            name = "exec sql";
            details = "exec sql processor";
            scheduleType = SchedulingStrategyTypeEnum.CRON;
            scheduleExpression = "0 0/1 * * * ? ";
        }};
        BusinessResult<ProcessorEntity> res = service.buildExecuteSqlProcess(dto);
        System.out.println(JSON.toJSONString(res));
    }

    @Test
    public void buildConvertToJsonProcessor() {
        BuildConvertToJsonProcessorDTO dto = new BuildConvertToJsonProcessorDTO() {{
            groupId = groupPid;
            name = "convert to json";
            details = "data convert to json processor";
        }};
        BusinessResult<ProcessorEntity> res = service.buildConvertToJsonProcess(dto);
        System.out.println(JSON.toJSONString(res));
    }

    @Test
    public void buildConvertJsonToSqlProcessor() {
        BuildConvertJsonToSqlProcessorDTO dto = new BuildConvertJsonToSqlProcessorDTO() {{
            groupId = groupPid;
            name = "convert json to sql";
            details = "json convert to sql processor";
            sqlType = StatementSqlTypeEnum.INSERT;
            tableName = "tb_test_data3";
            dbConnectionId = dbConId;
        }};
        BusinessResult<ProcessorEntity> res = service.buildConvertJsonToSqlProcess(dto);
        System.out.println(JSON.toJSONString(res));
    }

    @Test
    public void buildPutSqlProcessor() {
        BuildPutSqlProcessorDTO dto = new BuildPutSqlProcessorDTO() {{
            groupId = groupPid;
            name = "convert json to sql";
            details = "json convert to sql processor";
            dbConnectionId = dbConId;
        }};
        BusinessResult<ProcessorEntity> res = service.buildPutSqlProcess(dto);
        System.out.println(JSON.toJSONString(res));
    }

    @Test
    public void buildConnectProcessor() {
        BusinessResult<ConnectionEntity> res = service.buildConnectProcessors(groupPid,
                "017a10ee-82a2-134f-0845-ee2869a2b9c4",
                "017a10f3-82a2-134f-4c98-df5b4ce256c9",
                AutoEndBranchTypeEnum.SQL);
        System.out.println(JSON.toJSONString(res));
    }

    //--------------------

    @Resource
    INifiFlowBuild flowBuild;

    @Test
    public void buildSourceToTargetDataFlowTest(){
        flowBuild.buildSourceToTargetDataFlow();
    }
}
