package com.fisk.task.service.impl;

import com.davis.client.model.ConnectionEntity;
import com.davis.client.model.ProcessGroupEntity;
import com.davis.client.model.ProcessorEntity;
import com.fisk.common.entity.BusinessResult;
import com.fisk.common.enums.task.nifi.AutoEndBranchTypeEnum;
import com.fisk.common.enums.task.nifi.SchedulingStrategyTypeEnum;
import com.fisk.common.enums.task.nifi.StatementSqlTypeEnum;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.service.INifiComponentsBuild;
import com.fisk.task.service.INifiFlowBuild;
import com.fisk.task.utils.NifiPositionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gy
 */
@Service
@Slf4j
public class NifiFlowBuildImpl implements INifiFlowBuild {

    @Resource
    INifiComponentsBuild componentsBuild;

    @Override
    public BusinessResult<Object> buildSourceToTargetDataFlow() {
        String pid = "017a11f5-82a2-134f-121c-fcc1fdeba097";
        String dbConPoolId = "017a11f6-82a2-134f-34db-a77db9ed67d1";

        int xlevel = componentsBuild.getGroupCount(pid);

        //创建子组
        BuildProcessGroupDTO group2DTO = new BuildProcessGroupDTO();
        group2DTO.name = "子流程1";
        group2DTO.details = "java程序通过调用RESTAPI创建Nifi流程";
        group2DTO.groupId = pid;
        group2DTO.positionDTO = NifiPositionHelper.buildXPositionDTO(xlevel);
        BusinessResult<ProcessGroupEntity> groupRes = componentsBuild.buildProcessGroup(group2DTO);
        String groupId = groupRes.data.getId();

        //创建 执行sql 组件
        BuildExecuteSqlProcessorDTO execSqlDTO = new BuildExecuteSqlProcessorDTO();
        execSqlDTO.groupId = groupId;
        execSqlDTO.dbConnectionId = dbConPoolId;
        execSqlDTO.name = "Exec Query";
        execSqlDTO.details = "在源执行sql";
        execSqlDTO.scheduleType = SchedulingStrategyTypeEnum.CRON;
        execSqlDTO.scheduleExpression = "0/30 * * * * ? ";
        execSqlDTO.querySql = "select id as source_id,name,type,sub_type,value from tb_test_data";
        execSqlDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(0);
        BusinessResult<ProcessorEntity> execSqlRes = componentsBuild.buildExecuteSqlProcess(execSqlDTO, new ArrayList<String>());

        //创建 数据转json 组件
        BuildConvertToJsonProcessorDTO toJsonDTO = new BuildConvertToJsonProcessorDTO();
        toJsonDTO.groupId = groupId;
        toJsonDTO.name = "Convert Data To Json";
        toJsonDTO.details = "源数据转换为json";
        toJsonDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(1);
        BusinessResult<ProcessorEntity> toJsonRes = componentsBuild.buildConvertToJsonProcess(toJsonDTO);

        //创建 连接器 连接 执行sql 和 数据转json
        BusinessResult<ConnectionEntity> conRes = componentsBuild.buildConnectProcessors(groupId, execSqlRes.data.getId(), toJsonRes.data.getId(), AutoEndBranchTypeEnum.SUCCESS);

        //创建 json转sql 组件
        BuildConvertJsonToSqlProcessorDTO toSqlDTO = new BuildConvertJsonToSqlProcessorDTO();
        toSqlDTO.groupId = groupId;
        toSqlDTO.dbConnectionId = dbConPoolId;
        toSqlDTO.tableName = "tb_test_data3";
        toSqlDTO.sqlType = StatementSqlTypeEnum.INSERT;
        toSqlDTO.name = "Convert Json To Sql";
        toSqlDTO.details = "json转化为sql语句";
        toSqlDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(2);
        BusinessResult<ProcessorEntity> toSqlRes = componentsBuild.buildConvertJsonToSqlProcess(toSqlDTO);

        //连接 数据转json 和 json转sql
        BusinessResult<ConnectionEntity> jsonSqlCon = componentsBuild.buildConnectProcessors(groupId, toJsonRes.data.getId(), toSqlRes.data.getId(), AutoEndBranchTypeEnum.SUCCESS);

        BuildPutSqlProcessorDTO putSqlDTO = new BuildPutSqlProcessorDTO();
        putSqlDTO.groupId = groupId;
        putSqlDTO.dbConnectionId = dbConPoolId;
        putSqlDTO.name = "Put Sql To Target";
        putSqlDTO.details = "将生成的sql在目标系统执行";
        putSqlDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(3);
        BusinessResult<ProcessorEntity> putSqlRes = componentsBuild.buildPutSqlProcess(putSqlDTO);

        //连接 数据转json 和 json转sql
        BusinessResult<ConnectionEntity> sqlPutRes = componentsBuild.buildConnectProcessors(groupId, toSqlRes.data.getId(), putSqlRes.data.getId(), AutoEndBranchTypeEnum.SQL);

        List<ProcessorEntity> list = componentsBuild.enabledProcessor(groupId, execSqlRes.data, toJsonRes.data, toSqlRes.data, putSqlRes.data);

        return BusinessResult.of(true, "nifi流程创建成功", null);
    }
}
