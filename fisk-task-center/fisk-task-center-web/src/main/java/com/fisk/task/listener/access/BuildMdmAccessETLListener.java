package com.fisk.task.listener.access;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.entity.TBETLIncrementalPO;
import com.fisk.task.entity.TaskDwDimPO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import com.fisk.task.listener.postgre.datainput.impl.BuildFactoryHelper;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: wangjian
 * @Date: 2023-04-24
 * @Description:
 */
@Component
@Slf4j
public class BuildMdmAccessETLListener {

    @Resource
    public UserClient userClient;
    @Value("${fiData-data-ods-source}")
    public String dataSourceDwId;

    public ResultEnum msg(String dataInfo, Acknowledgment acke) {
        ResultEnum result = ResultEnum.SUCCESS;
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        int id = 0;
        int tableType = 0;
        log.info("dw创建表参数:" + dataInfo);
        //生成nifi流程
        try {
            ModelPublishDataDTO inpData = JSON.parseObject(dataInfo, ModelPublishDataDTO.class);
            List<ModelPublishTableDTO> dimensionList = inpData.dimensionList;
            ResultEntity<DataSourceDTO> DataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
            DataSourceTypeEnum conType = null;
            if (DataSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO dataSource = DataSource.data;
                conType = dataSource.conType;
            } else {
                log.error("userclient无法查询到ods库的连接信息");
                throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
            }
            for (ModelPublishTableDTO modelPublishTableDTO : dimensionList) {
                id = Math.toIntExact(modelPublishTableDTO.tableId);
                tableType = modelPublishTableDTO.createType;
                //生成版本号
                //获取时间戳版本号
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                Calendar calendar = Calendar.getInstance();
                String version = df.format(calendar.getTime());

//                ResultEnum resultEnum = taskPgTableStructureHelper.saveTableStructure(modelPublishTableDTO, version, conType);
//                if (resultEnum.getCode() != ResultEnum.TASK_TABLE_NOT_EXIST.getCode() && resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
//                    taskPgTableStructureMapper.updatevalidVersion(version);
//                    throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
//                }
//                log.info("执行存储过程返回结果" + resultEnum.getCode());
//                //生成建表语句
//                List<String> pgdbTable2 = new ArrayList<>();
//                ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
//                if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
//                    DataSourceDTO dataSource = fiDataDataSource.data;
//                    IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dataSource.conType);
//                    pgdbTable2 = dbCommand.buildDwStgAndOdsTable(modelPublishTableDTO);
//                    HashMap<String, Object> map = new HashMap<>();
//                    map.put("table_name", modelPublishTableDTO.tableName);
//                    taskDwDimMapper.deleteByMap(map);
//                    TaskDwDimPO taskDwDimPO = new TaskDwDimPO();
//                    //taskDwDimPO.areaBusinessName=businessAreaName;//业务域名
//                    taskDwDimPO.sqlContent = pgdbTable2.get(1);//创建表的sql
//                    taskDwDimPO.tableName = modelPublishTableDTO.tableName;
//                    taskDwDimPO.storedProcedureName = "update" + modelPublishTableDTO.tableName + "()";
//                    taskDwDimMapper.insert(taskDwDimPO);
//                    log.info("建模创表语句:" + JSON.toJSONString(pgdbTable2));
//                } else {
//                    log.error("userclient无法查询到dw库的连接信息");
//                    throw new FkException(ResultEnum.ERROR);
//                }
//                BusinessResult businessResult = iPostgreBuild.postgreBuildTable(pgdbTable2.get(0), BusinessTypeEnum.DATAMODEL);
//                if (!businessResult.success) {
//                    throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
//                }
//                if (resultEnum.getCode() == ResultEnum.TASK_TABLE_NOT_EXIST.getCode()) {
//                    BusinessResult businessResult1 = iPostgreBuild.postgreBuildTable(pgdbTable2.get(1), BusinessTypeEnum.DATAMODEL);
//                    if (!businessResult1.success) {
//                        throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
//                    }
//                }
//                //生成函数,并执行
//            /*String storedProcedure3 = createStoredProcedure3(modelPublishTableDTO);
//            log.info("dw库生成函数:"+storedProcedure3);
//            iPostgreBuild.postgreBuildTable(storedProcedure3, BusinessTypeEnum.DATAMODEL);*/
//                log.info("开始执行nifi创建数据同步");
//                TBETLIncrementalPO ETLIncremental = new TBETLIncrementalPO();
//                ETLIncremental.objectName = modelPublishTableDTO.tableName;
//                ETLIncremental.enableFlag = "1";
//                ETLIncremental.incrementalObjectivescoreBatchno = UUID.randomUUID().toString();
//                Map<String, Object> conditionHashMap = new HashMap<>();
//                conditionHashMap.put("object_name", ETLIncremental.objectName);
//                List<TBETLIncrementalPO> tbetlIncrementalPos = incrementalMapper.selectByMap(conditionHashMap);
//                if (tbetlIncrementalPos != null && tbetlIncrementalPos.size() > 0) {
//                    log.info("此表已有同步记录,无需重复添加");
//                } else {
//                    incrementalMapper.insert(ETLIncremental);
//                }
//                BuildNifiFlowDTO bfd = new BuildNifiFlowDTO();
//                bfd.userId = inpData.userId;
//                bfd.appId = inpData.businessAreaId;
//                bfd.synchronousTypeEnum = SynchronousTypeEnum.PGTOPG;
//                //来源为数据接入
//                bfd.dataClassifyEnum = DataClassifyEnum.DATAMODELING;
//                bfd.id = modelPublishTableDTO.tableId;
//                bfd.tableName = modelPublishTableDTO.tableName;
//                bfd.selectSql = modelPublishTableDTO.sqlScript;
//                //同步方式
//                bfd.synMode = modelPublishTableDTO.synMode;
//                bfd.queryStartTime = modelPublishTableDTO.queryStartTime;
//                bfd.queryEndTime = modelPublishTableDTO.queryEndTime;
//                bfd.appName = inpData.businessAreaName;
//                bfd.openTransmission = inpData.openTransmission;
//                bfd.updateSql = modelPublishTableDTO.factUpdateSql;
//                bfd.dataSourceDbId = modelPublishTableDTO.dataSourceDbId;
//                bfd.targetDbId = modelPublishTableDTO.targetDbId;
//                bfd.prefixTempName = modelPublishTableDTO.prefixTempName;
//                bfd.customScriptBefore = modelPublishTableDTO.customScript;
//                bfd.customScriptAfter = modelPublishTableDTO.customScriptAfter;
//                // 设置预览SQL执行语句
//                bfd.syncStgToOdsSql = modelPublishTableDTO.coverScript;
//                bfd.buildTableSql = pgdbTable2.get(0);
//                if (modelPublishTableDTO.createType == 0) {
//                    //类型为物理表
//                    bfd.type = OlapTableEnum.DIMENSION;
//                } else {
//                    //类型为物理表
//                    bfd.type = OlapTableEnum.FACT;
//                }
//                bfd.maxRowsPerFlowFile = modelPublishTableDTO.maxRowsPerFlowFile;
//                bfd.fetchSize = modelPublishTableDTO.fetchSize;
//                bfd.traceId = inpData.traceId;
//                log.info("nifi传入参数：" + JSON.toJSONString(bfd));
//                TableNifiSettingPO one = tableNifiSettingService.query().eq("app_id", bfd.appId).eq("table_access_id", bfd.id).eq("type", bfd.type.getValue()).one();
//                TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
//                if (one != null) {
//                    tableNifiSettingPO = one;
//                }
//                tableNifiSettingPO.appId = Math.toIntExact(bfd.appId);
//                tableNifiSettingPO.tableName = bfd.tableName;
//                tableNifiSettingPO.tableAccessId = Math.toIntExact(bfd.id);
//                tableNifiSettingPO.selectSql = bfd.selectSql;
//                tableNifiSettingPO.type = bfd.type.getValue();
//                tableNifiSettingPO.syncMode = 1;
//                tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
//                pc.publishBuildNifiFlowTask(bfd);
//                log.info("执行完成");
//                //0维度表
//                if (modelPublishTableDTO.createType == 0) {
//                    modelPublishStatusDTO.status = 1;
//                    modelPublishStatusDTO.id = Math.toIntExact(modelPublishTableDTO.tableId);
//                    modelPublishStatusDTO.type = 0;
//                    dataModelClient.updateDimensionPublishStatus(modelPublishStatusDTO);
//                } else {
//                    modelPublishStatusDTO.status = 1;
//                    modelPublishStatusDTO.id = Math.toIntExact(modelPublishTableDTO.tableId);
//                    modelPublishStatusDTO.type = 0;
//                    dataModelClient.updateFactPublishStatus(modelPublishStatusDTO);
//                }
//
//            }
//            return result;
//        } catch (Exception e) {
//            log.error("dw发布失败,表id为" + id + StackTraceHelper.getStackTraceInfo(e));
//            result = ResultEnum.ERROR;
//            if (tableType == 0) {
//                modelPublishStatusDTO.status = 2;
//                modelPublishStatusDTO.id = Math.toIntExact(id);
//                modelPublishStatusDTO.type = 0;
//                dataModelClient.updateDimensionPublishStatus(modelPublishStatusDTO);
//            } else {
//                modelPublishStatusDTO.status = 2;
//                modelPublishStatusDTO.id = Math.toIntExact(id);
//                modelPublishStatusDTO.type = 0;
//                dataModelClient.updateFactPublishStatus(modelPublishStatusDTO);
            }
//            return result;
//        } finally {
//            acke.acknowledge();
//        }
//    }
            return null;
        }catch (Exception e){
            return null;
        }
    }
}