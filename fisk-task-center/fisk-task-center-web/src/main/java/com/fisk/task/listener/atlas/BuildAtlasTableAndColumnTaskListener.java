package com.fisk.task.listener.atlas;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.enums.DeltaTimeParameterTypeEnum;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.task.entity.TBETLIncrementalPO;
import com.fisk.task.entity.TaskPgTableStructurePO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import com.fisk.task.po.app.TableNifiSettingPO;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.utils.StackTraceHelper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.TriggerUtils;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.cronutils.model.CronType.QUARTZ;

/**
 * @author: DennyHui
 * CreateTime: 2021/7/12 14:01
 * Description:
 */
@Component
@Slf4j
public class BuildAtlasTableAndColumnTaskListener
        extends ServiceImpl<TaskPgTableStructureMapper, TaskPgTableStructurePO> {

    @Resource
    DataAccessClient dc;
    @Resource
    PublishTaskController pc;
    @Resource
    private TBETLIncrementalMapper incrementalMapper;
    @Resource
    private TableNifiSettingServiceImpl tableNifiSettingService;

    public ResultEnum msg(String dataInfo, Acknowledgment acke) {
        log.info("进入Atlas生成表和字段");
        log.info("dataInfo:" + dataInfo);
        //无论是单条还是多条，加上这个都可以当作集合处理，达到不需要更改方法参数的目的
        dataInfo = "[" + dataInfo + "]";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            BuildPhysicalTableDTO buildPhysicalTableDTO = new BuildPhysicalTableDTO();
            //dataInfo  ->  buildPhysicalTableDTO
            //使用JSON.parseArray将 待发布的数据 转化为 list
            List<BuildPhysicalTableDTO> list = JSON.parseArray(dataInfo, BuildPhysicalTableDTO.class);

            //遍历BuildPhysicalTableDTO list
            for (BuildPhysicalTableDTO buildPhysicalTable : list) {
                buildPhysicalTableDTO = buildPhysicalTable;
                //获取物理表查询语句
                String physicalSelect = createPhysicalTable(buildPhysicalTableDTO);

                //endregion
                //新建ETL增量表：tb_etl_Incremental 的对象
                TBETLIncrementalPO ETLIncremental = new TBETLIncrementalPO();
                //是否使用简称
                if (buildPhysicalTableDTO.whetherSchema) {
                    ETLIncremental.objectName = buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName;
                } else {
                    ETLIncremental.objectName = buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName;
                }
                //获取接入的增量时间参数
                List<DeltaTimeDTO> deltaTimes = buildPhysicalTableDTO.deltaTimes;
                if (!CollectionUtils.isEmpty(deltaTimes)) {
                    for (DeltaTimeDTO dto : deltaTimes) {
                        if (Objects.equals(dto.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.CONSTANT) &&
                                Objects.equals(dto.systemVariableTypeEnum, SystemVariableTypeEnum.START_TIME)) {
                            ETLIncremental.incrementalObjectivescoreStart = sdf.parse(dto.variableValue);
                        }
                        if (Objects.equals(dto.deltaTimeParameterTypeEnum, DeltaTimeParameterTypeEnum.CONSTANT) &&
                                Objects.equals(dto.systemVariableTypeEnum, SystemVariableTypeEnum.END_TIME)) {
                            ETLIncremental.incrementalObjectivescoreEnd = sdf.parse(dto.variableValue);
                        }
                    }
                }

                ETLIncremental.enableFlag = "1";
                //生成数据同步批次号  uuid
                ETLIncremental.incrementalObjectivescoreBatchno = UUID.randomUUID().toString();
                Map<String, Object> conditionHashMap = new HashMap<>();
                //将数据同步流程的表名放入map集合
                conditionHashMap.put("object_name", ETLIncremental.objectName);
                //从数据库通过该表名查询，若有数据，则log.info:此表已有同步记录,无需重复添加
                List<TBETLIncrementalPO> tbetlIncrementalPos = incrementalMapper.selectByMap(conditionHashMap);
                if (tbetlIncrementalPos != null && tbetlIncrementalPos.size() > 0) {
                    log.info("此表已有同步记录,无需重复添加");
                } else {
                    incrementalMapper.insert(ETLIncremental);
                }
                //通过应用id,物理表id,表类型获取到一个TableNifiSettingPO对象   tb_table_nifi_setting
                TableNifiSettingPO one = tableNifiSettingService.query().eq("app_id", buildPhysicalTableDTO.appId).eq("table_access_id", buildPhysicalTableDTO.dbId).eq("type", OlapTableEnum.PHYSICS.getValue()).one();
                TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
                if (one != null) {
                    tableNifiSettingPO = one;
                }
                //设置应用id
                tableNifiSettingPO.appId = Integer.valueOf(buildPhysicalTableDTO.appId);
                //是否使用简称
                if (buildPhysicalTableDTO.whetherSchema) {
                    tableNifiSettingPO.tableName = buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName;
                } else {
                    tableNifiSettingPO.tableName = buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName;
                }
                //设置物理表id
                tableNifiSettingPO.tableAccessId = Integer.valueOf(buildPhysicalTableDTO.dbId);
                //设置物理表查询语句
                tableNifiSettingPO.selectSql = physicalSelect;
                //设置表类型
                tableNifiSettingPO.type = OlapTableEnum.PHYSICS.getValue();
                //设置同步方式
                tableNifiSettingPO.syncMode = buildPhysicalTableDTO.syncMode;
                //保存或更新tb_table_nifi_setting表的数据
                tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
                log.info("开始执行nifi创建数据同步");

                //nifi流程组件的属性设置开始.........
                BuildNifiFlowDTO bfd = new BuildNifiFlowDTO();
                bfd.userId = buildPhysicalTableDTO.userId;
                bfd.appId = Long.parseLong(buildPhysicalTableDTO.appId);
                bfd.id = Long.parseLong(buildPhysicalTableDTO.dbId);
                bfd.synchronousTypeEnum = SynchronousTypeEnum.TOPGODS;
                bfd.tableName = tableNifiSettingPO.tableName;
                //类型为物理表
                bfd.type = OlapTableEnum.PHYSICS;
                //来源为数据接入
                bfd.dataClassifyEnum = DataClassifyEnum.DATAACCESS;
                bfd.queryStartTime = buildPhysicalTableDTO.queryStartTime;
                bfd.queryEndTime = buildPhysicalTableDTO.queryEndTime;
                bfd.openTransmission = buildPhysicalTableDTO.openTransmission;
                bfd.excelFlow = buildPhysicalTableDTO.excelFlow;
                bfd.sftpFlow = buildPhysicalTableDTO.sftpFlow;
                bfd.deltaTimes = deltaTimes;
                bfd.generateVersionSql = buildPhysicalTableDTO.generateVersionSql;
                bfd.maxRowsPerFlowFile = buildPhysicalTableDTO.maxRowsPerFlowFile;
                bfd.fetchSize = buildPhysicalTableDTO.fetchSize;
                // 新属性赋值
                bfd.dataSourceDbId = buildPhysicalTableDTO.dataSourceDbId;
                bfd.targetDbId = buildPhysicalTableDTO.targetDbId;
                bfd.whereScript = buildPhysicalTableDTO.whereScript;
                bfd.buildTableSql = buildPhysicalTableDTO.buildTableSql;
                // stg抽取数据加载到ods的sql语句
                bfd.syncStgToOdsSql = buildPhysicalTableDTO.syncStgToOdsSql;
                //设置stg保存时间的sql
                bfd.deleteScript = buildPhysicalTableDTO.deleteStgScript;
                //发布历史id
                bfd.tableHistoryId = buildPhysicalTableDTO.tableHistoryId;
                log.info("nifi传入参数：" + JSON.toJSONString(bfd));
                //统一traceid,让流程串起来,原来traceid各自步骤的traceid是各自的,未了让流程能串起来,所以改成一样的,
                // 当然可以再加个父级traceid进去,各自步骤还是各自的,但这样就要加字段存,为了实现这么个功能改表不值得
                bfd.traceId = buildPhysicalTableDTO.traceId;
                if (bfd.openTransmission) {
                    bfd.popout = true;
                }
                pc.publishBuildNifiFlowTask(bfd);
                log.info("执行完成");
            }
            return ResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("系统异常" + StackTraceHelper.getStackTraceInfo(e));
            return ResultEnum.ERROR;
        }
    }

    private String createPhysicalTable(BuildPhysicalTableDTO buildPhysicalTableDTO) {
        /*StringBuilder sqlSelect = new StringBuilder();
        StringBuilder sqlFileds = new StringBuilder();
        sqlSelect.append("select ");
        sqlSelect.append((buildPhysicalTableDTO.driveType== DbTypeEnum.sqlserver ?" NEWID()":" UUID() ")+" as "+buildPhysicalTableDTO.appAbbreviation+"_"+buildPhysicalTableDTO.tableName+"key , ");
        //判断字符串函数与占位符
        List<TableFieldsDTO> tableFieldsDTOS = buildPhysicalTableDTO.tableFieldsDTOS;
        tableFieldsDTOS.forEach((l) -> {
            sqlFileds.append( l.fieldName + " " + l.fieldType.toLowerCase() + "("+l.fieldLength+") ,");
            sqlSelect.append(""+l.sourceFieldName+" as "+l.fieldName +" ,");
            });
        sqlFileds.delete(sqlFileds.length()-1,sqlFileds.length());
        sqlFileds.append(")");
        sqlSelect.delete(sqlSelect.length()-1,sqlSelect.length());
        sqlSelect.append(" from ("+buildPhysicalTableDTO.selectSql+") xyx");
        String s = sqlSelect.toString();*/
        String s = buildPhysicalTableDTO.selectSql;
        log.info("物理表查询语句" + s);
        return s;
    }

    /**
     * 解释cron表达式
     */
    public String describeCron(String expressiion) {
        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        Cron cron = parser.parse(expressiion);
        //设置语言
        CronDescriptor descriptor = CronDescriptor.instance(Locale.CHINESE);
        return descriptor.describe(cron);
    }

    /**
     * 检查cron表达式的合法性
     *
     * @param cron cron exp
     * @return true if valid
     */
    public boolean checkValid(String cron) {
        try {
            CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
            CronParser parser = new CronParser(cronDefinition);
            parser.parse(cron);
        } catch (IllegalArgumentException e) {
            System.out.println(String.format("cron=%s not valid", cron));
            return false;
        }
        return true;
    }

    /**
     * @param cronExpression cron表达式
     * @param numTimes       下一(几)次运行的时间
     * @return
     */
    public List<Date> getNextExecTime(String cronExpression, Integer numTimes) throws ParseException {
        List<String> list = new ArrayList<>();
        CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
        cronTriggerImpl.setCronExpression(cronExpression);
        // 这个是重点，一行代码搞定
        return TriggerUtils.computeFireTimes(cronTriggerImpl, null, numTimes);
    }

}
