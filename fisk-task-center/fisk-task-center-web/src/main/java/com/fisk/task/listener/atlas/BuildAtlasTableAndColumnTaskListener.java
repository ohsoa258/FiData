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
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.dto.task.BuildPhysicalTableDTO;
import com.fisk.task.entity.TBETLIncrementalPO;
import com.fisk.task.entity.TaskPgTableStructurePO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.TriggerUtils;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
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
        try {
            BuildPhysicalTableDTO buildPhysicalTableDTO = JSON.parseObject(dataInfo, BuildPhysicalTableDTO.class);
            String physicalSelect = createPhysicalTable(buildPhysicalTableDTO);
            //endregion
            TBETLIncrementalPO ETLIncremental = new TBETLIncrementalPO();
            if (buildPhysicalTableDTO.whetherSchema) {
                ETLIncremental.objectName = buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName;
            } else {
                ETLIncremental.objectName = buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName;
            }
            ETLIncremental.enableFlag = "1";
            ETLIncremental.incrementalObjectivescoreBatchno = UUID.randomUUID().toString();
            Map<String, Object> conditionHashMap = new HashMap<>();
            conditionHashMap.put("object_name", ETLIncremental.objectName);
            List<TBETLIncrementalPO> tbetlIncrementalPos = incrementalMapper.selectByMap(conditionHashMap);
            if (tbetlIncrementalPos != null && tbetlIncrementalPos.size() > 0) {
                log.info("此表已有同步记录,无需重复添加");
            } else {
                incrementalMapper.insert(ETLIncremental);
            }

            TableNifiSettingPO one = tableNifiSettingService.query().eq("app_id", buildPhysicalTableDTO.appId).eq("table_access_id", buildPhysicalTableDTO.dbId).eq("type", OlapTableEnum.PHYSICS.getValue()).one();
            TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
            if (one != null) {
                tableNifiSettingPO = one;
            }
            tableNifiSettingPO.appId = Integer.valueOf(buildPhysicalTableDTO.appId);
            if (buildPhysicalTableDTO.whetherSchema) {
                tableNifiSettingPO.tableName = buildPhysicalTableDTO.appAbbreviation + "." + buildPhysicalTableDTO.tableName;
            } else {
                tableNifiSettingPO.tableName = buildPhysicalTableDTO.appAbbreviation + "_" + buildPhysicalTableDTO.tableName;
            }

            tableNifiSettingPO.tableAccessId = Integer.valueOf(buildPhysicalTableDTO.dbId);
            tableNifiSettingPO.selectSql = physicalSelect;
            tableNifiSettingPO.type = OlapTableEnum.PHYSICS.getValue();
            tableNifiSettingPO.syncMode = buildPhysicalTableDTO.syncMode;
            tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
            log.info("开始执行nifi创建数据同步");
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
            log.info("nifi传入参数：" + JSON.toJSONString(bfd));
            pc.publishBuildNifiFlowTask(bfd);
            log.info("执行完成");
            return ResultEnum.SUCCESS;
        } catch (Exception e) {
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
