package com.fisk.datamodel.service.impl.dimension;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.datamodel.BuildDataModelHelper;
import com.fisk.common.service.dbBEBuild.datamodel.IBuildDataModelSqlCommand;
import com.fisk.common.service.dimensionquerysql.IBuildDimensionQuerySql;
import com.fisk.common.service.dimensionquerysql.impl.DimensionQuerySqlHelper;
import com.fisk.common.service.metadata.dto.metadata.*;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.check.CheckPhyDimFactTableIfExistsDTO;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.dto.DimensionIfEndDTO;
import com.fisk.datamodel.dto.dimension.*;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDataDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeListDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionMetaDTO;
import com.fisk.datamodel.dto.dimensionfolder.DimensionFolderPublishQueryDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.dimension.DimensionAttributePO;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import com.fisk.datamodel.entity.fact.FactPO;
import com.fisk.datamodel.enums.*;
import com.fisk.datamodel.map.dimension.DimensionMap;
import com.fisk.datamodel.mapper.dimension.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.dimension.DimensionMapper;
import com.fisk.datamodel.mapper.fact.FactAttributeMapper;
import com.fisk.datamodel.service.IDimension;
import com.fisk.datamodel.service.impl.BusinessAreaImpl;
import com.fisk.datamodel.service.impl.SystemVariablesImpl;
import com.fisk.datamodel.service.impl.fact.FactImpl;
import com.fisk.datamodel.utils.mysql.DataSourceConfigUtil;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.pgsql.PgsqlDelTableDTO;
import com.fisk.task.dto.pgsql.TableListDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
//@Lazy
public class DimensionImpl
        extends ServiceImpl<DimensionMapper, DimensionPO>
        implements IDimension {

    @Value("${fiData-data-date-dw-source}")
    private Integer dateDwSourceId;

    @Resource
    DimensionMapper mapper;
    @Resource
    private IDimension dimension;
    @Resource
    private DimensionAttributeImpl dimensionAttribute;
    @Resource
    private BusinessAreaImpl businessArea;
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    DimensionAttributeImpl dimensionAttributeImpl;
    @Resource
    BusinessAreaImpl businessAreaImpl;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    private DataFactoryClient dataFactoryClient;
    @Resource
    UserHelper userHelper;
    @Resource
    UserClient userClient;
    @Resource
    DataManageClient dataManageClient;
    @Resource
    private DimensionFolderImpl dimensionFolder;

    @Resource
    DataSourceConfigUtil dataSourceConfigUtil;
    @Resource
    SystemVariablesImpl systemVariables;
    @Resource
    private FactImpl factImpl;
    @Value("${fiData-data-dw-source}")
    private Integer dwSource;

    @Value("${spring.open-metadata}")
    private Boolean openMetadata;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addDimension(DimensionDTO dto) {
        //检测当前要插入的维度表是否已存在于数据库中
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getDimensionTabName, dto.dimensionTabName);
        DimensionPO po = mapper.selectOne(queryWrapper);
        if (po != null) {
            return ResultEnum.DIMENSION_EXIST;
        }

        //为要插入的维度表设置发布状态
        dto.isPublish = PublicStatusEnum.UN_PUBLIC.getValue();

        //dtp --> po
        DimensionPO model = DimensionMap.INSTANCES.dtoToPo(dto);

        //判断是否为生成时间维度表
        if (dto.timeTable) {

            DataSourceTypeEnum conType = getTargetDbInfo(dwSource).conType;
            IBuildDimensionQuerySql dimensionQuerySqlHelper = DimensionQuerySqlHelper.getDimensionQuerySqlHelperByConType(conType);
            String sql = dimensionQuerySqlHelper.buildDimensionQuerySql(dto.startTime, dto.endTime);

            model.setSqlScript(sql);
            //标识此表为时间表
            model.setIsDimDateTbl(true);

//             时间维度表不再直接修改发布状态
//             model.isPublish = PublicStatusEnum.PUBLIC_SUCCESS.getValue();

            //2023-05-04 李世纪注释，目前日期维度表和普通表走相同流程，不再通过JDBC直接建表和插入数据
            // 在ods库下生成数据源表，用于nifi发布流程后查找数据使用
//            editDateDimension(dto, dto.dimensionTabName);
        } else {
            model.setIsDimDateTbl(false);
        }
        // 设置临时表名称
        model.setPrefixTempName(PrefixTempNameEnum.DIMENSION_TEMP_NAME.getName());
        int flat = mapper.insert(model);
//        if (flat > 0 && dto.timeTable) {
//            return addTimeTableAttribute(dto);
//        }
        return flat > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 编辑时间维度表
     *
     * @param dto
     * @param oldTimeTable
     */
    public void editDateDimension(DimensionDTO dto, String oldTimeTable) {
        Connection conn = null;
        Statement stat = null;
        try {
            conn = getConnect();
            stat = conn.createStatement();
            if (!dto.dimensionTabName.equals(oldTimeTable)) {
                //删除表
                boolean execute = stat.execute("drop table " + oldTimeTable);
                if (execute) {
                    throw new FkException(ResultEnum.DELETE_ERROR);
                }
            }
            //获取数据库表名   sqlServer   dmp_ods库是否有重复的表名
            ResultSet rs = conn.getMetaData().getTables(null, null, dto.dimensionTabName, null);
            //如果表名重复，抛出表已存在异常
            if (rs.next()) {
                throw new FkException(ResultEnum.TABLE_IS_EXIST);
            }

            //执行建表语句
            int flat = stat.executeUpdate(buildTableSql(dto.dimensionTabName));
            //如果成功建表
            if (flat >= 0) {
                int ifEnd = 701;
                boolean ifGo = true;
                String startTime = dto.startTime;
                while (ifGo) {
                    DimensionIfEndDTO strsql = insertTableDataSql(dto.dimensionTabName, startTime, dto.endTime, ifEnd);
//                log.info("sql语句：{}", strSql);
                    ifEnd = strsql.ifGoOn;
                    stat.addBatch(strsql.result);
                    stat.executeBatch();
                    startTime = strsql.goOnTime;
                    if (strsql.ifGoOn == 0) {
                        ifGo = false;
                    }
                }
            }
        } catch (Exception e) {
            log.error("editDateDimension ex:", e);
            throw new FkException(ResultEnum.VISUAL_CONNECTION_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(stat);
            AbstractCommonDbHelper.closeConnection(conn);
        }
    }

    private Connection getConnect() {
        ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(dateDwSourceId);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        DataSourceDTO dwSource = dataSourceConfig.data;
        AbstractCommonDbHelper commonDbHelper = new AbstractCommonDbHelper();
        return commonDbHelper.connection(dwSource.conStr, dwSource.conAccount, dwSource.conPassword, dwSource.conType);

    }

    /**
     * 拼接创表语句
     *
     * @param dimensionTabName
     * @return
     */
    public String buildTableSql(String dimensionTabName) {
        //获取ods数据源配置信息   主要是连接类型
        DataSourceDTO dwSource = dataSourceConfigUtil.getDwSource();
        //通过连接类型，决定使用oracle或sqlServer还是mysql的创表语句实现类
        IBuildDataModelSqlCommand command = BuildDataModelHelper.getDBCommand(dwSource.conType);
        return command.buildTimeDimensionCreateTable(dimensionTabName);
    }

    /**
     * 拼接insert语句
     *
     * @param tableName
     * @param startTime
     * @param endTime
     * @return
     */
    public DimensionIfEndDTO insertTableDataSql(String tableName, String startTime, String endTime, Integer ifEnd) {
        StringBuilder str = new StringBuilder();
        //使用默认时区和语言环境获得一个日历
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        Date date;
        try {
            //将页面传过来的开始日期转换为yyyy-MM-dd的格式
            date = sdf.parse(startTime);
        } catch (ParseException e) {
            log.error("insertTableDataSql ex:", e);
            return null;
        }
        //将页面传过来的开始时间作为此日历的时间
        calendar.setTime(date);
        // 设置每一周的第一天是哪一天 即周一作为每一周的第一天
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        //日期减1
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        boolean flat = true;
        String[] monthName = {"January", "February",
                "March", "April", "May", "June", "July",
                "August", "September", "October", "November",
                "December"};
        int i = 0;
        String goOnTime = "";
        //预拼接插入语句
        str.append("insert into " + tableName);
        while (flat) {
            //日期加1
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            //获取此日历当前时间值的Date对象
            Date dt1 = calendar.getTime();
            //将dt1转为yyyy-MM-dd的格式
            String reStr = sdf.format(dt1);
            goOnTime = reStr;
            //此判断用于退出循环:退出条件，如果当前要插入的时间和结束日期一致，就认为插入完毕，不再进行循环--插入数据的操作
            if (endTime.equals(reStr) || ifEnd % 700 == 0) {
                if (endTime.equals(reStr)) {
                    flat = false;
                    ifEnd = -1;
                } else {
                    flat = false;
                    ifEnd = 700;
                }

            }
            //获取星期几
            int dayNumberOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            dayNumberOfWeek = dayNumberOfWeek - 1;
            if (dayNumberOfWeek == 0) {
                dayNumberOfWeek = 7;
            }
            //获取星期名称
            String englishDayNameOfWeek = getEnglishDayNameOfWeek(dayNumberOfWeek);
            //几号
            int dayNumberOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            //一年中第几天
            int dayNumberOfYear = calendar.get(Calendar.DAY_OF_YEAR);
            //第几周
            int weekNumberOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
            //月份名称
            String englishMonthName = monthName[calendar.get(Calendar.MONTH)];
            //第几月
            int monthNumberOfYear = calendar.get(Calendar.MONTH) + 1;
            //季度
            int calendarQuarter = getCurrentMonth(calendar.get(Calendar.MONTH) + 1);
            //季度年
            int calendarYear = calendar.get(Calendar.YEAR);
            //FullDateDay
            String fullDateStr = sdf2.format(dt1);
            // 是否是工作日
            int weekDay = dayNumberOfWeek == 6 || dayNumberOfWeek == 7 ? 0 : 1;
            if (i == 0) {
                str.append(" values('" + reStr + "',"
                        + dayNumberOfWeek + ",'"
                        + englishDayNameOfWeek + "',"
                        + dayNumberOfMonth + ","
                        + dayNumberOfYear + ","
                        + weekNumberOfYear + ",'"
                        + englishMonthName + "',"
                        + monthNumberOfYear + ","
                        + calendarQuarter + ","
                        + calendarYear + ",'"
                        + fullDateStr + "',"
                        + weekDay + ")"
                );
            } else {
                str.append(",('" + reStr + "',"
                        + dayNumberOfWeek + ",'"
                        + englishDayNameOfWeek + "',"
                        + dayNumberOfMonth + ","
                        + dayNumberOfYear + ","
                        + weekNumberOfYear + ",'"
                        + englishMonthName + "',"
                        + monthNumberOfYear + ","
                        + calendarQuarter + ","
                        + calendarYear + ",'"
                        + fullDateStr + "',"
                        + weekDay + ")"
                );
            }
            i += 1;
            ifEnd += 1;
        }
        DimensionIfEndDTO endDTO = new DimensionIfEndDTO();
        endDTO.ifGoOn = ifEnd;
        endDTO.result = String.valueOf(str);
        endDTO.setGoOnTime(goOnTime);
        return endDTO;
    }

    /**
     * 获取星期名称
     *
     * @param dayNumberOfWeek
     * @return
     */
    public String getEnglishDayNameOfWeek(int dayNumberOfWeek) {
        String name = "";
        switch (dayNumberOfWeek) {
            case 1:
                name = "Monday";
                break;
            case 2:
                name = "Tuesday";
                break;
            case 3:
                name = "Wednesday";
                break;
            case 4:
                name = "Thursday";
                break;
            case 5:
                name = "Friday";
                break;
            case 6:
                name = "Saturday";
                break;
            case 7:
                name = "Sunday";
            default:
                break;
        }
        return name;
    }

    /**
     * 获取季度
     *
     * @param currentMonth
     * @return
     */
    public int getCurrentMonth(int currentMonth) {
        int dt = 0;
        if (currentMonth >= Calendar.JANUARY && currentMonth <= Calendar.MARCH) {
            dt = 1;
        } else if (currentMonth >= Calendar.APRIL && currentMonth <= Calendar.JUNE) {
            dt = 2;
        } else if (currentMonth >= Calendar.JULY && currentMonth <= Calendar.SEPTEMBER) {
            dt = 3;
        } else if (currentMonth >= Calendar.OCTOBER && currentMonth <= Calendar.DECEMBER) {
            dt = 4;
        }
        return dt;
    }

    public ResultEnum addTimeTableAttribute(DimensionDTO dto) {
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionPO::getDimensionTabName, dto.dimensionTabName)
                .eq(DimensionPO::getBusinessId, dto.businessId);
        DimensionPO po = mapper.selectOne(queryWrapper);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        String[] columnList = {"FullDateAlternateKey", "DayNumberOfWeek", "EnglishDayNameOfWeek", "DayNumberOfMonth",
                "DayNumberOfYear", "WeekNumberOfYear", "EnglishMonthName", "MonthNumberOfYear",
                "CalendarQuarter", "CalendarYear", "FullDateKey", "Is_WeekDay"};
        String[] columnDataTypeList = {"DATE", "INT", "VARCHAR", "INT", "INT", "INT", "VARCHAR", "INT", "INT", "INT", "DATE", "INT"};
        String[] columnDataTypeLengthList = {"0", "0", "10", "0", "0", "0", "10", "0", "0", "0", "0", "0"};
        DataSourceDTO odsSource = dataSourceConfigUtil.getDwSource();
        switch (odsSource.conType) {
            case MYSQL:
                break;
            case ORACLE:
                columnDataTypeList = new String[]{"DATE", "NUMBER", "VARCHAR2", "NUMBER", "NUMBER", "NUMBER",
                        "VARCHAR2", "NUMBER", "NUMBER", "NUMBER", "DATE", "NUMBER"};
                break;
            case SQLSERVER:
                columnDataTypeList = new String[]{"DATE", "INT", "VARCHAR", "INT", "INT", "INT", "VARCHAR", "INT", "INT", "INT", "DATE", "INT"};
                break;
            case POSTGRESQL:
                columnDataTypeList = new String[]{"DATE", "INT2", "VARCHAR", "INT2", "INT2", "INT2", "VARCHAR", "INT2", "INT2", "INT2", "DATE", "INT2"};
                break;
            case DORIS:
                break;
            default:
        }
        List<DimensionAttributeDTO> list = new ArrayList<>();
        for (int i = 0; i < columnList.length; i++) {
            DimensionAttributeDTO attributeDTO = new DimensionAttributeDTO();
            attributeDTO.attributeType = DimensionAttributeEnum.BUSINESS_KEY.getValue();
            attributeDTO.dimensionFieldCnName = columnList[i];
            attributeDTO.dimensionFieldEnName = columnList[i];
            attributeDTO.dimensionFieldType = columnDataTypeList[i];
            attributeDTO.dimensionFieldLength = Integer.parseInt(columnDataTypeLengthList[i]);
            list.add(attributeDTO);
        }
        ResultEnum result = dimensionAttributeImpl.addTimeTableAttribute(list, (int) po.id);
        if (result.getCode() == ResultEnum.SUCCESS.getCode() && openMetadata) {
            //同步到atlas
            synchronousMetadata(DataSourceConfigEnum.DMP_DW.getValue(), po, DataModelTableTypeEnum.DW_DIMENSION.getValue());
        }
        return result;
    }

    @Override
    public ResultEnum updateDimension(DimensionDTO dto) {
        DimensionPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getDimensionTabName, dto.dimensionTabName);
        DimensionPO po = mapper.selectOne(queryWrapper);
        if (po != null && po.id != model.id) {
            return ResultEnum.DIMENSION_EXIST;
        }
        if (model.timeTable && (!model.startTime.equals(dto.startTime)
                || !model.startTime.equals(dto.startTime)
                || !model.dimensionTabName.equals(dto.dimensionTabName))) {
            editDateDimension(dto, model.dimensionTabName);
        }
        dto.businessId = model.businessId;
        //model = DimensionMap.INSTANCES.dtoToPo(dto);
        model.dimensionCnName = dto.dimensionCnName;
        model.dimensionTabName = dto.dimensionTabName;
        model.dimensionDesc = dto.dimensionDesc;
        model.dimensionFolderId = dto.dimensionFolderId;
        int flat = mapper.updateById(model);
        if (flat > 0 && dto.timeTable && openMetadata) {
            //同步atlas
            DimensionPO dimensionPo = mapper.selectById(dto.id);
            if (dimensionPo != null) {
                synchronousMetadata(DataSourceConfigEnum.DMP_DW.getValue(), dimensionPo, DataModelTableTypeEnum.DW_DIMENSION.getValue());
            }
        }

        //同步元数据
        if (model.isPublish == PublicStatusEnum.PUBLIC_SUCCESS.getValue() && openMetadata) {
            asyncSynchronousMetadata(model);
        }

        return flat > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    public void asyncSynchronousMetadata(DimensionPO model) {
        synchronousMetadata(DataSourceConfigEnum.DMP_DW.getValue(), model, DataModelTableTypeEnum.DW_DIMENSION.getValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteDimension(int id) {
        try {
            // 删除之前检查该维度表是否已经被配置到存在的管道里面：
            // 方式：检查配置库-dmp_factory_db库 tb_nifi_custom_workflow_detail表内是否存在该维度表，
            // 如果存在则不允许删除，给出提示并告知该表被配置到哪个管道里面    tips:数仓建模的维度表对应的table type是4  数仓表任务-数仓维度表任务
            CheckPhyDimFactTableIfExistsDTO checkDto = new CheckPhyDimFactTableIfExistsDTO();
            checkDto.setTblId((long) id);
            checkDto.setChannelDataEnum(ChannelDataEnum.getName(4));
            ResultEntity<List<NifiCustomWorkflowDetailDTO>> booleanResultEntity = dataFactoryClient.checkPhyTableIfExists(checkDto);
            if (booleanResultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
                return ResultEnum.DISPATCH_REMOTE_ERROR;
            }
            List<NifiCustomWorkflowDetailDTO> data = booleanResultEntity.getData();
            if (!CollectionUtils.isEmpty(data)) {
                //这里的getWorkflowId 已经被替换为 workflowName
                List<String> collect = data.stream().map(NifiCustomWorkflowDetailDTO::getWorkflowId).collect(Collectors.toList());
                log.info("当前要删除的表存在于以下管道中：" + collect);
                return ResultEnum.ACCESS_PHYTABLE_EXISTS_IN_DISPATCH;
            }

            DimensionPO model = mapper.selectById(id);
            if (model == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }

            BusinessAreaPO businessArea = businessAreaImpl.getById(model.businessId);
            if (businessArea == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            //判断维度表是否存在关联
            QueryWrapper<DimensionAttributePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(DimensionAttributePO::getAssociateDimensionId, id);
            List<DimensionAttributePO> poList = dimensionAttributeMapper.selectList(queryWrapper);
            if (!CollectionUtils.isEmpty(poList)) {
                return ResultEnum.TABLE_ASSOCIATED;
            }
            //判断维度表是否与事实表有关联
            QueryWrapper<FactAttributePO> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.lambda().eq(FactAttributePO::getAssociateDimensionId, id);
            List<FactAttributePO> factAttributePoList = factAttributeMapper.selectList(queryWrapper1);
            if (!CollectionUtils.isEmpty(factAttributePoList)) {
                return ResultEnum.TABLE_ASSOCIATED;
            }
            //删除维度字段数据
            QueryWrapper<DimensionAttributePO> attributePoQueryWrapper = new QueryWrapper<>();
            attributePoQueryWrapper.select("id").lambda().eq(DimensionAttributePO::getDimensionId, id);
            List<Integer> dimensionAttributeIds = (List) dimensionAttributeMapper.selectObjs(attributePoQueryWrapper);
            if (!CollectionUtils.isEmpty(dimensionAttributeIds)) {
                ResultEnum resultEnum = dimensionAttributeImpl.deleteDimensionAttribute(dimensionAttributeIds);
                if (ResultEnum.SUCCESS != resultEnum) {
                    throw new FkException(resultEnum);
                }
            }
            //拼接删除niFi参数
            DataModelVO vo = niFiDelProcess(model.businessId, id);
            publishTaskClient.deleteNifiFlow(vo);
            //拼接删除DW/Doris库中维度表
            PgsqlDelTableDTO dto = delDwDorisTable(model.dimensionTabName);
            publishTaskClient.publishBuildDeletePgsqlTableTask(dto);

            // 删除factory-dispatch对应的表配置
            List<DeleteTableDetailDTO> list = new ArrayList<>();
            DeleteTableDetailDTO deleteTableDetailDto = new DeleteTableDetailDTO();
            deleteTableDetailDto.appId = String.valueOf(model.businessId);
            deleteTableDetailDto.tableId = String.valueOf(id);
            // 数仓维度
            deleteTableDetailDto.channelDataEnum = ChannelDataEnum.DW_DIMENSION_TASK;
            list.add(deleteTableDetailDto);
            // 分析维度
            deleteTableDetailDto.channelDataEnum = ChannelDataEnum.OLAP_DIMENSION_TASK;
            list.add(deleteTableDetailDto);
            dataFactoryClient.editByDeleteTable(list);

            int flat = mapper.deleteByIdWithFill(model);
            if (flat > 0) {
                List<String> delQualifiedName = new ArrayList<>();
                //删除dw
                MetaDataInstanceAttributeDTO dataSourceConfigDw = getDataSourceConfig(DataSourceConfigEnum.DMP_DW.getValue());
                if (dataSourceConfigDw != null && !CollectionUtils.isEmpty(dataSourceConfigDw.dbList)) {
                    delQualifiedName.add(dataSourceConfigDw.dbList.get(0).qualifiedName + "_" + DataModelTableTypeEnum.DW_DIMENSION.getValue() + "_" + id);
                }
                //删除Olap
                MetaDataInstanceAttributeDTO dataSourceConfigOlap = getDataSourceConfig(DataSourceConfigEnum.DMP_OLAP.getValue());
                if (dataSourceConfigOlap != null && !CollectionUtils.isEmpty(dataSourceConfigOlap.dbList)) {
                    delQualifiedName.add(dataSourceConfigOlap.dbList.get(0).qualifiedName + "_" + DataModelTableTypeEnum.DORIS_DIMENSION.getValue() + "_" + id);
                }

                if (openMetadata) {
                    //删除atlas
                    MetaDataDeleteAttributeDTO deleteDto = new MetaDataDeleteAttributeDTO();
                    deleteDto.qualifiedNames = delQualifiedName;
                    deleteDto.classifications = businessArea.getBusinessName();
                    dataManageClient.deleteMetaData(deleteDto);
                }
            }
            return flat > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        } catch (Exception e) {
            log.error("deleteDimension:" + e.getMessage());
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    /**
     * 拼接niFi删除流程
     *
     * @param businessAreaId
     * @param dimensionId
     * @return
     */
    public DataModelVO niFiDelProcess(int businessAreaId, int dimensionId) {
        DataModelVO vo = new DataModelVO();
        vo.businessId = String.valueOf(businessAreaId);
        vo.dataClassifyEnum = DataClassifyEnum.DATAMODELING;
        vo.delBusiness = false;
        DataModelTableVO tableVO = new DataModelTableVO();
        tableVO.type = OlapTableEnum.DIMENSION;
        List<Long> ids = new ArrayList<>();
        ids.add(Long.valueOf(dimensionId));
        tableVO.ids = ids;
        vo.dimensionIdList = tableVO;
        return vo;
    }

    /**
     * 拼接删除DW/Doris表
     *
     * @param dimensionName
     * @return
     */
    public PgsqlDelTableDTO delDwDorisTable(String dimensionName) {
        PgsqlDelTableDTO dto = new PgsqlDelTableDTO();
        dto.businessTypeEnum = BusinessTypeEnum.DATAMODEL;
        dto.delApp = false;
        List<TableListDTO> tableList = new ArrayList<>();
        TableListDTO table = new TableListDTO();
        table.tableName = dimensionName;
        tableList.add(table);
        dto.tableList = tableList;
        dto.userId = userHelper.getLoginUserInfo().id;
        return dto;
    }

    @Override
    public DimensionDTO getDimension(int id) {
        DimensionPO po = mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        DimensionDTO dimensionDTO = DimensionMap.INSTANCES.poToDto(po);
        dimensionDTO.deltaTimes = systemVariables.getSystemVariable(id, CreateTypeEnum.CREATE_DIMENSION.getValue());
        return dimensionDTO;
    }

    @Override
    public ResultEnum updateDimensionSql(DimensionSqlDTO dto) {
        DimensionPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        model.sqlScript = dto.sqlScript;
        model.dataSourceId = dto.dataSourceId;
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DimensionMetaDTO> getDimensionNameList(DimensionQueryDTO dto) {
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda().eq(DimensionPO::getBusinessId, dto.businessAreaId);
        if (dto.dimensionId != 0) {
            queryWrapper.lambda().ne(DimensionPO::getId, dto.dimensionId);
        }
        List<DimensionPO> list = mapper.selectList(queryWrapper);
        return DimensionMap.INSTANCES.poToListNameDto(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum updateDimensionDateAttribute(DimensionDateAttributeDTO dto) {
        //根据业务域id,还原之前的维度表设置的日期维度
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getBusinessId, dto.businessAreaId)
                .eq(DimensionPO::getIsDimDateTbl, true);
        List<DimensionPO> list = mapper.selectList(queryWrapper);
        //获取维度字段
        QueryWrapper<DimensionAttributePO> queryWrapper1 = new QueryWrapper<>();
        List<DimensionAttributePO> attributePoList = dimensionAttributeMapper.selectList(queryWrapper1);
        if (!CollectionUtils.isEmpty(list)) {
            for (DimensionPO item : list) {
                item.isDimDateTbl = false;
                if (mapper.updateById(item) == 0) {
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                }
                List<DimensionAttributePO> attributePoStreamList = attributePoList.stream()
                        .filter(e -> e.dimensionId == item.id).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(attributePoStreamList)) {
                    for (DimensionAttributePO attributePo : attributePoStreamList) {
                        attributePo.isDimDateField = false;
                        if (dimensionAttributeMapper.updateById(attributePo) == 0) {
                            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                        }
                    }
                }
            }
        }

        DimensionPO dimensionPo = mapper.selectById(dto.dimensionId);
        if (dimensionPo == null) {
            return ResultEnum.SUCCESS;
        }
        DimensionAttributePO dimensionAttributePo = dimensionAttributeMapper.selectById(dto.dimensionAttributeId);
        if (dimensionAttributePo == null) {
            return ResultEnum.SUCCESS;
        }
        dimensionPo.isDimDateTbl = true;
        if (mapper.updateById(dimensionPo) == 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        dimensionAttributePo.isDimDateField = true;
        return dimensionAttributeMapper.updateById(dimensionAttributePo) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DimensionDateAttributeDTO getDimensionDateAttribute(int businessId) {
        DimensionDateAttributeDTO data = new DimensionDateAttributeDTO();
        //查询设置时间维度表
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getBusinessId, businessId)
                .eq(DimensionPO::getIsDimDateTbl, true);
        List<DimensionPO> dimensionPoList = mapper.selectList(queryWrapper);
        if (dimensionPoList == null || dimensionPoList.size() == 0) {
            return data;
        }
        data.dimensionId = dimensionPoList.get(0).id;
        //查询设置时间维度表字段
        QueryWrapper<DimensionAttributePO> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda()
                .eq(DimensionAttributePO::getDimensionId, data.dimensionId)
                .eq(DimensionAttributePO::getIsDimDateField, true);
        List<DimensionAttributePO> dimensionAttributePoList = dimensionAttributeMapper.selectList(queryWrapper1);
        if (dimensionAttributePoList == null || dimensionAttributePoList.size() == 0) {
            return data;
        }
        data.dimensionAttributeId = dimensionAttributePoList.get(0).id;
        return data;
    }

    @Override
    public void updateDimensionPublishStatus(ModelPublishStatusDTO dto) {
        DimensionPO dimension = mapper.selectById(dto.id);
        if (dimension == null) {
            log.info("数据建模元数据实时同步失败,维度表不存在!");
            return;
        }
        int dataSourceId;
        int dataModelType;
        //0:DW发布状态
        if (dto.type == 0) {
            dimension.isPublish = dto.status;
            dataSourceId = DataSourceConfigEnum.DMP_DW.getValue();
            dataModelType = DataModelTableTypeEnum.DW_DIMENSION.getValue();
        } else {
            dimension.dorisPublish = dto.status;
            dataSourceId = DataSourceConfigEnum.DMP_OLAP.getValue();
            dataModelType = DataModelTableTypeEnum.DORIS_DIMENSION.getValue();
        }
        int flat = mapper.updateById(dimension);
        if (flat == 0 || dto.status != PublicStatusEnum.PUBLIC_SUCCESS.getValue()) {
            log.info("维度表更改状态失败!");
            return;
        }
        if (openMetadata) {
            synchronousMetadata(dataSourceId, dimension, dataModelType);
        }
    }

    /**
     * 数据实时同步到atlas
     *
     * @param dataSourceId
     * @param dimension
     * @param dataModelType
     */
    public void synchronousMetadata(int dataSourceId, DimensionPO dimension, int dataModelType) {

        BusinessAreaPO businessAreaPO = businessAreaImpl.query().eq("id", dimension.businessId).one();
        if (businessAreaPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        //实时更新元数据
        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();
        MetaDataInstanceAttributeDTO data = getDataSourceConfig(dataSourceId);
        if (data == null) {
            log.info("维度表元数据实时更新,查询实例数据失败!");
        }
        //表
        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();
        MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
        //table.contact_info = "";
        table.description = dimension.dimensionDesc;
        table.name = dimension.dimensionTabName;
        table.qualifiedName = data.dbList.get(0).qualifiedName + "_" + dataModelType + "_" + dimension.id;
        table.comment = String.valueOf(dimension.businessId);
        table.displayName = dimension.dimensionCnName;

        //获取业务域负责人
        table.owner = businessAreaPO.getBusinessAdmin();

        /*//所属人
        List<Long> ids = new ArrayList<>();
        ids.add(Long.parseLong(dimension.createUser));
        ResultEntity<List<UserDTO>> userListByIds = userClient.getUserListByIds(ids);
        if (userListByIds.code == ResultEnum.SUCCESS.getCode()) {
            table.owner = userListByIds.data.get(0).getUsername();
        }*/

        //字段
        List<MetaDataColumnAttributeDTO> columnList = new ArrayList<>();
        DimensionAttributeListDTO dimensionAttributeList = dimensionAttributeImpl.getDimensionAttributeList((int) dimension.id);
        for (DimensionAttributeDTO field : dimensionAttributeList.attributeDTOList) {
            MetaDataColumnAttributeDTO column = new MetaDataColumnAttributeDTO();
            String fieldTypeLength = field.dimensionFieldLength == 0 ? "" : "(" + field.dimensionFieldLength + ")";
            column.dataType = field.dimensionFieldType + fieldTypeLength;
            column.description = field.dimensionFieldDes;
            column.name = field.dimensionFieldEnName;
            column.qualifiedName = table.qualifiedName + "_" + field.id;
            column.owner = table.owner;
            column.displayName = field.dimensionFieldCnName;
            columnList.add(column);
        }
        table.columnList = columnList;
        tableList.add(table);
        data.dbList.get(0).tableList = tableList;
        list.add(data);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 更新元数据内容
                log.info("维度表构建元数据实时同步数据对象开始.........: 参数为: {}", JSON.toJSONString(list));
                dataManageClient.consumeMetaData(list);
            }
        }).start();

    }

    /**
     * 获取数据源配置信息
     *
     * @param dataSourceId
     * @return
     */
    public MetaDataInstanceAttributeDTO getDataSourceConfig(int dataSourceId) {
        ResultEntity<DataSourceDTO> result = userClient.getFiDataDataSourceById(dataSourceId);
        if (result.code != ResultEnum.SUCCESS.getCode()) {
            return null;
        }
        MetaDataInstanceAttributeDTO data = new MetaDataInstanceAttributeDTO();
        data.name = result.data.conIp;
        data.hostname = result.data.conIp;
        data.port = result.data.conPort.toString();
        data.platform = result.data.platform;
        data.qualifiedName = result.data.conIp;
        data.protocol = result.data.protocol;
        data.rdbms_type = result.data.conType.getName();
        data.displayName = result.data.conIp;
        //库
        List<MetaDataDbAttributeDTO> dbList = new ArrayList<>();
        MetaDataDbAttributeDTO db = new MetaDataDbAttributeDTO();
        db.name = result.data.conDbname;
        db.displayName = result.data.conDbname;
        db.qualifiedName = result.data.conIp + "_" + result.data.conDbname;
        dbList.add(db);
        data.dbList = dbList;
        return data;
    }

    @Override
    public DimensionDTO getDimensionByName(String tableName) {
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionPO::getDimensionTabName, tableName);
        DimensionPO po = mapper.selectOne(queryWrapper);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return DimensionMap.INSTANCES.poToDto(po);
    }

    @Override
    public List<TableNameDTO> getPublishSuccessDimTable(Integer businessId) {
        List<DimensionPO> list = this.query()
                .select("dimension_tab_name", "business_id", "id")
                .eq("is_publish", PublicStatusEnum.PUBLIC_SUCCESS.getValue())
                .eq("business_id", businessId)
                .or()
                .eq("share", true)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<TableNameDTO> data = new ArrayList<>();
        for (DimensionPO po : list) {
            TableNameDTO dto = new TableNameDTO();
            dto.tableName = po.dimensionTabName;

            DimensionAttributeListDTO dimensionAttributeList = dimensionAttributeImpl.getDimensionAttributeList((int) po.id);
            if (CollectionUtils.isEmpty(dimensionAttributeList.attributeDTOList)) {
                continue;
            }
            List<TableColumnDTO> columnList = new ArrayList<>();
            for (DimensionAttributeDTO item : dimensionAttributeList.attributeDTOList) {
                TableColumnDTO column = new TableColumnDTO();
                column.fieldName = item.dimensionFieldEnName;
                columnList.add(column);
            }

            dto.columnList = columnList;

            data.add(dto);
        }

        return data;
    }

    /**
     * 根据表名获取事实或维度表id
     *
     * @param tblName
     * @return
     */
    @Override
    public Long getFactOrDimTable(String tblName) {
        Long tblId = null;
        if (tblName.contains("dim_")) {
            DimensionPO one = getOne(new LambdaQueryWrapper<DimensionPO>().eq(DimensionPO::getDimensionTabName, tblName));
            if (one != null) {
                return one.id;
            } else {
                return null;
            }
        } else {
            FactPO factPO = factImpl.getFactIdByFactName(tblName);
            if (factPO != null) {
                return factPO.id;
            } else {
                return null;
            }
        }
    }

    /**
     * 获取业务域下的维度表计数
     *
     * @return
     */
    @Override
    public Integer getDimCountByBid(Integer businessId) {
        LambdaQueryWrapper<DimensionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DimensionPO::getBusinessId, businessId).ne(DimensionPO::getDimensionFolderId, 1);
        return mapper.selectCount(wrapper);
    }

    /**
     * 获取总共的维度表计数
     *
     * @return
     */
    @Override
    public Integer getDimTotalCount() {
        LambdaQueryWrapper<DimensionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DimensionPO::getDelFlag, 1).ne(DimensionPO::getDimensionFolderId, 1);
        return mapper.selectCount(wrapper);
    }

    /**
     * 获取总共的公共维度表计数
     *
     * @return
     */
    @Override
    public Integer getPublicDimTotalCount() {
        LambdaQueryWrapper<DimensionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DimensionPO::getDelFlag, 1).eq(DimensionPO::getDimensionFolderId, 1);
        return mapper.selectCount(wrapper);
    }

    /**
     * 获取维度tree
     *
     * @return
     */
    @Override
    public List<DimensionTreeDTO> getDimensionTree() {
        List<BusinessAreaPO> businessAreaPOS = businessArea.list();

        List<BusinessAreaDimDTO> areaDimDTOS = new ArrayList<>();
        List<BusinessAreaDimDTO> publicDimDTOS = new ArrayList<>();

        BusinessAreaDimDTO publicBusinessAreaDimDTO = new BusinessAreaDimDTO();
        publicBusinessAreaDimDTO.setBusinessName("公共域维度");
        publicBusinessAreaDimDTO.setId(0);
        List<DimensionListDTO> publicDimensionListDTOS = new ArrayList<>();

        for (BusinessAreaPO businessAreaPO : businessAreaPOS) {
            LambdaQueryWrapper<DimensionPO> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(DimensionPO::getBusinessId, businessAreaPO.getId())
                    .orderByAsc(DimensionPO::getDimensionTabName);
            List<DimensionPO> dimensionPOS = dimension.list(wrapper1);

            BusinessAreaDimDTO businessAreaDimDTO = new BusinessAreaDimDTO();
            businessAreaDimDTO.setBusinessName(businessAreaPO.getBusinessName());
            businessAreaDimDTO.setId(businessAreaPO.getId());
            List<DimensionListDTO> dimensionListDTOS = new ArrayList<>();

            for (DimensionPO dimensionPO : dimensionPOS) {
                LambdaQueryWrapper<DimensionAttributePO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(DimensionAttributePO::getDimensionId, dimensionPO.getId());
                List<DimensionAttributePO> attributePOList = dimensionAttribute.list(wrapper);
                DimensionListDTO dimensionListDTO = new DimensionListDTO();
                dimensionListDTO.dimensionTabName = dimensionPO.dimensionTabName;
                dimensionListDTO.setId(dimensionPO.getId());
                dimensionListDTO.setDimensionCnName(dimensionPO.getDimensionCnName());
                dimensionListDTO.setPrefixTempName(dimensionPO.getPrefixTempName());
                dimensionListDTO.setIsPublish(dimensionPO.getIsPublish());
                List<DimensionAttributeDataDTO> attributeDataDTOS = new ArrayList<>();

                for (DimensionAttributePO dimensionAttributePO : attributePOList) {
                    DimensionAttributeDataDTO attributeDataDTO = new DimensionAttributeDataDTO();
                    attributeDataDTO.setId(dimensionAttributePO.getId());
                    attributeDataDTO.setDimensionFieldCnName(dimensionAttributePO.getDimensionFieldCnName());
                    attributeDataDTO.setDimensionFieldEnName(dimensionAttributePO.getDimensionFieldEnName());
                    attributeDataDTO.setDimensionFieldDes(dimensionAttributePO.getDimensionFieldDes());
                    attributeDataDTO.setDimensionFieldType(dimensionAttributePO.getDimensionFieldType());
                    attributeDataDTO.setDimensionFieldLength(dimensionAttributePO.getDimensionFieldLength());
                    attributeDataDTOS.add(attributeDataDTO);
                }
                //维度字段
                dimensionListDTO.setAttributeList(attributeDataDTOS);

                //公共维度表集合  1是公共维度文件夹id
                if (dimensionPO.getDimensionFolderId() == 1) {
                    publicDimensionListDTOS.add(dimensionListDTO);
                } else {
                    //其他维度表
                    dimensionListDTOS.add(dimensionListDTO);

                }

            }
            businessAreaDimDTO.setDimensionList(dimensionListDTOS);
            areaDimDTOS.add(businessAreaDimDTO);
        }

        publicBusinessAreaDimDTO.setDimensionList(publicDimensionListDTOS);
        publicDimDTOS.add(publicBusinessAreaDimDTO);
        DimensionTreeDTO dimensionTreeDTO = new DimensionTreeDTO();
        dimensionTreeDTO.setPublicDim(publicDimDTOS);
        dimensionTreeDTO.setOtherDimsByArea(areaDimDTOS);
        List<DimensionTreeDTO> list = new ArrayList();
        list.add(dimensionTreeDTO);
        return list;
    }

    /**
     * 维度表跨业务域移动
     *
     * @param dto
     * @return
     */
    @Override
    public ResultEntity<Object> transDimToBArea(DimTransDTO dto) {
        ResultEnum resultEnum = null;
        /*
        如果该事实表已被配置到管道中，则不允许转移，牵扯到kafka发消息的topic，因此给出提示
         */
        // 移动之前检查该事实表是否已经被配置到存在的管道里面：如果管道中有则不允许移动
        // 方式：检查配置库-dmp_factory_db库 tb_nifi_custom_workflow_detail表内是否存在该事实表，
        // 如果存在则不允许删除，给出提示并告知该表被配置到哪个管道里面    tips:数仓建模的事实表对应的table type是5  数仓表任务-数仓事实表任务
        CheckPhyDimFactTableIfExistsDTO checkDto = new CheckPhyDimFactTableIfExistsDTO();
        checkDto.setTblId((long) dto.dimId);
        checkDto.setChannelDataEnum(ChannelDataEnum.getName(5));
        ResultEntity<List<NifiCustomWorkflowDetailDTO>> booleanResultEntity = dataFactoryClient.checkPhyTableIfExists(checkDto);
        if (booleanResultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
            return ResultEntityBuild.build(ResultEnum.DISPATCH_REMOTE_ERROR);
        }
        List<NifiCustomWorkflowDetailDTO> data = booleanResultEntity.getData();
        if (!CollectionUtils.isEmpty(data)) {
            //这里的getWorkflowId 已经被替换为 workflowName
            List<String> collect = data.stream().map(NifiCustomWorkflowDetailDTO::getWorkflowId).collect(Collectors.toList());
            log.info("当前要删除的表存在于以下管道中：" + collect);
            return ResultEntityBuild.build(ResultEnum.FACT_EXISTS_IN_DISPATCH, collect);
        }

        /*
        1、转移维度表
         */
        log.info("==========开始转移维度表==========");
        //获取要转移的事实表信息
        LambdaQueryWrapper<DimensionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DimensionPO::getId, dto.getDimId());
        DimensionPO one = getOne(wrapper);
        //修改信息
        one.setDimensionFolderId(dto.toDimensionFolderId);
        one.setBusinessId(dto.toBusinessId);
        updateById(one);
        log.info("==========维度表转移完成==========");

        /*
        2、删除原nifi
         */
        log.info("==========开始删除nifi原流程==========");
        //拼接删除niFi参数
        DataModelVO vo = niFiDelProcess(dto.curBusinessId, dto.dimId);
        ResultEntity<Object> result = publishTaskClient.deleteNifiFlowByKafka(vo);
        log.info("==========nifi删除结果:" + result.getCode());

        /*
        3、重新发布维度表
         */
        log.info("==========开始重新发布==========");
        DimensionFolderPublishQueryDTO queryDTO = new DimensionFolderPublishQueryDTO();
        queryDTO.setBusinessAreaId(dto.toBusinessId);
        List<Integer> dimIds = new ArrayList<>();
        dimIds.add(dto.dimId);
        queryDTO.setDimensionIds(dimIds);
        //不同步数据
        queryDTO.setOpenTransmission(false);
        queryDTO.setRemark("从业务域" + dto.curBusinessId + " 移动到业务域 " + dto.toBusinessId);
        resultEnum = dimensionFolder.batchPublishDimensionFolder(queryDTO);
        log.info("==========重新发布结果:" + result.getCode());
        return ResultEntityBuild.build(resultEnum);
    }

    @Override
    public List<DimensionDTO> getDimensionTableByIds(List<Integer> ids) {
        return DimensionMap.INSTANCES.listPoToListDto(dimension.getBaseMapper().selectBatchIds(ids));
    }

    public List<MetaDataTableAttributeDTO> getDimensionMetaData(BusinessAreaPO area,
                                                                String dbQualifiedName,
                                                                Integer dataModelType,
                                                                String businessAdmin) {
        List<DimensionPO> list = this.query()
                .eq("business_id", area.getId())
                .eq("is_publish", PublicStatusEnum.PUBLIC_SUCCESS.getValue())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();

        for (DimensionPO item : list) {
            MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
            table.contact_info = "";
            table.description = item.dimensionDesc;
            table.name = item.dimensionTabName;
            table.comment = String.valueOf(item.businessId);
            table.qualifiedName = dbQualifiedName + "_" + dataModelType + "_" + item.id;
            table.displayName = item.dimensionCnName;
            table.owner = businessAdmin;
            table.sqlScript = item.sqlScript;
            table.coverScript = item.coverScript;
            table.tableConfigId = Long.valueOf(item.id).intValue();
            table.dataSourceId = item.dataSourceId;
            table.columnList = getDimensionAttributeMetaData(item.id, table);
            table.isExistStg = true;
            table.isExistClassification = true;
            table.isShareDim = item.share;
            table.AppName = area.getBusinessName();
            table.whetherSchema = false;
            tableList.add(table);
        }

        return tableList;

    }

    public List<MetaDataTableAttributeDTO> getDimensionMetaDataByLastSyncTime(BusinessAreaPO area,
                                                                              String dbQualifiedName,
                                                                              Integer dataModelType,
                                                                              String businessAdmin,
                                                                              List<Integer> dimIds) {
        List<DimensionPO> list = this.query()
                .eq("business_id", area.getId())
                .eq("is_publish", PublicStatusEnum.PUBLIC_SUCCESS.getValue())
                .in("id", dimIds)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();

        for (DimensionPO item : list) {
            MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
            table.contact_info = "";
            table.description = item.dimensionDesc;
            table.name = item.dimensionTabName;
            table.comment = String.valueOf(item.businessId);
            table.qualifiedName = dbQualifiedName + "_" + dataModelType + "_" + item.id;
            table.displayName = item.dimensionCnName;
            table.owner = businessAdmin;
            table.sqlScript = item.sqlScript;
            table.coverScript = item.coverScript;
            table.tableConfigId = Long.valueOf(item.id).intValue();
            table.dataSourceId = item.dataSourceId;
            table.columnList = getDimensionAttributeMetaData(item.id, table);
            table.isExistStg = true;
            table.isExistClassification = true;
            table.isShareDim = item.share;
            table.AppName = area.getBusinessName();
            table.whetherSchema = false;
            tableList.add(table);
        }

        return tableList;

    }

    public List<MetaDataTableAttributeDTO> getDimensionMetaDataOfBatchTbl(long businessId,
                                                                          List<Integer> tblIds,
                                                                          String dbQualifiedName,
                                                                          Integer dataModelType,
                                                                          String businessAdmin) {
        List<DimensionPO> list = this.query()
                .eq("business_id", businessId)
                .eq("is_publish", PublicStatusEnum.PUBLIC_SUCCESS.getValue())
                .in("id", tblIds)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();

        for (DimensionPO item : list) {
            MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
            table.contact_info = "";
            table.description = item.dimensionDesc;
            table.name = item.dimensionTabName;
            table.comment = String.valueOf(item.businessId);
            table.qualifiedName = dbQualifiedName + "_" + dataModelType + "_" + item.id;
            table.displayName = item.dimensionCnName;
            table.owner = businessAdmin;
            table.columnList = getDimensionAttributeMetaData(item.id, table);
            tableList.add(table);
        }

        return tableList;

    }

    public List<MetaDataColumnAttributeDTO> getDimensionAttributeMetaData(long dimensionId, MetaDataTableAttributeDTO table) {
        List<MetaDataColumnAttributeDTO> columnList = new ArrayList<>();
        DimensionAttributeListDTO dimensionAttributeList = dimensionAttributeImpl.getDimensionAttributeList((int) dimensionId);
        for (DimensionAttributeDTO field : dimensionAttributeList.attributeDTOList) {
            MetaDataColumnAttributeDTO column = new MetaDataColumnAttributeDTO();
            String fieldTypeLength = field.dimensionFieldLength == 0 ? "" : "(" + field.dimensionFieldLength + ")";
            column.dataType = field.dimensionFieldType + fieldTypeLength;
            column.description = field.dimensionFieldDes;
            column.name = field.dimensionFieldEnName;
            column.qualifiedName = table.qualifiedName + "_" + field.id;
            column.owner = table.owner;
            column.displayName = field.dimensionFieldCnName;
            columnList.add(column);
        }
        return columnList;
    }

    /**
     * 获取数据源信息
     *
     * @param id
     * @return
     */
    private DataSourceDTO getTargetDbInfo(Integer id) {
        ResultEntity<DataSourceDTO> dataSourceConfig = null;
        try {
            dataSourceConfig = userClient.getFiDataDataSourceById(id);
            if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
            }
            if (Objects.isNull(dataSourceConfig.data)) {
                throw new FkException(ResultEnum.DATA_QUALITY_DATASOURCE_NOT_EXISTS);
            }
        } catch (Exception e) {
            log.error("调用userClient服务获取数据源失败,", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        return dataSourceConfig.data;
    }

}