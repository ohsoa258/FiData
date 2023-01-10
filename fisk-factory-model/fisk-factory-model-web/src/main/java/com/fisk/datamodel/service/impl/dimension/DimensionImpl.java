package com.fisk.datamodel.service.impl.dimension;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.task.BusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.datamodel.BuildDataModelHelper;
import com.fisk.common.service.dbBEBuild.datamodel.IBuildDataModelSqlCommand;
import com.fisk.common.service.metadata.dto.metadata.*;
import com.fisk.datafactory.client.DataFactoryClient;
import com.fisk.datafactory.dto.customworkflowdetail.DeleteTableDetailDTO;
import com.fisk.datafactory.enums.ChannelDataEnum;
import com.fisk.datamanage.client.DataManageClient;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimension.DimensionDateAttributeDTO;
import com.fisk.datamodel.dto.dimension.DimensionQueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeListDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionMetaDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.entity.BusinessAreaPO;
import com.fisk.datamodel.entity.dimension.DimensionAttributePO;
import com.fisk.datamodel.entity.dimension.DimensionPO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import com.fisk.datamodel.enums.DataModelTableTypeEnum;
import com.fisk.datamodel.enums.DimensionAttributeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.dimension.DimensionMap;
import com.fisk.datamodel.mapper.dimension.DimensionAttributeMapper;
import com.fisk.datamodel.mapper.dimension.DimensionMapper;
import com.fisk.datamodel.mapper.fact.FactAttributeMapper;
import com.fisk.datamodel.service.IDimension;
import com.fisk.datamodel.service.impl.BusinessAreaImpl;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

    @Resource
    DimensionMapper mapper;
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
    DataSourceConfigUtil dataSourceConfigUtil;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addDimension(DimensionDTO dto) {
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getDimensionTabName, dto.dimensionTabName);
        DimensionPO po = mapper.selectOne(queryWrapper);
        if (po != null) {
            return ResultEnum.DIMENSION_EXIST;
        }
        dto.isPublish = PublicStatusEnum.UN_PUBLIC.getValue();
        DimensionPO model = DimensionMap.INSTANCES.dtoToPo(dto);
        //判断是否为生成时间维度表
        if (dto.timeTable) {
            model.isPublish = PublicStatusEnum.PUBLIC_SUCCESS.getValue();
            //生成物理表以及插入数据
            editDateDimension(dto, dto.dimensionTabName);
        }
        int flat = mapper.insert(model);
        if (flat > 0 && dto.timeTable) {
            return addTimeTableAttribute(dto);
        }
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
            conn = dataSourceConfigUtil.getStatement();
            stat = conn.createStatement();
            if (!dto.dimensionTabName.equals(oldTimeTable)) {
                //删除表
                boolean execute = stat.execute("drop table " + oldTimeTable);
                if (execute) {
                    throw new FkException(ResultEnum.DELETE_ERROR);
                }
            }
            //获取数据库表名
            ResultSet rs = conn.getMetaData().getTables(null, null, dto.dimensionTabName, null);
            if( rs.next() ){
                throw new FkException(ResultEnum.TABLE_IS_EXIST);
            }
            int flat = stat.executeUpdate(buildTableSql(dto.dimensionTabName));
            if (flat>=0) {
                String strSql = insertTableDataSql(dto.dimensionTabName, dto.startTime, dto.endTime);
                stat.addBatch(strSql);
                stat.executeBatch();
            }
        } catch (Exception e) {
            log.error("editDateDimension ex:", e);
            throw new FkException(ResultEnum.VISUAL_CONNECTION_ERROR);
        } finally {
            AbstractCommonDbHelper.closeStatement(stat);
            AbstractCommonDbHelper.closeConnection(conn);
        }
    }

    /**
     * 拼接创表语句
     *
     * @param dimensionTabName
     * @return
     */
    public String buildTableSql(String dimensionTabName) {
        DataSourceDTO dwSource = dataSourceConfigUtil.getDwSource();
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
    public String insertTableDataSql(String tableName, String startTime, String endTime) {
        StringBuilder str = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(startTime);
        } catch (ParseException e) {
            log.error("insertTableDataSql ex:", e);
            return "";
        }
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR,-1);
        boolean flat=true;
        String[] monthName = {"January", "February",
                "March", "April", "May", "June", "July",
                "August", "September", "October", "November",
                "December"};
        int i=0;
        str.append("insert into "+tableName);
        while (flat) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date dt1 = calendar.getTime();
            String reStr = sdf.format(dt1);
            if (endTime.equals(reStr)) {
                flat = false;
            }
            //获取星期几
            int dayNumberOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            dayNumberOfWeek = dayNumberOfWeek - 1;
            if (dayNumberOfWeek == 0) {
                dayNumberOfWeek = 7;
            }
            //获取星期名称
            String englishDayNameOfWeek=getEnglishDayNameOfWeek(dayNumberOfWeek);
            //几号
            int dayNumberOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
            //一年中第几天
            int dayNumberOfYear=calendar.get(Calendar.DAY_OF_YEAR);
            //第几周
            int weekNumberOfYear=calendar.get(Calendar.WEEK_OF_YEAR);
            //月份名称
            String englishMonthName= monthName[calendar.get(Calendar.MONTH)];;
            //第几月
            int monthNumberOfYear=calendar.get(Calendar.MONTH) + 1;
            //季度
            int calendarQuarter=getCurrentMonth(calendar.get(Calendar.MONTH) + 1);
            //季度年
            int calendarYear=calendar.get(Calendar.YEAR);
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
                        + calendarYear + ")"
                );
            } else {
                str.append(",('"+reStr+"',"
                        +dayNumberOfWeek+",'"
                        +englishDayNameOfWeek+"',"
                        +dayNumberOfMonth+","
                        +dayNumberOfYear+","
                        +weekNumberOfYear+",'"
                        +englishMonthName+"',"
                        +monthNumberOfYear+","
                        +calendarQuarter+","
                        +calendarYear+")"
                );
            }
            i+=1;
        }
        return str.toString();
    }

    /**
     * 获取星期名称
     * @param dayNumberOfWeek
     * @return
     */
    public String getEnglishDayNameOfWeek(int dayNumberOfWeek) {
        String name="";
        switch (dayNumberOfWeek) {
            case 1:
                name="Monday";
                break;
            case 2:
                name="Tuesday";
                break;
            case 3:
                name="Wednesday";
                break;
            case 4:
                name="Thursday";
                break;
            case 5:
                name="Friday";
                break;
            case 6:
                name="Saturday";
                break;
            case 7:
                name="Sunday";
            default:
                break;
        }
        return name;
    }

    /**
     * 获取季度
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
        String[] columnList = {"FullDateAlternateKey", "DayNumberOfWeek",
                "EnglishDayNameOfWeek", "DayNumberOfMonth", "DayNumberOfYear", "WeekNumberOfYear", "EnglishMonthName",
                "MonthNumberOfYear", "CalendarQuarter", "CalendarYear"};
        String[] columnDataTypeList = {"DATE", "INT", "VARCHAR", "INT", "INT", "INT", "VARCHAR", "INT", "INT", "INT"};
        String[] columnDataTypeLengthList = {"0", "0", "10", "0", "0", "0", "10", "0", "0", "0"};
        DataSourceDTO odsSource = dataSourceConfigUtil.getDwSource();
        switch (odsSource.conType) {
            case MYSQL:
                break;
            case ORACLE:
                columnDataTypeList = new String[]{"DATE", "NUMBER", "VARCHAR2", "NUMBER", "NUMBER", "NUMBER", "VARCHAR2", "NUMBER", "NUMBER", "NUMBER"};
                break;
            case SQLSERVER:
                columnDataTypeList = new String[]{"DATE", "INT", "VARCHAR", "INT", "INT", "INT", "VARCHAR", "INT", "INT", "INT"};
                break;
            case POSTGRESQL:
                columnDataTypeList = new String[]{"DATE", "INT2", "VARCHAR", "INT2", "INT2", "INT2", "VARCHAR", "INT2", "INT2", "INT2"};
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
        if (result.getCode() == ResultEnum.SUCCESS.getCode()) {
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
        if (flat > 0 && dto.timeTable) {
            //同步atlas
            DimensionPO dimensionPo = mapper.selectById(dto.id);
            if (dimensionPo != null) {
                synchronousMetadata(DataSourceConfigEnum.DMP_DW.getValue(), dimensionPo, DataModelTableTypeEnum.DW_DIMENSION.getValue());
            }
        }

        //同步元数据
        if (model.isPublish == PublicStatusEnum.PUBLIC_SUCCESS.getValue()) {
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
            List<FactAttributePO> factAttributePoList=factAttributeMapper.selectList(queryWrapper1);
            if (!CollectionUtils.isEmpty(factAttributePoList)) {
                return ResultEnum.TABLE_ASSOCIATED;
            }
            //删除维度字段数据
            QueryWrapper<DimensionAttributePO> attributePoQueryWrapper=new QueryWrapper<>();
            attributePoQueryWrapper.select("id").lambda().eq(DimensionAttributePO::getDimensionId,id);
            List<Integer> dimensionAttributeIds=(List)dimensionAttributeMapper.selectObjs(attributePoQueryWrapper);
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
                //删除atlas
                MetaDataDeleteAttributeDTO deleteDto = new MetaDataDeleteAttributeDTO();
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
                deleteDto.qualifiedNames = delQualifiedName;
                deleteDto.classifications = businessArea.getBusinessName();
                dataManageClient.deleteMetaData(deleteDto);
            }
            return flat > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        } catch (Exception e) {
            log.error("deleteDimension:" + e.getMessage());
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    /**
     * 拼接niFi删除流程
     * @param businessAreaId
     * @param dimensionId
     * @return
     */
    public DataModelVO niFiDelProcess(int businessAreaId,int dimensionId) {
        DataModelVO vo=new DataModelVO();
        vo.businessId= String.valueOf(businessAreaId);
        vo.dataClassifyEnum= DataClassifyEnum.DATAMODELING;
        vo.delBusiness=false;
        DataModelTableVO tableVO=new DataModelTableVO();
        tableVO.type= OlapTableEnum.DIMENSION;
        List<Long> ids=new ArrayList<>();
        ids.add(Long.valueOf(dimensionId));
        tableVO.ids=ids;
        vo.dimensionIdList=tableVO;
        return vo;
    }

    /**
     * 拼接删除DW/Doris表
     * @param dimensionName
     * @return
     */
    public PgsqlDelTableDTO delDwDorisTable(String dimensionName) {
        PgsqlDelTableDTO dto=new PgsqlDelTableDTO();
        dto.businessTypeEnum= BusinessTypeEnum.DATAMODEL;
        dto.delApp=false;
        List<TableListDTO> tableList=new ArrayList<>();
        TableListDTO table=new TableListDTO();
        table.tableName=dimensionName;
        tableList.add(table);
        dto.tableList=tableList;
        dto.userId=userHelper.getLoginUserInfo().id;
        return dto;
    }

    @Override
    public DimensionDTO getDimension(int id) {
        DimensionPO po=mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return DimensionMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum updateDimensionSql(DimensionSqlDTO dto) {
        DimensionPO model = mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        model.sqlScript = dto.sqlScript;
        model.appId = dto.appId;
        return mapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DimensionMetaDTO>getDimensionNameList(DimensionQueryDTO dto) {
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda().eq(DimensionPO::getBusinessId,dto.businessAreaId);
        if (dto.dimensionId != 0) {
            queryWrapper.lambda().ne(DimensionPO::getId, dto.dimensionId);
        }
        List<DimensionPO> list=mapper.selectList(queryWrapper);
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

        DimensionPO dimensionPo=mapper.selectById(dto.dimensionId);
        if (dimensionPo == null) {
            return ResultEnum.SUCCESS;
        }
        DimensionAttributePO dimensionAttributePo=dimensionAttributeMapper.selectById(dto.dimensionAttributeId);
        if (dimensionAttributePo == null) {
            return ResultEnum.SUCCESS;
        }
        dimensionPo.isDimDateTbl=true;
        if (mapper.updateById(dimensionPo) == 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        dimensionAttributePo.isDimDateField=true;
        return dimensionAttributeMapper.updateById(dimensionAttributePo)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DimensionDateAttributeDTO getDimensionDateAttribute(int businessId) {
        DimensionDateAttributeDTO data=new DimensionDateAttributeDTO();
        //查询设置时间维度表
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getBusinessId,businessId)
                .eq(DimensionPO::getIsDimDateTbl,true);
        List<DimensionPO> dimensionPoList=mapper.selectList(queryWrapper);
        if (dimensionPoList == null || dimensionPoList.size() == 0) {
            return data;
        }
        data.dimensionId=dimensionPoList.get(0).id;
        //查询设置时间维度表字段
        QueryWrapper<DimensionAttributePO> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.lambda()
                .eq(DimensionAttributePO::getDimensionId,data.dimensionId)
                .eq(DimensionAttributePO::getIsDimDateField,true);
        List<DimensionAttributePO> dimensionAttributePoList=dimensionAttributeMapper.selectList(queryWrapper1);
        if (dimensionAttributePoList == null || dimensionAttributePoList.size() == 0) {
            return data;
        }
        data.dimensionAttributeId=dimensionAttributePoList.get(0).id;
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
        synchronousMetadata(dataSourceId, dimension, dataModelType);
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

        /*try {
            MetaDataAttributeDTO metaDataAttribute = new MetaDataAttributeDTO();
            metaDataAttribute.instanceList = list;
            metaDataAttribute.userId = Long.parseLong(dimension.createUser);
            // 更新元数据内容
            log.info("维度表构建元数据实时同步数据对象开始.........: 参数为: {}", JSON.toJSONString(list));
            dataManageClient.metaData(metaDataAttribute);
        } catch (Exception e) {
            log.error("【dataManageClient.MetaData()】方法报错,ex", e);
        }*/


        new Thread(new Runnable() {
            @Override
            public void run() {
                // 更新元数据内容
                log.info("维度表构建元数据实时同步数据对象开始.........: 参数为: {}", JSON.toJSONString(list));
                dataManageClient.consumeMetaData(list);
            }
        }).start();

        //修改元数据
        /*ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 更新元数据内容
                    log.info("维度表构建元数据实时同步数据对象开始.........: 参数为: {}", JSON.toJSONString(list));
                    dataManageClient.consumeMetaData(list);
                } catch (Exception e) {
                    log.error("【dataManageClient.MetaData()】方法报错,ex", e);
                }
            }
        });*/
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

    public List<MetaDataTableAttributeDTO> getDimensionMetaData(long businessId,
                                                                String dbQualifiedName,
                                                                Integer dataModelType,
                                                                String businessAdmin) {
        List<DimensionPO> list = this.query()
                .eq("business_id", businessId)
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

            table.columnList = getDimensionAttributeMetaData(item.id, table);
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

}