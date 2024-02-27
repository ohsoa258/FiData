package com.fisk.task.listener.doris;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.davis.client.ApiException;
import com.davis.client.model.*;
import com.fisk.common.core.baseObject.entity.BusinessResult;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.enums.datamodel.DataModelTblTypeEnum;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.enums.ComponentIdTypeEnum;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.datamodel.dto.modelpublish.ModelPublishDataDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.controller.PublishTaskController;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.dto.nifi.*;
import com.fisk.task.dto.task.BuildNifiFlowDTO;
import com.fisk.task.entity.TBETLIncrementalPO;
import com.fisk.task.entity.TaskDwDimPO;
import com.fisk.task.entity.TaskPgTableStructurePO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import com.fisk.task.enums.PortComponentEnum;
import com.fisk.task.listener.postgre.datainput.IbuildTable;
import com.fisk.task.listener.postgre.datainput.impl.BuildFactoryHelper;
import com.fisk.task.mapper.TBETLIncrementalMapper;
import com.fisk.task.mapper.TaskDwDimMapper;
import com.fisk.task.mapper.TaskPgTableStructureMapper;
import com.fisk.task.po.AppNifiSettingPO;
import com.fisk.task.po.NifiConfigPO;
import com.fisk.task.po.TableNifiSettingPO;
import com.fisk.task.service.doris.IDorisBuild;
import com.fisk.task.service.nifi.IJdbcBuild;
import com.fisk.task.service.nifi.ITaskDwDim;
import com.fisk.task.service.nifi.impl.AppNifiSettingServiceImpl;
import com.fisk.task.service.nifi.impl.TableNifiSettingServiceImpl;
import com.fisk.task.service.pipeline.impl.NifiConfigServiceImpl;
import com.fisk.task.utils.NifiHelper;
import com.fisk.task.utils.NifiPositionHelper;
import com.fisk.task.utils.StackTraceHelper;
import com.fisk.task.utils.TaskPgTableStructureHelper;
import com.fisk.task.utils.nifi.INiFiHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: cfk
 * CreateTime: 2021/08/03 15:05
 * Description:
 */
@Component
@Slf4j
public class BuildDataModelDorisTableListener
        extends ServiceImpl<TaskPgTableStructureMapper, TaskPgTableStructurePO> {
    @Resource
    DataModelClient dc;
    @Resource
    IDorisBuild doris;
    @Resource
    IJdbcBuild iPostgreBuild;
    @Resource
    ITaskDwDim iTaskDwDim;
    @Resource
    TaskDwDimMapper taskDwDimMapper;
    @Resource
    DataAccessClient client;
    @Resource
    TaskPgTableStructureMapper taskPgTableStructureMapper;
    @Resource
    INiFiHelper componentsBuild;
    @Resource
    TaskPgTableStructureHelper taskPgTableStructureHelper;
    @Resource
    AppNifiSettingServiceImpl appNifiSettingService;
    @Resource
    NifiConfigServiceImpl nifiConfigService;
    @Resource
    TableNifiSettingServiceImpl tableNifiSettingService;
    @Resource
    public DataModelClient dataModelClient;
    @Resource
    PublishTaskController pc;
    @Resource
    private TBETLIncrementalMapper incrementalMapper;
    @Resource
    public UserClient userClient;
    @Resource
    INiFiHelper iNiFiHelper;

    @Value("${fiData-data-dw-source}")
    public String dataSourceDwId;
    public String appParentGroupId;
    public String appGroupId;
    public String groupEntityId;
    public String taskGroupEntityId;
    public String appInputPortId;
    public String tableInputPortId;
    public String appOutputPortId;
    public String tableOutputPortId;


    public ResultEnum msg(String dataInfo, Acknowledgment acke) {
        ResultEnum result = ResultEnum.SUCCESS;
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        int id = 0;
        int tableType = 0;
        log.info("dw创建表参数:" + dataInfo);
        //saveTableStructure(list);
        //1.,查询语句,并存库
        //2.修改表的存储过程
        //3.生成nifi流程
        try {
            //将转为json字符串的数据重新转为对象
            ModelPublishDataDTO inpData = JSON.parseObject(dataInfo, ModelPublishDataDTO.class);
            //获取维度列表
            List<ModelPublishTableDTO> dimensionList = inpData.dimensionList;
            //远程调用systemCenter的方法，获取id为1的数据源的信息，也就是dw   dmp_system_db库----db_datasource_config表
            ResultEntity<DataSourceDTO> DataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
            DataSourceTypeEnum conType = null;
            if (DataSource.code == ResultEnum.SUCCESS.getCode()) {
                //获取数据源信息
                DataSourceDTO dataSource = DataSource.data;
                //conType==1  SqlServer
                conType = dataSource.conType;
            } else {
                log.error("userclient无法查询到dw库的连接信息");
                throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
            }
            //遍历维度列表
            for (ModelPublishTableDTO modelPublishTableDTO : dimensionList) {
                //获取表id
                id = Math.toIntExact(modelPublishTableDTO.tableId);
                //获取表类型
                tableType = modelPublishTableDTO.createType;
                //生成版本号
                //获取时间戳版本号
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                Calendar calendar = Calendar.getInstance();
                String version = df.format(calendar.getTime());

                //todo：doris数据库不支持存储过程，因此这里先将doris数仓排除在外
                //调用方法，保存建模相关表结构数据(保存版本号)
                ResultEnum resultEnum = null;
                String msg = null;
                if (!DataSourceTypeEnum.DORIS.getName().equalsIgnoreCase(conType.getName())) {
                    resultEnum = taskPgTableStructureHelper.saveTableStructure(modelPublishTableDTO, version, conType);
                    if (resultEnum.getCode() != ResultEnum.TASK_TABLE_NOT_EXIST.getCode() && resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
                        taskPgTableStructureMapper.updatevalidVersion(version);
                        throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL, "修改表结构失败，请您检查表是否存在或字段数据格式是否能被转换为目标格式");
                    }
                    log.info("数仓执行修改表结构的存储过程返回结果" + resultEnum);
                } else {
                    msg = taskPgTableStructureHelper.saveTableStructureForDoris(modelPublishTableDTO, version, conType);
//                    resultEnum = taskPgTableStructureHelper.saveTableStructureForDoris(modelPublishTableDTO, version, conType);
//                    if (resultEnum.getCode() != ResultEnum.TASK_TABLE_NOT_EXIST.getCode() && resultEnum.getCode() != ResultEnum.SUCCESS.getCode()) {
                    if (!ResultEnum.TASK_TABLE_NOT_EXIST.getMsg().equals(msg) && !ResultEnum.SUCCESS.getMsg().equals(msg)) {
                        taskPgTableStructureMapper.updatevalidVersion(version);
                        throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL, msg);
                    }
                    log.info("数仓执行修改表结构的存储过程返回结果" + msg);
                }

                //如果前端选择删除目标表 则这里删除目标表
                if (inpData.ifDropTargetTbl) {
                    log.info("开始删除目标表");
                    String dropSql = "DROP TABLE IF EXISTS " + modelPublishTableDTO.tableName;
                    BusinessResult businessResult = iPostgreBuild.postgreBuildTable(dropSql, BusinessTypeEnum.DATAMODEL);
                    if (!businessResult.success) {
                        throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
                    } else {
                        msg = "表不存在";
                        resultEnum = ResultEnum.TASK_TABLE_NOT_EXIST;
                    }
                }


                //生成建表语句
                List<String> pgdbTable2 = new ArrayList<>();
                //远程调用systemCenter的方法，获取id为1的数据源的信息，也就是dw   dmp_system_db库----tb_datasource_config表
                ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
                //如果获取成功
                if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                    //获取数据源
                    DataSourceDTO dataSource = fiDataDataSource.data;
                    //根据数据源连接类型，获取建表实现类   pg/sqlServer/doris
                    IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dataSource.conType);

                    //todo:针对doris作为数仓  当创建dim维度表的时候，创建的是主键模型
                    //todo:当创建fact事实表（fact/help/config）表的时候，创建的是Duplicate 明细模型（冗余模型），即对进表的源数据不做任何操作
                    //     事实表如果有主键 则建的表也是主键模型
                    //todo:上文仅针对dw表 不针对temp临时表，临时表统一是冗余模型
                    if (DataSourceTypeEnum.DORIS.getName().equalsIgnoreCase(conType.getName())) {
                        //获取此次发布类型 dim or fact
                        DataModelTblTypeEnum dimOrFact = inpData.getDimOrFact();
                        log.info("此次数仓表发布类型：" + dimOrFact);
                        switch (dimOrFact) {
                            case DIM:
                                pgdbTable2 = dbCommand.buildDorisDimTables(modelPublishTableDTO);
                                break;
                            case FACT:
                                //校验事实表是否有主键
                                boolean ifUnique = false;
                                for (ModelPublishFieldDTO dto : modelPublishTableDTO.fieldList) {
                                    if (dto.isBusinessKey == 1) {
                                        ifUnique = true;
                                        break;
                                    }
                                }
                                //如果没有主键 则是冗余模型
                                if (!ifUnique) {
                                    pgdbTable2 = dbCommand.buildDorisFactTables(modelPublishTableDTO);
                                } else {
                                    //如果有主键 则是主键模型
                                    pgdbTable2 = dbCommand.buildDorisDimTables(modelPublishTableDTO);
                                }
                                break;
                            default:
                                throw new FkException(ResultEnum.ENUM_TYPE_ERROR, String.valueOf(dimOrFact));
                        }
                    } else {
                        //根据维度列表字段，调用方法，返回两条建表语句
                        pgdbTable2 = dbCommand.buildDwStgAndOdsTable(modelPublishTableDTO);
                    }

                    //新建map集合，预装载键值对
                    HashMap<String, Object> map = new HashMap<>();
                    //放入表名称
                    map.put("table_name", modelPublishTableDTO.tableName);
                    //从dmp_task_db模块 tb_task_dw_dim表删除待发布的表信息
                    taskDwDimMapper.deleteByMap(map);

                    //新建tb_task_dw_dim表的po
                    TaskDwDimPO taskDwDimPO = new TaskDwDimPO();
                    //taskDwDimPO.areaBusinessName=businessAreaName;//业务域名
                    //获取创建表的sql，也就是pgdbTable2集合中的第二条sql CREATE TABLE....
                    taskDwDimPO.sqlContent = pgdbTable2.get(1);
                    //表名
                    taskDwDimPO.tableName = modelPublishTableDTO.tableName;
                    //要调用的存储过程名称
                    taskDwDimPO.storedProcedureName = "update" + modelPublishTableDTO.tableName + "()";
                    //插入到tb_task_dw_dim表中
                    taskDwDimMapper.insert(taskDwDimPO);
                    log.info("建模创表语句:" + JSON.toJSONString(pgdbTable2));
                } else {
                    log.error("userclient无法查询到dw库的连接信息");
                    throw new FkException(ResultEnum.ERROR);
                }
                //执行临时表建表sql，也就是pgdbTable2集合中的第一条sql  DROP TABLE IF EXISTS....  数据建模---dw库
                BusinessResult businessResult = iPostgreBuild.postgreBuildTable(pgdbTable2.get(0), BusinessTypeEnum.DATAMODEL);
                if (!businessResult.success) {
                    throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
                }

                if (resultEnum != null) {
                    if (resultEnum.getCode() == ResultEnum.TASK_TABLE_NOT_EXIST.getCode()) {
                        //执行最终表创建表的sql,也就是pgdbTable2集合中的第二条sql CREATE TABLE....
                        BusinessResult businessResult1 = iPostgreBuild.postgreBuildTable(pgdbTable2.get(1), BusinessTypeEnum.DATAMODEL);
                        if (!businessResult1.success) {
                            throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
                        }
                    }
                } else {
                    if (ResultEnum.TASK_TABLE_NOT_EXIST.getMsg().equals(msg)) {
                        //执行最终表创建表的sql,也就是pgdbTable2集合中的第二条sql CREATE TABLE....
                        BusinessResult businessResult1 = iPostgreBuild.postgreBuildTable(pgdbTable2.get(1), BusinessTypeEnum.DATAMODEL);
                        if (!businessResult1.success) {
                            throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
                        }
                    }
                }

                //生成函数,并执行
            /*String storedProcedure3 = createStoredProcedure3(modelPublishTableDTO);
            log.info("dw库生成函数:"+storedProcedure3);
            iPostgreBuild.postgreBuildTable(storedProcedure3, BusinessTypeEnum.DATAMODEL);*/
                log.info("开始执行nifi创建数据同步");
                //新建dmp_system_db库----tb_etl_Incremental表对象
                TBETLIncrementalPO ETLIncremental = new TBETLIncrementalPO();
                //数据同步流程的表名
                ETLIncremental.objectName = modelPublishTableDTO.tableName;
                //同步流程是否启动,1启动  2停止
                ETLIncremental.enableFlag = "1";
                //数据同步批次号
                ETLIncremental.incrementalObjectivescoreBatchno = UUID.randomUUID().toString();

                Map<String, Object> conditionHashMap = new HashMap<>();
                conditionHashMap.put("object_name", ETLIncremental.objectName);
                //通过表名获取数据，校验是否已有同步记录
                List<TBETLIncrementalPO> tbetlIncrementalPos = incrementalMapper.selectByMap(conditionHashMap);
                if (tbetlIncrementalPos != null && tbetlIncrementalPos.size() > 0) {
                    log.info("此表已有同步记录,无需重复添加");
                } else {
                    //没有同步记录，则插入一条
                    incrementalMapper.insert(ETLIncremental);
                }

                BuildNifiFlowDTO bfd = new BuildNifiFlowDTO();
                //用户id
                bfd.userId = inpData.userId;
                //应用id
                bfd.appId = inpData.businessAreaId;
                //ODSTODW
                bfd.synchronousTypeEnum = SynchronousTypeEnum.PGTOPG;
                //来源为数据建模
                bfd.dataClassifyEnum = DataClassifyEnum.DATAMODELING;
                //表id
                bfd.id = modelPublishTableDTO.tableId;
                //表名
                bfd.tableName = modelPublishTableDTO.tableName;
                //selectsql(只有toDoris,todw用得到)
                bfd.selectSql = modelPublishTableDTO.sqlScript;
                //同步方式
                bfd.synMode = modelPublishTableDTO.synMode;
                //查询范围开始时间
                bfd.queryStartTime = modelPublishTableDTO.queryStartTime;
                //查询范围结束时间
                bfd.queryEndTime = modelPublishTableDTO.queryEndTime;
                //业务域名称
                bfd.appName = inpData.businessAreaName;
                //是否同步
                bfd.openTransmission = inpData.openTransmission;
                //事实表-维度键的更新sql集合
                bfd.updateSql = modelPublishTableDTO.factUpdateSql;
                //数据来源id
                bfd.dataSourceDbId = modelPublishTableDTO.dataSourceDbId;
                //目标数据源id
                bfd.targetDbId = modelPublishTableDTO.targetDbId;
                //临时表名称
                bfd.prefixTempName = modelPublishTableDTO.prefixTempName;
                //自定义脚本执行--前
                bfd.customScriptBefore = modelPublishTableDTO.customScript;
                //自定义脚本执行--后
                bfd.customScriptAfter = modelPublishTableDTO.customScriptAfter;
                //设置预览SQL执行语句  覆盖脚本执行SQL语句
                bfd.syncStgToOdsSql = modelPublishTableDTO.coverScript;
                //删除temp表的sql
                bfd.deleteScript = modelPublishTableDTO.deleteTempScript;
                //临时表(建模temp_tablename)建表语句
                bfd.buildTableSql = pgdbTable2.get(0);
                bfd.concurrencyNums = 1;

                //获取表名 为了拼接临时表主键名称
                String tableName = modelPublishTableDTO.tableName;
                if (conType == DataSourceTypeEnum.POSTGRESQL) {
                    if (modelPublishTableDTO.createType == 0) {
                        //临时表主键名称
                        bfd.pkName = tableName.substring(4) + "key";
                    } else {
                        bfd.pkName = tableName.substring(5) + "key";
                    }
                } else {
                    bfd.pkName = tableName.substring(tableName.indexOf("_") + 1) + "key";
                }

                if (modelPublishTableDTO.createType == 0) {
                    //类型为物理表
                    bfd.type = OlapTableEnum.DIMENSION;
                } else {
                    //类型为事实表
                    bfd.type = OlapTableEnum.FACT;
                }
                bfd.maxRowsPerFlowFile = modelPublishTableDTO.maxRowsPerFlowFile;
                bfd.fetchSize = modelPublishTableDTO.fetchSize;
                bfd.traceId = inpData.traceId;
                log.info("nifi传入参数：" + JSON.toJSONString(bfd));
                //通过应用id(app_id)，表id(table_access_id),表类别(物理表3,事实表2,维度表1,指标表0)type从dmp_system_db库 tb_table_nifi_setting表获取数据
                TableNifiSettingPO one = tableNifiSettingService.query().eq("app_id", bfd.appId).eq("table_access_id", bfd.id).eq("type", bfd.type.getValue()).one();
                TableNifiSettingPO tableNifiSettingPO = new TableNifiSettingPO();
                if (one != null) {
                    tableNifiSettingPO = one;
                }
                //应用id
                tableNifiSettingPO.appId = Math.toIntExact(bfd.appId);
                //表名称
                tableNifiSettingPO.tableName = bfd.tableName;
                //表id
                tableNifiSettingPO.tableAccessId = Math.toIntExact(bfd.id);
                //查询sql
                tableNifiSettingPO.selectSql = bfd.selectSql;
                //表类别(物理表3,事实表2,维度表1,指标表0)
                tableNifiSettingPO.type = bfd.type.getValue();
                //同步方式
                tableNifiSettingPO.syncMode = 1;
                //执行保存或更新
                tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
                //弹框
                bfd.popout = true;
                //调用方法，创建同步数据nifi流程
                pc.publishBuildNifiFlowTask(bfd);
                log.info("执行完成");

                //执行完成后，修改维度表或事实表的发布状态
                //0维度表
                if (modelPublishTableDTO.createType == 0) {
                    modelPublishStatusDTO.status = 1;
                    modelPublishStatusDTO.id = Math.toIntExact(modelPublishTableDTO.tableId);
                    modelPublishStatusDTO.type = 0;
                    dataModelClient.updateDimensionPublishStatus(modelPublishStatusDTO);
                } else {
                    modelPublishStatusDTO.status = 1;
                    modelPublishStatusDTO.id = Math.toIntExact(modelPublishTableDTO.tableId);
                    modelPublishStatusDTO.type = 0;
                    dataModelClient.updateFactPublishStatus(modelPublishStatusDTO);
                }

            }
            return result;
        } catch (Exception e) {
            log.error("dw发布失败,表id为" + id + StackTraceHelper.getStackTraceInfo(e));
            result = ResultEnum.CREATE_TABLE_ERROR;
            //执行失败后，修改维度表或事实表的发布状态
            if (tableType == 0) {
                modelPublishStatusDTO.status = 2;
                modelPublishStatusDTO.id = Math.toIntExact(id);
                modelPublishStatusDTO.type = 0;
                dataModelClient.updateDimensionPublishStatus(modelPublishStatusDTO);
            } else {
                modelPublishStatusDTO.status = 2;
                modelPublishStatusDTO.id = Math.toIntExact(id);
                modelPublishStatusDTO.type = 0;
                dataModelClient.updateFactPublishStatus(modelPublishStatusDTO);
            }
//            return result;
            throw new FkException(ResultEnum.DATA_MODEL_PUBLISH_ERROR, e.toString(), e);
        } finally {
            acke.acknowledge();
        }
    }

    public ResultEnum buildDorisAggregateTableListener(String dataInfo, Acknowledgment acke) {
        ResultEnum result = ResultEnum.SUCCESS;
        ModelPublishStatusDTO modelPublishStatusDTO = new ModelPublishStatusDTO();
        int id = 0;
        int tableType = 0;
        log.info("dw创建doris聚合模型表参数:" + dataInfo);
        //saveTableStructure(list);
        //1.,查询语句,并存库
        //2.是否修改表结构
        //3.生成nifi流程
        try {
            //将转为json字符串的数据重新转为对象
            ModelPublishDataDTO inpData = JSON.parseObject(dataInfo, ModelPublishDataDTO.class);
            //获取维度列表
            List<ModelPublishTableDTO> dimensionList = inpData.dimensionList;
            //远程调用systemCenter的方法，获取id为1的数据源的信息，也就是dw   dmp_system_db库----db_datasource_config表
            ResultEntity<DataSourceDTO> DataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
            DataSourceTypeEnum conType = null;
            if (DataSource.code == ResultEnum.SUCCESS.getCode()) {
                //获取数据源信息
                DataSourceDTO dataSource = DataSource.data;
                //conType==1  SqlServer
                conType = dataSource.conType;
            } else {
                log.error("userclient无法查询到dw库的连接信息");
                throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
            }

            //遍历维度列表
            for (ModelPublishTableDTO modelPublishTableDTO : dimensionList) {
                //获取表id
                id = Math.toIntExact(modelPublishTableDTO.tableId);
                //获取表类型
                tableType = modelPublishTableDTO.createType;
                //生成版本号
                //获取时间戳版本号
                DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                Calendar calendar = Calendar.getInstance();
                String version = df.format(calendar.getTime());

                //调用方法，保存建模相关表结构数据(保存版本号)
                ResultEnum resultEnum = null;
                String msg = taskPgTableStructureHelper.saveTableStructureForDoris(modelPublishTableDTO, version, conType);
                if (!ResultEnum.TASK_TABLE_NOT_EXIST.getMsg().equals(msg) && !ResultEnum.SUCCESS.getMsg().equals(msg)) {
                    taskPgTableStructureMapper.updatevalidVersion(version);
                    throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL, msg);
                }
                log.info("数仓-建doris聚合模型执行修改表结构的方法的返回结果：" + msg);

                //生成建表语句
                List<String> pgdbTable2 = new ArrayList<>();
                //远程调用systemCenter的方法，获取id为1的数据源的信息，也就是dw   dmp_system_db库----tb_datasource_config表
                //如果获取成功
                if (DataSource.code == ResultEnum.SUCCESS.getCode()) {
                    //获取数据源
                    DataSourceDTO dataSource = DataSource.data;
                    //根据数据源连接类型，获取建表实现类   doris
                    IbuildTable dbCommand = BuildFactoryHelper.getDBCommand(dataSource.conType);

                    //获取字段  根据字段的属性 判断建什么模型
                    List<ModelPublishFieldDTO> fieldList = modelPublishTableDTO.getFieldList();
                    //是否主键模型
                    boolean ifUniqueModel = false;
                    //是否聚合模型
                    boolean ifAggregate = false;
                    for (ModelPublishFieldDTO dto : fieldList) {
                        if (dto.isBusinessKey == 1) {
                            ifUniqueModel = true;
                            break;
                        } else if (dto.isAggregateKey == 1) {
                            ifAggregate = true;
                            break;
                        }
                    }

                    if (DataSourceTypeEnum.DORIS.getName().equalsIgnoreCase(conType.getName())) {

                        //聚合模型
                        if (ifAggregate) {
                            //todo:针对doris作为数仓  该方法只用于创建doris聚合模型表
                            pgdbTable2 = dbCommand.buildDorisaAggregateTables(modelPublishTableDTO);
                        } else if (ifUniqueModel) {
                            //主键模型  不包含系统字段
                            pgdbTable2 = dbCommand.buildDorisDimTablesWithoutSystemFields(modelPublishTableDTO);
                        } else {
                            //冗余模型  不包含系统字段
                            pgdbTable2 = dbCommand.buildDorisFactTablesWithoutSystemFields(modelPublishTableDTO);
                        }

                    }

                    //新建map集合，预装载键值对
                    HashMap<String, Object> map = new HashMap<>();
                    //放入表名称
                    map.put("table_name", modelPublishTableDTO.tableName);
                    //从dmp_task_db模块 tb_task_dw_dim表删除待发布的表信息
                    taskDwDimMapper.deleteByMap(map);

                    //新建tb_task_dw_dim表的po
                    TaskDwDimPO taskDwDimPO = new TaskDwDimPO();
                    //taskDwDimPO.areaBusinessName=businessAreaName;//业务域名
                    //获取doris创建聚合模型表的sql
                    if (CollectionUtils.isNotEmpty(pgdbTable2)) taskDwDimPO.sqlContent = pgdbTable2.get(1);
                    //表名
                    taskDwDimPO.tableName = modelPublishTableDTO.tableName;
                    //要调用的存储过程名称
                    taskDwDimPO.storedProcedureName = "update" + modelPublishTableDTO.tableName + "()";
                    //插入到tb_task_dw_dim表中
                    taskDwDimMapper.insert(taskDwDimPO);
                    log.info("建模创表语句:" + JSON.toJSONString(pgdbTable2));
                } else {
                    log.error("userclient无法查询到dw库的连接信息");
                    throw new FkException(ResultEnum.ERROR);
                }

                // 聚合模型直接建目标表  不建临时表
                BusinessResult businessResult1 = iPostgreBuild.postgreBuildTable(pgdbTable2.get(1), BusinessTypeEnum.DATAMODEL);
                if (!businessResult1.success) {
                    throw new FkException(ResultEnum.TASK_TABLE_CREATE_FAIL);
                }

                //建表完成后，修改事实表的发布状态
                modelPublishStatusDTO.status = 1;
                modelPublishStatusDTO.id = Math.toIntExact(modelPublishTableDTO.tableId);
                modelPublishStatusDTO.type = 0;
                dataModelClient.updateFactPublishStatus(modelPublishStatusDTO);
            }
            return result;
        } catch (Exception e) {
            log.error("dw发布失败,表id为" + id + StackTraceHelper.getStackTraceInfo(e));
            //执行失败后，修改维度表或事实表的发布状态
            if (tableType == 0) {
                modelPublishStatusDTO.status = 2;
                modelPublishStatusDTO.id = Math.toIntExact(id);
                modelPublishStatusDTO.type = 0;
                dataModelClient.updateDimensionPublishStatus(modelPublishStatusDTO);
            } else {
                modelPublishStatusDTO.status = 2;
                modelPublishStatusDTO.id = Math.toIntExact(id);
                modelPublishStatusDTO.type = 0;
                dataModelClient.updateFactPublishStatus(modelPublishStatusDTO);
            }
//            return result;
            throw new FkException(ResultEnum.DATA_MODEL_PUBLISH_ERROR, e.toString(), e);
        } finally {
            acke.acknowledge();
        }
    }

    /**
     * 发消息删除nifi流程
     *
     * @param dataInfo
     * @param acke
     * @return
     */
    public ResultEnum deleteNifiFlowByKafka(String dataInfo, Acknowledgment acke) {
        log.info("dw发消息删除nifi流程:" + dataInfo);
        DataModelVO dataModelVO = JSON.parseObject(dataInfo, DataModelVO.class);
        return iNiFiHelper.deleteNifiFlow(dataModelVO);
    }

    public String createStoredProcedure3(ModelPublishTableDTO modelPublishTableDTO) {
        String tableName = modelPublishTableDTO.tableName;
        List<ModelPublishFieldDTO> fieldList1 = modelPublishTableDTO.fieldList;
        String tablePk = "";
        if (modelPublishTableDTO.createType == 0) {
            tablePk = modelPublishTableDTO.tableName.substring(4) + "key";
        } else {
            tablePk = modelPublishTableDTO.tableName.substring(5) + "key";
        }
        String fileds = tablePk + ",";
        for (ModelPublishFieldDTO modelPublishFieldDTO : fieldList1) {
            fileds += "" + modelPublishFieldDTO.fieldEnName + ",";
        }
        String truncateTable = "'truncate table " + tableName + "';\n";
        fileds += "fi_createtime,fi_updatetime";
        String storedProcedureSql = "CREATE OR REPLACE PROCEDURE public.update" + tableName + "() \n" +
                "LANGUAGE 'plpgsql'\nas $BODY$\nDECLARE\nmysqlp text;\nmysqlk text;\nbegin\n";
        storedProcedureSql += "mysqlk:=" + truncateTable + "mysqlp:='INSERT INTO " + tableName + " (" + fileds + ") SELECT " + fileds + " FROM('||' " + selectSql1(modelPublishTableDTO);
        storedProcedureSql += "\nraise notice'%',mysqlk;\nEXECUTE mysqlk;\nraise notice'%',mysqlp;\nEXECUTE mysqlp;\n";
        storedProcedureSql += "end\n$BODY$;\n";
        List<ModelPublishFieldDTO> fieldList = modelPublishTableDTO.fieldList;
        //找到不同的关联表
        List<ModelPublishFieldDTO> collect1 = fieldList.stream().filter(e -> e.associateDimensionName != null).collect(Collectors.toList());
        List<ModelPublishFieldDTO> modelPublishFieldDTOS = collect1.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ModelPublishFieldDTO::getAssociateDimensionName))), ArrayList::new));
        modelPublishFieldDTOS.removeAll(Collections.singleton(null));
        if (modelPublishFieldDTOS.size() != 0) {
            int i = 0;
            for (ModelPublishFieldDTO modelPublishFieldDTO : modelPublishFieldDTOS) {
                //找到每个关联表关联的所有字段
                List<ModelPublishFieldDTO> collect = fieldList.stream().filter(e -> e.associateDimensionName != null && e.associateDimensionName.equals(modelPublishFieldDTO.associateDimensionName)).collect(Collectors.toList());
                //拼接语句,添加外键
                if (modelPublishFieldDTO.associateDimensionFieldName != null) {
                    storedProcedureSql = storedProcedureSql.replace("DECLARE\n", "DECLARE\nmysql" + i + " text;\n");
                    //	UPDATE fact_InternetSales1
                    //  SET Date1key=d.Date1key
                    // FROM dim_Date1 as d
                    //  WHERE fact_InternetSales1.orderdate=d.fulldatealternatekey
                    String associateDimensionName = modelPublishFieldDTO.associateDimensionName;
                    String associateDimensionNamePK = modelPublishFieldDTO.associateDimensionName.substring(4) + "key";
                    String sql = "update " + modelPublishTableDTO.tableName + " set  " + associateDimensionNamePK +
                            "=" + associateDimensionName + "." + associateDimensionNamePK + " from " + associateDimensionName + " where ";
                    //条件

                    for (int j = 0; j < collect.size(); j++) {
                        ModelPublishFieldDTO modelPublishFieldDTO1 = collect.get(j);
                        sql += modelPublishTableDTO.tableName + "." + modelPublishFieldDTO1.fieldEnName + "=" + modelPublishFieldDTO1.associateDimensionName + "." + modelPublishFieldDTO1.associateDimensionFieldName + " and ";
                    }
                    sql = sql.substring(0, sql.length() - 4);
                    storedProcedureSql = storedProcedureSql.replace("begin\n", "begin\nmysql" + i + ":='" + sql + "';\n");
                    storedProcedureSql = storedProcedureSql.replace("EXECUTE mysqlp;\n", "EXECUTE mysqlp;\n\nraise notice'%',mysql" + i + ";\nEXECUTE mysql" + i + ";\n");
                }
                i++;
            }
        }
        return storedProcedureSql;
    }

    public String selectSql1(ModelPublishTableDTO modelPublishTableDTO) {
        String tableName = modelPublishTableDTO.tableName;
        String tablePk = "";
        if (modelPublishTableDTO.createType == 0) {
            tablePk = modelPublishTableDTO.tableName.substring(4) + "key";
        } else {
            tablePk = modelPublishTableDTO.tableName.substring(5) + "key";
        }
        String pgdwDblink = "";
        String selectSql = "select * from dblink('||'''" + pgdwDblink + "'''||','||'''";
        String selectSql1 = "select sys_guid() as " + tablePk + ", ";
        StringBuilder selectSql2 = new StringBuilder("coalesce( fi_createtime,null),coalesce( fi_updatetime,null),");
        StringBuilder selectSql3 = new StringBuilder(tablePk + " varchar,fi_createtime varchar,fi_updatetime varchar,");
        StringBuilder selectSql4 = new StringBuilder(tablePk + "=EXCLUDED." + tablePk + ",fi_createtime=EXCLUDED.fi_createtime,fi_updatetime=EXCLUDED.fi_updatetime,");
        StringBuilder selectSql5 = new StringBuilder();
        StringBuilder selectSql6 = new StringBuilder(" (" + modelPublishTableDTO.sqlScript + ") fi1 ");
        List<ModelPublishFieldDTO> fieldList = modelPublishTableDTO.fieldList;
        fieldList.forEach((l) -> {
            log.info("字段属性为:" + l);
            if (l.fieldType.toLowerCase().contains("float")) {
                selectSql2.append("coalesce(" + l.sourceFieldName + " ,0.00),");
                selectSql3.append("" + l.fieldEnName + " numeric ,");
            } else if (l.fieldType.toLowerCase().contains("varchar")) {
                selectSql2.append("coalesce(" + l.sourceFieldName + " ,null),");
                selectSql3.append("" + l.fieldEnName + " VARCHAR ,");
            } else if (l.fieldType.toLowerCase().contains("int")) {
                selectSql2.append("coalesce(" + l.sourceFieldName + " ,0),");
                selectSql3.append("" + l.fieldEnName + " int ,");
            } else if (l.fieldType.toLowerCase().contains("numeric")) {
                selectSql2.append("coalesce(" + l.sourceFieldName + " ,0.00),");
                selectSql3.append("" + l.fieldEnName + " numeric ,");
            }

            selectSql4.append("" + l.fieldEnName + "=EXCLUDED." + l.fieldEnName + ",");
            if (l.isPrimaryKey == 1) {
                selectSql5.append("," + l.fieldEnName + "");
            }

            //多表
            /*if(l.associateDimensionName!=null){
            String filed=l.associateDimensionName.substring(4)+"_pk ";
                selectSql2.append(filed);
                selectSql3.append(filed+" varchar,");
                selectSql4.append(filed+"=EXCLUDED."+filed+",");
                String uuid=UUID.randomUUID().toString().replaceAll("-","");
                selectSql6.append(" left join ( select * from "+l.associateDimensionName+" ) fi"+uuid+" on fi1."+l.fieldEnName+"= fi"+uuid+"."+l.associateDimensionFieldName);
            }*/
        });
        String sql4 = selectSql6.toString();
        String sql = selectSql2.toString();
        sql = sql.substring(0, sql.length() - 1);
        sql += " from  " + sql4 + " '''||') as t (";
        String sql1 = selectSql3.toString();
        String sql3 = selectSql5.toString();
        if (sql3.length() != 0) {
            tablePk = sql3.substring(1);
        }
        sql += sql1.substring(0, sql1.length() - 1) + "))'||' AS ods ON CONFLICT ( " + tablePk + " )  DO UPDATE SET ";
        String sql2 = selectSql4.toString();
        sql += sql2.substring(0, sql2.length() - 1) + "';";
        selectSql += selectSql1 + sql;
        return selectSql;
    }

    public List<String> createPgdbTable2(ModelPublishTableDTO modelPublishTableDTO) {
        List<String> sqlList = new ArrayList<>();
        List<ModelPublishFieldDTO> fieldList = modelPublishTableDTO.fieldList;
        String tableName = modelPublishTableDTO.tableName;
        String tablePk = "";
        if (modelPublishTableDTO.createType == 0) {
            tablePk = "\"" + tableName.substring(4) + "key\"";
        } else {
            tablePk = "\"" + tableName.substring(5) + "key\"";
        }

        StringBuilder sql = new StringBuilder();
        StringBuilder pksql = new StringBuilder("PRIMARY KEY ( ");
        sql.append("CREATE TABLE " + modelPublishTableDTO.tableName + " ( " + tablePk + " varchar(50), ");
        StringBuilder sqlFileds = new StringBuilder();
        StringBuilder sqlFileds1 = new StringBuilder();
        StringBuilder stgSqlFileds = new StringBuilder();
        log.info("pg_dw建表字段信息:" + fieldList);
        fieldList.forEach((l) -> {
            if (l.fieldType.contains("INT") || l.fieldType.contains("TEXT")) {
                sqlFileds.append("\"" + l.fieldEnName + "\" " + l.fieldType.toLowerCase() + ",");
                stgSqlFileds.append("\"" + l.fieldEnName + "\" text,");
            } else if (l.fieldType.toLowerCase().contains("numeric") || l.fieldType.toLowerCase().contains("float")) {
                sqlFileds.append("\"" + l.fieldEnName + "\" float ,");
                stgSqlFileds.append("\"" + l.fieldEnName + "\" text,");
            } else {
                sqlFileds.append("\"" + l.fieldEnName + "\" " + l.fieldType.toLowerCase() + "(" + l.fieldLength + ") ,");
                stgSqlFileds.append("\"" + l.fieldEnName + "\" text,");
            }
            if (l.isPrimaryKey == 1) {
                pksql.append("" + l.fieldEnName + " ,");
            }

        });

        String sql1 = sql.toString();
        //String associatedKey = associatedConditions(fieldList);
        String associatedKey = "";
        String sql2 = sqlFileds.toString() + associatedKey;
        sql2 += "fi_createtime varchar(50),fi_updatetime varchar(50)";
        sql2 += ",fidata_batch_code varchar(50)";
        String sql3 = sqlFileds1.toString();
        if (Objects.equals("", sql3)) {
            sql1 += sql2;
        } else {
            sql1 += sql2 + sql3;
        }
        String havePk = pksql.toString();
        if (havePk.length() != 14) {
            sql1 += "," + havePk.substring(0, havePk.length() - 1) + ")";
        }
        sql1 += ")";
        //创建表
        log.info("pg_dw建表语句" + sql1);
        //String stgTable = sql1.replaceFirst(tableName, "stg_" + tableName);
        String stgTable = "DROP TABLE IF EXISTS stg_" + tableName + "; CREATE TABLE stg_" + tableName + " (" + tablePk + " varchar(50) NOT NULL DEFAULT sys_guid()," + stgSqlFileds.toString() + associatedKey + "fi_createtime varchar(50) DEFAULT to_char(CURRENT_TIMESTAMP, 'yyyy-MM-dd HH24:mi:ss'),fi_updatetime varchar(50),fi_enableflag varchar(50),fi_error_message text,fidata_batch_code varchar(50),fidata_flow_batch_code varchar(50), fi_sync_type varchar(50) DEFAULT '2',fi_verify_type varchar(50) DEFAULT '3');";
        stgTable += "create index " + tableName + "enableflagsy on stg_" + tableName + " (fi_enableflag);";
        sqlList.add(stgTable);
        sqlList.add(sql1);
        HashMap<String, Object> map = new HashMap<>();
        map.put("table_name", tableName);
        taskDwDimMapper.deleteByMap(map);
        TaskDwDimPO taskDwDimPO = new TaskDwDimPO();
        //taskDwDimPO.areaBusinessName=businessAreaName;//业务域名
        taskDwDimPO.sqlContent = sql1;//创建表的sql
        taskDwDimPO.tableName = tableName;
        taskDwDimPO.storedProcedureName = "update" + tableName + "()";
        taskDwDimMapper.insert(taskDwDimPO);
        return sqlList;
    }

    public String associatedConditions(List<ModelPublishFieldDTO> fieldList) {
        String filed = "";
        List<ModelPublishFieldDTO> collect1 = fieldList.stream().filter(e -> e.associateDimensionName != null).collect(Collectors.toList());
        List<ModelPublishFieldDTO> modelPublishFieldDTOS = collect1.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ModelPublishFieldDTO::getAssociateDimensionName))), ArrayList::new));
        modelPublishFieldDTOS.removeAll(Collections.singleton(null));
        if (modelPublishFieldDTOS.size() != 0) {
            for (ModelPublishFieldDTO modelPublishFieldDTO : modelPublishFieldDTOS) {
                filed += modelPublishFieldDTO.associateDimensionName.substring(4) + "key varchar(50),";
            }
        }
        return filed;
    }

    public void createNiFiFlow(ModelPublishDataDTO modelPublishDataDTO, ModelPublishTableDTO modelMetaDataDTO, String businessAreaName, DataClassifyEnum dataClassifyEnum, OlapTableEnum olapTableEnum) {
        BuildDbControllerServiceDTO buildDbControllerServiceDTO = new BuildDbControllerServiceDTO();
        //数据连接池
        ControllerServiceEntity data = new ControllerServiceEntity();
        //应用组
        ProcessGroupEntity data1 = new ProcessGroupEntity();
        //任务组
        ProcessGroupEntity data2 = new ProcessGroupEntity();

        //4. 创建任务组创建时要把原任务组删掉,防止重复发布带来影响  dto.id, dto.appId
        DataModelVO dataModelVO = new DataModelVO();
        dataModelVO.dataClassifyEnum = dataClassifyEnum;
        dataModelVO.delBusiness = false;
        dataModelVO.businessId = String.valueOf(modelPublishDataDTO.businessAreaId);
        DataModelTableVO dataModelTableVO = new DataModelTableVO();
        dataModelTableVO.type = olapTableEnum;
        List<Long> ids = new ArrayList<>();
        ids.add(modelMetaDataDTO.tableId);
        dataModelTableVO.ids = ids;
        dataModelVO.indicatorIdList = dataModelTableVO;
        if (modelPublishDataDTO.nifiCustomWorkflowId == null) {
            componentsBuild.deleteNifiFlow(dataModelVO);
        }
        AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
        if (modelPublishDataDTO.nifiCustomWorkflowId != null) {
            appNifiSettingPO = appNifiSettingService.query().eq("app_id", modelPublishDataDTO.businessAreaId).eq("nifi_custom_workflow_id", modelPublishDataDTO.nifiCustomWorkflowId).eq("type", dataClassifyEnum.getValue()).eq("del_flag", 1).one();

        } else {
            appNifiSettingPO = appNifiSettingService.query().eq("app_id", modelPublishDataDTO.businessAreaId).eq("type", dataClassifyEnum.getValue()).eq("del_flag", 1).one();

        }
        if (appNifiSettingPO != null && modelMetaDataDTO.groupComponentId == null) {
            try {
                data1 = NifiHelper.getProcessGroupsApi().getProcessGroup(appNifiSettingPO.appComponentId);
            } catch (ApiException e) {
                log.error("组查询失败" + e);
            }

        } else {
            //创建应用组

            BuildProcessGroupDTO dto = new BuildProcessGroupDTO();
            dto.name = modelPublishDataDTO.businessAreaName;
            dto.details = modelPublishDataDTO.businessAreaName;
            //根据组个数，定义坐标
            int count = 0;
            if (modelMetaDataDTO.groupComponentId != null) {
                count = componentsBuild.getGroupCount(modelMetaDataDTO.groupComponentId);
                dto.groupId = modelMetaDataDTO.groupComponentId;
            } else {
                NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName()).one();
                if (nifiConfigPO != null) {
                    dto.groupId = nifiConfigPO.componentId;
                } else {
                    BuildProcessGroupDTO buildProcessGroupDTO = new BuildProcessGroupDTO();
                    buildProcessGroupDTO.name = ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName();
                    buildProcessGroupDTO.details = ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName();
                    int groupCount = componentsBuild.getGroupCount(NifiConstants.ApiConstants.ROOT_NODE);
                    buildProcessGroupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(groupCount);
                    BusinessResult<ProcessGroupEntity> processGroupEntityBusinessResult = componentsBuild.buildProcessGroup(buildProcessGroupDTO);
                    if (processGroupEntityBusinessResult.success) {
                        dto.groupId = processGroupEntityBusinessResult.data.getId();
                        NifiConfigPO nifiConfigPO1 = new NifiConfigPO();
                        nifiConfigPO1.componentId = dto.groupId;
                        nifiConfigPO1.componentKey = ComponentIdTypeEnum.DAILY_NIFI_FLOW_GROUP_ID.getName();
                        nifiConfigService.save(nifiConfigPO1);
                    } else {
                        throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, processGroupEntityBusinessResult.msg);
                    }
                }
                count = componentsBuild.getGroupCount(dto.groupId);
            }

            dto.positionDTO = NifiPositionHelper.buildXPositionDTO(count);
            BusinessResult<ProcessGroupEntity> res = componentsBuild.buildProcessGroup(dto);
            if (res.success) {
                data1 = res.data;
            } else {
                throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res.msg);
            }

            ResultEntity<DataSourceDTO> fiDataDataSource = userClient.getFiDataDataSourceById(Integer.parseInt(dataSourceDwId));
            if (fiDataDataSource.code == ResultEnum.SUCCESS.getCode()) {
                DataSourceDTO dwData = fiDataDataSource.data;
                buildDbControllerServiceDTO.driverLocation = dwData.conType.getDriverLocation();
                buildDbControllerServiceDTO.driverName = dwData.conType.getDriverName();
                buildDbControllerServiceDTO.conUrl = dwData.conStr;
                buildDbControllerServiceDTO.pwd = dwData.conPassword;
                buildDbControllerServiceDTO.user = dwData.conAccount;
            } else {
                log.error("userclient无法查询到dw库的连接信息");
                throw new FkException(ResultEnum.ERROR);
            }

            buildDbControllerServiceDTO.name = businessAreaName;
            buildDbControllerServiceDTO.enabled = true;
       /*     if(modelMetaDataDTO.groupStructureId!=null){
                buildDbControllerServiceDTO.groupId = modelMetaDataDTO.groupStructureId;
            }else{
                buildDbControllerServiceDTO.groupId = data1.getId();
            }*/
            buildDbControllerServiceDTO.groupId = NifiConstants.ApiConstants.ROOT_NODE;
            buildDbControllerServiceDTO.details = businessAreaName;

            NifiConfigPO nifiConfigPO = nifiConfigService.query().eq("component_key", ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID.getName()).one();
            if (nifiConfigPO != null) {
                data.setId(nifiConfigPO.componentId);
            } else {
                BusinessResult<ControllerServiceEntity> controllerServiceEntityBusinessResult = componentsBuild.buildDbControllerService(buildDbControllerServiceDTO);
                if (controllerServiceEntityBusinessResult.success) {
                    data = controllerServiceEntityBusinessResult.data;
                    NifiConfigPO SaveNifiConfigPO = new NifiConfigPO();
                    SaveNifiConfigPO.componentId = controllerServiceEntityBusinessResult.data.getId();
                    SaveNifiConfigPO.componentKey = ComponentIdTypeEnum.PG_DW_DB_POOL_COMPONENT_ID.getName();
                    nifiConfigService.save(SaveNifiConfigPO);
                } else {
                    throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, controllerServiceEntityBusinessResult.msg);
                }
            }

            // TODO: 创建input组件功能(第一层应用)
            ProcessGroupEntity processGroupData1 = null;
            try {
                processGroupData1 = NifiHelper.getProcessGroupsApi().getProcessGroup(data1.getId());

            } catch (ApiException e) {
                log.error(e.getMessage());
            }
            assert processGroupData1 != null;
            appParentGroupId = processGroupData1.getComponent().getParentGroupId();

//            appInputPortId = buildPortComponent(processGroupData1.getComponent().getName(), appParentGroupId, processGroupData1.getPosition().getX(),
//                    processGroupData1.getPosition().getY(), PortComponentEnum.APP_INPUT_PORT_COMPONENT);

            // TODO: 创建output组件功能(第一层应用)
//            appOutputPortId = buildPortComponent(processGroupData1.getComponent().getName(), appParentGroupId, processGroupData1.getPosition().getX(),
//                    processGroupData1.getPosition().getY(), PortComponentEnum.APP_OUTPUT_PORT_COMPONENT);

        }

        /*TableNifiSettingPO tableNifiSettingPO = tableNifiSettingService.query().eq("app_id", modelMetaDataDTO.appId).eq("table_access_id", modelMetaDataDTO.id).eq("type", olapTableEnum.getValue()).one();
        if(tableNifiSettingPO!=null){
           data2.setId(tableNifiSettingPO.tableComponentId);
        }else{*/
        //创建任务组
        BuildProcessGroupDTO buildProcessGroupDTO = new BuildProcessGroupDTO();
        buildProcessGroupDTO.name = modelMetaDataDTO.tableName;
        buildProcessGroupDTO.details = modelMetaDataDTO.tableName;
        buildProcessGroupDTO.groupId = data1.getId();
        //根据组个数，定义坐标
        int count1 = componentsBuild.getGroupCount(data1.getId());
        buildProcessGroupDTO.positionDTO = NifiPositionHelper.buildXPositionDTO(count1);
        //创建组
        BusinessResult<ProcessGroupEntity> res1 = componentsBuild.buildProcessGroup(buildProcessGroupDTO);
        if (res1.success) {
            data2 = res1.data;
        } else {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, res1.msg);
        }

        // TODO: 创建input组件功能(第二层表)
        ProcessGroupEntity processGroupData2 = null;
        try {
            processGroupData2 = NifiHelper.getProcessGroupsApi().getProcessGroup(data2.getId());

        } catch (ApiException e) {
            log.error(e.getMessage());
        }
        assert processGroupData2 != null;
        appParentGroupId = processGroupData2.getComponent().getParentGroupId();

        // TODO: 创建output组件功能(第二层表)
        // 创建input_port(任务)   (后期入库)
        tableInputPortId = buildPortComponent(processGroupData2.getComponent().getName(), appParentGroupId,
                processGroupData2.getPosition().getX(), processGroupData2.getPosition().getY(), PortComponentEnum.TASK_INPUT_PORT_COMPONENT);
        // 创建output_port(任务)   (后期入库)
        tableOutputPortId = buildPortComponent(processGroupData2.getComponent().getName(), appParentGroupId,
                processGroupData2.getPosition().getX(), processGroupData2.getPosition().getY(), PortComponentEnum.TASK_OUTPUT_PORT_COMPONENT);

        //}

        //创建组件,启动组件
        TableNifiSettingPO tableNifiSetting = new TableNifiSettingPO();
        String Componentid = createComponents(modelPublishDataDTO, data2.getId(), data.getId(), "update" + modelMetaDataDTO.tableName + "()", data1, tableNifiSetting);

        //回写
        savaNifiAllSetting(modelPublishDataDTO, data, data1, data2, Componentid, modelMetaDataDTO, dataClassifyEnum, olapTableEnum, tableNifiSetting);

    }

    public void savaNifiAllSetting(ModelPublishDataDTO ModelPublishDataDTO, ControllerServiceEntity controllerServiceEntity, ProcessGroupEntity processGroupEntity1, ProcessGroupEntity processGroupEntity2, String Componentid, ModelPublishTableDTO modelMetaDataDTO, DataClassifyEnum dataClassifyEnum, OlapTableEnum olapTableEnum, TableNifiSettingPO tableNifiSettingPO) {
        AppNifiSettingPO appNifiSettingPO = new AppNifiSettingPO();
        AppNifiSettingPO appNifiSettingPO1 = new AppNifiSettingPO();
        if (ModelPublishDataDTO.nifiCustomWorkflowId == null) {
            appNifiSettingPO1 = appNifiSettingService.query().eq("app_id", ModelPublishDataDTO.businessAreaId).eq("type", dataClassifyEnum.getValue()).eq("del_flag", 1).one();
        } else {
            appNifiSettingPO1 = appNifiSettingService.query().eq("app_id", ModelPublishDataDTO.businessAreaId).eq("nifi_custom_workflow_id", ModelPublishDataDTO.nifiCustomWorkflowId).eq("type", dataClassifyEnum.getValue()).eq("del_flag", 1).one();
        }
        if (appNifiSettingPO1 != null) {
            appNifiSettingPO = appNifiSettingPO1;
        }

        appNifiSettingPO.appId = String.valueOf(ModelPublishDataDTO.businessAreaId);
        appNifiSettingPO.type = dataClassifyEnum.getValue();
        //做判断,是否新增
        appNifiSettingPO.appComponentId = processGroupEntity1.getId();
        appNifiSettingPO.nifiCustomWorkflowId = ModelPublishDataDTO.nifiCustomWorkflowId;
        appNifiSettingService.saveOrUpdate(appNifiSettingPO);
        Map<String, Object> queryCondition = new HashMap<>();
        queryCondition.put("app_id", ModelPublishDataDTO.businessAreaId);
        queryCondition.put("table_access_id", modelMetaDataDTO.tableId);
        queryCondition.put("type", olapTableEnum.getValue());
        if (modelMetaDataDTO.nifiCustomWorkflowDetailId != null && !Objects.equals(modelMetaDataDTO.nifiCustomWorkflowDetailId, "null")) {
            queryCondition.put("nifi_custom_workflow_detail_id", modelMetaDataDTO.nifiCustomWorkflowDetailId);
            tableNifiSettingPO.nifiCustomWorkflowDetailId = modelMetaDataDTO.nifiCustomWorkflowDetailId;
        }
        tableNifiSettingService.removeByMap(queryCondition);
        tableNifiSettingPO.tableAccessId = Math.toIntExact(modelMetaDataDTO.tableId);
        tableNifiSettingPO.tableName = modelMetaDataDTO.tableName;
        tableNifiSettingPO.appId = Math.toIntExact(ModelPublishDataDTO.businessAreaId);
        tableNifiSettingPO.selectSql = "call update" + modelMetaDataDTO.tableName + "()";
        tableNifiSettingPO.queryIncrementProcessorId = Componentid;
        tableNifiSettingPO.tableComponentId = processGroupEntity2.getId();
        tableNifiSettingPO.type = olapTableEnum.getValue();
        tableNifiSettingService.saveOrUpdate(tableNifiSettingPO);
    }

    /*
     * 创建组件
     * */
    public String createComponents(ModelPublishDataDTO ModelPublishDataDTO, String groupId, String componentId, String executsql, ProcessGroupEntity data1, TableNifiSettingPO tableNifiSetting) {
        List<ProcessorEntity> processors = new ArrayList<>();
        BuildCallDbProcedureProcessorDTO callDbProcedureProcessorDTO = new BuildCallDbProcedureProcessorDTO();
        callDbProcedureProcessorDTO.name = "CallDbProcedure";
        callDbProcedureProcessorDTO.details = "CallDbProcedure";
        callDbProcedureProcessorDTO.groupId = groupId;
        callDbProcedureProcessorDTO.dbConnectionId = componentId;
        callDbProcedureProcessorDTO.executsql = "call " + executsql;
        callDbProcedureProcessorDTO.haveNextOne = false;
        callDbProcedureProcessorDTO.positionDTO = NifiPositionHelper.buildYPositionDTO(1);
        BusinessResult<ProcessorEntity> processorEntityBusinessResult = componentsBuild.buildCallDbProcedureProcess(callDbProcedureProcessorDTO);
        if (!processorEntityBusinessResult.success) {
            throw new FkException(ResultEnum.TASK_NIFI_BUILD_COMPONENTS_ERROR, processorEntityBusinessResult.msg);
        }
        log.info("组件id为:" + processorEntityBusinessResult.data.getId());
        processors.add(processorEntityBusinessResult.data);
        if (ModelPublishDataDTO.nifiCustomWorkflowId != null) {
            List<ProcessorEntity> processorEntities = componentsBuild.enabledProcessor(groupId, processors);
        }

        ProcessorEntity processor = processorEntityBusinessResult.data;

        appGroupId = data1.getComponent().getParentGroupId();

        // TODO 创建input_port(组)   (后期入库)
        String inputPortId = buildPortComponent(processor.getComponent().getName(), processor.getComponent().getParentGroupId(),
                processor.getPosition().getX(), processor.getPosition().getY(), PortComponentEnum.COMPONENT_INPUT_PORT_COMPONENT);

        // TODO 创建output_port(组)   (后期入库)
        String outputPortId = buildPortComponent(processor.getComponent().getName(), processor.getComponent().getParentGroupId(),
                processor.getPosition().getX(), processor.getPosition().getY(), PortComponentEnum.COMPONENT_OUTPUT_PORT_COMPONENT);


        // ===============================================================================================================
        // TODO 创建input_port connection(组)
        String componentInputPortConnectionId = buildPortConnection(processor.getComponent().getParentGroupId(),
                processor.getComponent().getParentGroupId(), processor.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                processor.getComponent().getParentGroupId(), inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                0, PortComponentEnum.COMPONENT_INPUT_PORT_CONNECTION);

        // TODO 创建output_port connection(组)
        String componentOutputPortConnectionId = buildPortConnection(processor.getComponent().getParentGroupId(),
                processor.getComponent().getParentGroupId(), outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                processor.getComponent().getParentGroupId(), processor.getId(), ConnectableDTO.TypeEnum.PROCESSOR,
                3, PortComponentEnum.COMPONENT_OUTPUT_PORT_CONNECTION);

        // TODO 创建input_port connection(任务)
        String taskInputPortConnectionId = buildPortConnection(data1.getId(),
                groupId, inputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                data1.getId(), tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
                0, PortComponentEnum.TASK_INPUT_PORT_CONNECTION);

        // TODO 创建output connection(任务)
        String taskOutputPortConnectionId = buildPortConnection(data1.getId(),
                data1.getId(), tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                groupId, outputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
                2, PortComponentEnum.TASK_OUTPUT_PORT_CONNECTION);

        // TODO 创建input_port connection(应用)
//        String appInputPortConnectionId = buildPortConnection(appGroupId,
//                data1.getId(), tableInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
//                appGroupId, appInputPortId, ConnectableDTO.TypeEnum.INPUT_PORT,
//                0, PortComponentEnum.APP_INPUT_PORT_CONNECTION);

        // TODO 创建output connection(应用)
//        String appOutputPortConnectionId = buildPortConnection(appGroupId,
//                appGroupId, appOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
//                data1.getId(), tableOutputPortId, ConnectableDTO.TypeEnum.OUTPUT_PORT,
//                1, PortComponentEnum.APP_OUTPUT_PORT_CONNECTION);

        tableNifiSetting.processorInputPortConnectId = componentInputPortConnectionId;
        tableNifiSetting.processorOutputPortConnectId = componentOutputPortConnectionId;
        tableNifiSetting.tableInputPortConnectId = taskInputPortConnectionId;
        tableNifiSetting.tableOutputPortConnectId = taskOutputPortConnectionId;
        tableNifiSetting.tableInputPortId = tableInputPortId;
        tableNifiSetting.tableOutputPortId = tableOutputPortId;
        tableNifiSetting.processorInputPortId = inputPortId;
        tableNifiSetting.processorOutputPortId = outputPortId;
        return processorEntityBusinessResult.data.getId();
    }


    /**
     * 创建input_port/output_port组件
     *
     * @param portName    组件名称
     * @param componentId 上级id
     * @param componentX  坐标
     * @param componentY  坐标
     * @param typeEnum    组件类型
     * @return 生成的组件id
     */
    private String buildPortComponent(String portName, String componentId, Double componentX, Double componentY, PortComponentEnum typeEnum) {

        BuildPortDTO buildPortDTO = new BuildPortDTO();
        PortEntity portEntity;

        switch (typeEnum.getValue()) {
            // 创建input_port(应用)
            case 0:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_APP_SUFFIX;
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.INPUT_PORT_Y / 2 + NifiConstants.PortConstants.INPUT_PORT_OFFSET_Y;
                portEntity = componentsBuild.buildInputPort(buildPortDTO);
                return portEntity.getId();
            // 创建output_port(应用)
            case 1:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_APP_SUFFIX;
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.OUTPUT_PORT_Y;
                portEntity = componentsBuild.buildOutputPort(buildPortDTO);
                return portEntity.getId();
            // 创建input_port(任务)
            case 2:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_TABLE_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX / NifiConstants.AttrConstants.POSITION_X;
                buildPortDTO.componentY = componentY / NifiConstants.AttrConstants.POSITION_X + NifiConstants.PortConstants.INPUT_PORT_Y;
                portEntity = componentsBuild.buildInputPort(buildPortDTO);
                return portEntity.getId();
            // 创建output_port(任务)
            case 3:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_TABLE_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX / NifiConstants.AttrConstants.POSITION_X;
                buildPortDTO.componentY = componentY / NifiConstants.AttrConstants.POSITION_X + NifiConstants.PortConstants.OUTPUT_PORT_Y;
                portEntity = componentsBuild.buildOutputPort(buildPortDTO);
                return portEntity.getId();
            // 创建input_port(组)
            case 4:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_FIELD_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.INPUT_PORT_Y;
                portEntity = componentsBuild.buildInputPort(buildPortDTO);
                return portEntity.getId();
            // 创建output_port(组)
            case 5:
                buildPortDTO.portName = portName + NifiConstants.PortConstants.PORT_NAME_FIELD_SUFFIX + new Date().getTime();
                buildPortDTO.componentId = componentId;
                buildPortDTO.componentX = componentX;
                buildPortDTO.componentY = componentY + NifiConstants.PortConstants.OUTPUT_PORT_Y;
                portEntity = componentsBuild.buildOutputPort(buildPortDTO);
                return portEntity.getId();
            default:
                break;
        }
        return null;
    }

    /**
     * 创建input_port/output_port connections
     *
     * @param fatherComponentId   当前组件父id
     * @param destinationGroupId  destinationGroupId
     * @param destinationId       destinationId
     * @param destinationTypeEnum destinationTypeEnum
     * @param sourceGroupId       sourceGroupId
     * @param sourceId            sourceId
     * @param sourceTypeEnum      sourceTypeEnum
     * @param level               level
     * @param typeEnum            typeEnum
     * @return connection id
     */
    private String buildPortConnection(String fatherComponentId, String destinationGroupId, String destinationId, ConnectableDTO.TypeEnum destinationTypeEnum,
                                       String sourceGroupId, String sourceId, ConnectableDTO.TypeEnum sourceTypeEnum, int level, PortComponentEnum typeEnum) {
        BuildConnectDTO buildConnectDTO = new BuildConnectDTO();
        NifiConnectDTO destination = new NifiConnectDTO();
        NifiConnectDTO source = new NifiConnectDTO();
        ConnectionEntity connectionEntity;
        switch (typeEnum.getValue()) {
            // 创建input_port连接(应用)
            case 6:
                // 创建input_port连接(任务)
            case 8:
                // 创建input_port连接(组)
            case 10:
                buildConnectDTO.fatherComponentId = fatherComponentId;
                destination.groupId = destinationGroupId;
                destination.id = destinationId;
                destination.typeEnum = destinationTypeEnum;
                source.groupId = sourceGroupId;
                source.id = sourceId;
                source.typeEnum = sourceTypeEnum;
                buildConnectDTO.destination = destination;
                buildConnectDTO.source = source;
                connectionEntity = componentsBuild.buildInputPortConnections(buildConnectDTO);
                return connectionEntity.getId();
            // 创建output_port连接(应用)
            case 7:
                // 创建output_port连接(任务)
            case 9:
                // 创建output_port连接(组)
            case 11:
                buildConnectDTO.fatherComponentId = fatherComponentId;
                destination.groupId = destinationGroupId;
                destination.id = destinationId;
                destination.typeEnum = destinationTypeEnum;
                source.groupId = sourceGroupId;
                source.id = sourceId;
                source.typeEnum = sourceTypeEnum;
                buildConnectDTO.level = level;
                buildConnectDTO.destination = destination;
                buildConnectDTO.source = source;
                connectionEntity = componentsBuild.buildOutPortPortConnections(buildConnectDTO);
                return connectionEntity.getId();
            default:
                break;
        }
        return null;
    }

}
