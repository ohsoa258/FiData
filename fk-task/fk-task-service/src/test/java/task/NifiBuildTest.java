package task;

import com.alibaba.fastjson.JSON;
import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.fisk.common.constants.NifiConstants;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.enums.task.nifi.StatementSqlTypeEnum;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.service.IAtlasBuild;
import com.fisk.task.vo.ProcessGroupsVO;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.service.INifiFlowBuild;
import com.fisk.task.utils.NifiHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class NifiBuildTest {

    private final String groupPid = "017a11b8-82a2-134f-ee9c-3b4c1425b0b3";
    private final String dbConId = "017a11b9-82a2-134f-2f7e-12b3e8ef41d2";

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
        ControllerServiceEntity res = service.getDbControllerService(groupPid);
        System.out.println(JSON.toJSONString(res));
    }

    @Test
    public void enableConnectionPool() {
        BusinessResult<ControllerServiceEntity> res = service.updateDbControllerServiceState(groupPid);
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
    public void buildSourceToTargetDataFlowTest() {
        for (int i = 0; i < 3; i++) {
            flowBuild.buildSourceToTargetDataFlow();
        }
    }

    @Resource
    RestTemplate httpClient;

    @Test
    public void enabledProcessor() {
        String id = "017a118e-82a2-134f-08ab-1422202dd7b2";
        try {
            ProcessorEntity res = NifiHelper.getProcessorsApi().getProcessor(id);
            if (res.getComponent().getState() == ProcessorDTO.StateEnum.RUNNING) {
                System.out.println("");
            }
            ProcessorRunStatusEntity dto = new ProcessorRunStatusEntity();
            dto.state = "RUNNING";
            dto.disconnectedNodeAcknowledged = true;
            dto.revision = res.getRevision();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<ProcessorRunStatusEntity> request = new HttpEntity<>(dto, headers);

            String url = NifiConstants.ApiConstants.BASE_PATH + "/processors/" + id + "/run-status";
            ResponseEntity<String> response = httpClient.exchange(url, HttpMethod.PUT, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                ProcessorEntity resEntity = JSON.parseObject(response.getBody(), ProcessorEntity.class);
                System.out.println(JSON.toJSONString(resEntity));
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAllProcessor() {
        String id = "017a11f5-82a2-134f-121c-fcc1fdeba097";
        try {
            String url = NifiConstants.ApiConstants.BASE_PATH + "/process-groups/" + id + "/process-groups";
            ResponseEntity<ProcessGroupsVO> res = httpClient.exchange(url, HttpMethod.GET, null, ProcessGroupsVO.class);
            if (res.getStatusCode() == HttpStatus.OK) {
                System.out.println(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAllProcessor1() {
        String id = "";
        try {
            System.out.println(service.getGroupCount(id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
