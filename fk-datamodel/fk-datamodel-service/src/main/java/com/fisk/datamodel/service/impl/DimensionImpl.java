package com.fisk.datamodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.enums.task.BusinessTypeEnum;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.datamodel.dto.dimension.DimensionDTO;
import com.fisk.datamodel.dto.dimension.DimensionDateAttributeDTO;
import com.fisk.datamodel.dto.dimension.DimensionQueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
import com.fisk.datamodel.dto.dimensionattribute.DimensionMetaDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.entity.*;
import com.fisk.datamodel.enums.DimensionAttributeEnum;
import com.fisk.datamodel.enums.PublicStatusEnum;
import com.fisk.datamodel.map.DimensionMap;
import com.fisk.datamodel.mapper.*;
import com.fisk.datamodel.service.IDimension;
import com.fisk.datamodel.vo.DataModelTableVO;
import com.fisk.datamodel.vo.DataModelVO;
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
import java.sql.*;
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
public class DimensionImpl implements IDimension {

    @Resource
    DimensionMapper mapper;
    @Resource
    DimensionAttributeMapper dimensionAttributeMapper;
    @Resource
    FactAttributeMapper factAttributeMapper;
    @Resource
    DimensionAttributeImpl dimensionAttributeImpl;
    @Resource
    PublishTaskClient publishTaskClient;
    @Resource
    UserHelper userHelper;

    @Value("${generate.date-dimension.datasource.typeName}")
    private String typeName;
    @Value("${generate.date-dimension.datasource.driver}")
    private String driver;
    @Value("${generate.date-dimension.datasource.url}")
    private String url;
    @Value("${generate.date-dimension.datasource.userName}")
    private String userName;
    @Value("${generate.date-dimension.datasource.password}")
    private String password;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addDimension(DimensionDTO dto) throws SQLException {
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getDimensionTabName,dto.dimensionTabName);
        DimensionPO po=mapper.selectOne(queryWrapper);
        if (po !=null)
        {
            return ResultEnum.DIMENSION_EXIST;
        }
        dto.isPublish= PublicStatusEnum.UN_PUBLIC.getValue();
        DimensionPO model= DimensionMap.INSTANCES.dtoToPo(dto);
        //判断是否为生成时间维度表
        if (dto.timeTable)
        {
            dto.isPublish= PublicStatusEnum.PUBLIC_SUCCESS.getValue();
            //生成物理表以及插入数据
            editDateDimension(dto,dto.dimensionTabName);
        }
        int flat=mapper.insert(model);
        if (flat>0 && dto.timeTable)
        {
            return addTimeTableAttribute(dto);
        }
        return flat>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    public void editDateDimension(DimensionDTO dto, String oldTimeTable) throws SQLException
    {
        Connection conn=getStatement(driver,url,userName,password);
        Statement stat = conn.createStatement();
        try {
            if (!dto.dimensionTabName.equals(oldTimeTable))
            {
                //删除表
                boolean execute = stat.execute("drop table " + oldTimeTable);
                if (execute)
                {
                    throw new FkException(ResultEnum.DELETE_ERROR);
                }
            }
            //获取数据库表名
            ResultSet rs = conn.getMetaData().getTables(null, null, dto.dimensionTabName, null);
            if( rs.next() ){
                throw new FkException(ResultEnum.TABLE_IS_EXIST);
            }
            int flat = stat.executeUpdate(buildTableSql(dto.dimensionTabName));
            if (flat>=0)
            {
                String strSql = insertTableDataSql(dto.dimensionTabName, dto.startTime, dto.endTime);
                stat.addBatch(strSql);
                stat.executeBatch();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new FkException(ResultEnum.VISUAL_CONNECTION_ERROR);
        }
        finally {
            stat.close();
            conn.close();
        }
    }

    /**
     * 拼接创表语句
     * @param dimensionTabName
     * @return
     */
    public String buildTableSql(String dimensionTabName)
    {
        String sql="CREATE TABLE "+dimensionTabName +"("
                +"FullDateAlternateKey date not null,"
                +"DayNumberOfWeek int not null,"
                +"EnglishDayNameOfWeek varchar(10) not null,"
                +"DayNumberOfMonth int not null,"
                +"DayNumberOfYear int not null,"
                +"WeekNumberOfYear int not null,"
                +"EnglishMonthName varchar(10) not null,"
                +"MonthNumberOfYear int not null,"
                +"CalendarQuarter int not null,"
                +"CalendarYear int not null)";
        switch (typeName.toLowerCase())
        {
            case "mysql":
                break;
            case "oracle":
                sql="CREATE TABLE "+dimensionTabName +"("
                        +"FullDateAlternateKey date not null,"
                        +"DayNumberOfWeek number  not null,"
                        +"EnglishDayNameOfWeek varchar2(10) not null,"
                        +"DayNumberOfMonth number not null,"
                        +"DayNumberOfYear number not null,"
                        +"WeekNumberOfYear number not null,"
                        +"EnglishMonthName varchar2(10) not null,"
                        +"MonthNumberOfYear number not null,"
                        +"CalendarQuarter number not null,"
                        +"CalendarYear number not null)";
                break;
            case "sqlserver":
                break;
            case "postgresql":
                sql="CREATE TABLE "+dimensionTabName +"("
                        +"FullDateAlternateKey date not null,"
                        +"DayNumberOfWeek int2  not null,"
                        +"EnglishDayNameOfWeek varchar(10) not null,"
                        +"DayNumberOfMonth int2 not null,"
                        +"DayNumberOfYear int2 not null,"
                        +"WeekNumberOfYear int2 not null,"
                        +"EnglishMonthName varchar(10) not null,"
                        +"MonthNumberOfYear int2 not null,"
                        +"CalendarQuarter int2 not null,"
                        +"CalendarYear int2 not null)";
                break;
            case "doris":
                sql="CREATE TABLE "+dimensionTabName +"("
                        +"FullDateAlternateKey date not null,"
                        +"DayNumberOfWeek int  not null,"
                        +"EnglishDayNameOfWeek varchar(10) not null,"
                        +"DayNumberOfMonth int not null,"
                        +"DayNumberOfYear int not null,"
                        +"WeekNumberOfYear int not null,"
                        +"EnglishMonthName varchar(10) not null,"
                        +"MonthNumberOfYear int not null,"
                        +"CalendarQuarter int not null,"
                        +"CalendarYear int not null) DISTRIBUTED BY HASH ( `FullDateAlternateKey` )"
                        +"BUCKETS 16 PROPERTIES (\"replication_num\" = \"1\")";
                break;
            default:
                sql="";
                break;
        }
        return sql;
    }

    /**
     * 拼接insert语句
     * @param tableName
     * @param startTime
     * @param endTime
     * @return
     */
    public String insertTableDataSql(String tableName,String startTime,String endTime) {
        StringBuilder str = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = sdf.parse(startTime);
        } catch (ParseException e) {
            e.printStackTrace();
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
        while (flat)
        {
            calendar.add(Calendar.DAY_OF_YEAR,1);
            Date dt1=calendar.getTime();
            String reStr = sdf.format(dt1);
            if (endTime.equals(reStr))
            {
                flat=false;
            }
            //获取星期几
            int DayNumberOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            DayNumberOfWeek = DayNumberOfWeek - 1;
            if (DayNumberOfWeek == 0) {
                DayNumberOfWeek = 7;
            }
            //获取星期名称
            String englishDayNameOfWeek=getEnglishDayNameOfWeek(DayNumberOfWeek);
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
            if (i==0)
            {
                str.append(" values('"+reStr+"',"
                        +DayNumberOfWeek+",'"
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
            else {
                str.append(",('"+reStr+"',"
                        +DayNumberOfWeek+",'"
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
    public String getEnglishDayNameOfWeek(int dayNumberOfWeek)
    {
        String name="";
        switch (dayNumberOfWeek)
        {
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
    public int getCurrentMonth(int currentMonth)
    {
        int dt = 0;
        if (currentMonth >= 1 && currentMonth <= 3)
        {
            dt=1;
        }
        else if (currentMonth >= 4 && currentMonth <= 6)
        {
            dt=2;
        }
        else if (currentMonth >= 7 && currentMonth <= 9)
        {
            dt=3;
        }
        else if (currentMonth >= 10 && currentMonth <= 12)
        {
            dt=4;
        }
        return dt;
    }

    public ResultEnum addTimeTableAttribute(DimensionDTO dto){
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(DimensionPO::getDimensionTabName,dto.dimensionTabName)
                .eq(DimensionPO::getBusinessId,dto.businessId);
        DimensionPO po=mapper.selectOne(queryWrapper);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        String[] columnList = {"FullDateAlternateKey", "DayNumberOfWeek",
                "EnglishDayNameOfWeek", "DayNumberOfMonth", "DayNumberOfYear", "WeekNumberOfYear", "EnglishMonthName",
                "MonthNumberOfYear", "CalendarQuarter", "CalendarYear"};
        String[] columnDataTypeList={"DATE","INT","VARCHAR","INT","INT","INT","VARCHAR","INT","INT","INT"};
        String[] columnDataTypeLengthList={"0","0","10","0","0","0","10","0","0","0"};
        switch (typeName.toLowerCase())
        {
            case "mysql":
                break;
            case "oracle":
                columnDataTypeList=new String[]{"DATE","NUMBER","VARCHAR2","NUMBER","NUMBER","NUMBER","VARCHAR2","NUMBER","NUMBER","NUMBER"};
                break;
            case "sqlserver":
                columnDataTypeList=new String[]{"DATE","INT","VARCHAR","INT","INT","INT","VARCHAR","INT","INT","INT"};
                break;
            case "postgresql":
                columnDataTypeList=new String[]{"DATE","INT2","VARCHAR","INT2","INT2","INT2","VARCHAR","INT2","INT2","INT2"};
                break;
            case "doris":
                break;
            default:
        }
        List<DimensionAttributeDTO> list=new ArrayList<>();
        for (int i=0;i<columnList.length;i++)
        {
            DimensionAttributeDTO attributeDTO=new DimensionAttributeDTO();
            attributeDTO.attributeType= DimensionAttributeEnum.BUSINESS_KEY.getValue();
            attributeDTO.dimensionFieldCnName=columnList[i];
            attributeDTO.dimensionFieldEnName=columnList[i];
            attributeDTO.dimensionFieldType=columnDataTypeList[i];
            attributeDTO.dimensionFieldLength=Integer.parseInt(columnDataTypeLengthList[i]);
            list.add(attributeDTO);
        }
        return dimensionAttributeImpl.addTimeTableAttribute(list,(int)po.id);
    }

    /**
     * 连接数据库
     *
     * @param driver   driver
     * @param url      url
     * @param username username
     * @param password password
     * @return statement
     */
    public Connection getStatement(String driver, String url, String username, String password) {
        Connection conn;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        return conn;
    }

    @Override
    public ResultEnum updateDimension(DimensionDTO dto) throws SQLException {
        DimensionPO model=mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        QueryWrapper<DimensionPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getDimensionTabName,dto.dimensionTabName);
        DimensionPO po=mapper.selectOne(queryWrapper);
        if (po !=null && po.id !=model.id)
        {
            return ResultEnum.DIMENSION_EXIST;
        }
        if (model.timeTable && (!model.startTime.equals(dto.startTime)
                || !model.startTime.equals(dto.startTime)
                || !model.dimensionTabName.equals(dto.dimensionTabName)))
        {
            editDateDimension(dto,model.dimensionTabName);
        }
        dto.businessId=model.businessId;
        model= DimensionMap.INSTANCES.dtoToPo(dto);
        return mapper.updateById(model)>0? ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteDimension(int id)
    {
        try {
            DimensionPO model=mapper.selectById(id);
            if (model == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            //判断维度表是否存在关联
            QueryWrapper<DimensionAttributePO> queryWrapper=new QueryWrapper<>();
            queryWrapper.lambda().eq(DimensionAttributePO::getAssociateDimensionId,id);
            List<DimensionAttributePO> poList=dimensionAttributeMapper.selectList(queryWrapper);
            if (poList.size()>0)
            {
                return ResultEnum.TABLE_ASSOCIATED;
            }
            //判断维度表是否与事实表有关联
            QueryWrapper<FactAttributePO> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.lambda().eq(FactAttributePO::getAssociateDimensionId,id);
            List<FactAttributePO> factAttributePOList=factAttributeMapper.selectList(queryWrapper1);
            if (factAttributePOList.size()>0)
            {
                return ResultEnum.TABLE_ASSOCIATED;
            }
            //删除维度字段数据
            QueryWrapper<DimensionAttributePO> attributePOQueryWrapper=new QueryWrapper<>();
            attributePOQueryWrapper.select("id").lambda().eq(DimensionAttributePO::getDimensionId,id);
            List<Integer> dimensionAttributeIds=(List)dimensionAttributeMapper.selectObjs(attributePOQueryWrapper);
            if (!CollectionUtils.isEmpty(dimensionAttributeIds))
            {
                ResultEnum resultEnum = dimensionAttributeImpl.deleteDimensionAttribute(dimensionAttributeIds);
                if (ResultEnum.SUCCESS !=resultEnum)
                {
                    throw new FkException(resultEnum);
                }
            }
            //拼接删除niFi参数
            DataModelVO vo = niFiDelProcess(model.businessId, id);
            publishTaskClient.deleteNifiFlow(vo);
            //拼接删除DW/Doris库中维度表
            PgsqlDelTableDTO dto = delDwDorisTable(model.dimensionTabName);
            publishTaskClient.publishBuildDeletePgsqlTableTask(dto);

            return mapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        catch (Exception e)
        {
            log.error("deleteDimension:"+e.getMessage());
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    /**
     * 拼接niFi删除流程
     * @param businessAreaId
     * @param dimensionId
     * @return
     */
    public DataModelVO niFiDelProcess(int businessAreaId,int dimensionId)
    {
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
    public PgsqlDelTableDTO delDwDorisTable(String dimensionName)
    {
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
    public DimensionDTO getDimension(int id)
    {
        DimensionPO po=mapper.selectById(id);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        return DimensionMap.INSTANCES.poToDto(po);
    }

    @Override
    public ResultEnum updateDimensionSql(DimensionSqlDTO dto)
    {
        DimensionPO model=mapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        model.sqlScript=dto.sqlScript;
        return mapper.updateById(model)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public List<DimensionMetaDTO>getDimensionNameList(DimensionQueryDTO dto)
    {
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").lambda().eq(DimensionPO::getBusinessId,dto.businessAreaId);
        if (dto.dimensionId !=0)
        {
            queryWrapper.lambda().ne(DimensionPO::getId,dto.dimensionId);
        }
        List<DimensionPO> list=mapper.selectList(queryWrapper);
        return DimensionMap.INSTANCES.poToListNameDto(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum updateDimensionDateAttribute(DimensionDateAttributeDTO dto)
    {
        //根据业务域id,还原之前的维度表设置的日期维度
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getBusinessId,dto.businessAreaId)
                .eq(DimensionPO::getIsDimDateTbl,true);
        List<DimensionPO> list=mapper.selectList(queryWrapper);
        //获取维度字段
        QueryWrapper<DimensionAttributePO> queryWrapper1=new QueryWrapper<>();
        List<DimensionAttributePO> attributePOList=dimensionAttributeMapper.selectList(queryWrapper1);
        if (list!=null && list.size()>0)
        {
            for (DimensionPO item:list)
            {
                item.isDimDateTbl=false;
                if (mapper.updateById(item)==0)
                {
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                }
                List<DimensionAttributePO> attributePOS=attributePOList.stream()
                        .filter(e->e.dimensionId==item.id).collect(Collectors.toList());
                if (attributePOS !=null && attributePOList.size()>0)
                {
                    for (DimensionAttributePO attributePO:attributePOS)
                    {
                        attributePO.isDimDateField=false;
                        if (dimensionAttributeMapper.updateById(attributePO)==0)
                        {
                            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                        }
                    }
                }
            }
        }

        DimensionPO dimensionPO=mapper.selectById(dto.dimensionId);
        if (dimensionPO ==null)
        {
            return ResultEnum.SUCCESS;
        }
        DimensionAttributePO dimensionAttributePO=dimensionAttributeMapper.selectById(dto.dimensionAttributeId);
        if (dimensionAttributePO==null)
        {
            return ResultEnum.SUCCESS;
        }
        dimensionPO.isDimDateTbl=true;
        int flat=mapper.updateById(dimensionPO);
        if (flat==0)
        {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        dimensionAttributePO.isDimDateField=true;
        return dimensionAttributeMapper.updateById(dimensionAttributePO)>0?ResultEnum.SUCCESS:ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public DimensionDateAttributeDTO getDimensionDateAttribute(int businessId)
    {
        DimensionDateAttributeDTO data=new DimensionDateAttributeDTO();
        //查询设置时间维度表
        QueryWrapper<DimensionPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(DimensionPO::getBusinessId,businessId)
                .eq(DimensionPO::getIsDimDateTbl,true);
        List<DimensionPO> dimensionPOList=mapper.selectList(queryWrapper);
        if (dimensionPOList ==null || dimensionPOList.size()==0)
        {
            return data;
        }
        data.dimensionId=dimensionPOList.get(0).id;
        //查询设置时间维度表字段
        QueryWrapper<DimensionAttributePO> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.lambda()
                .eq(DimensionAttributePO::getDimensionId,data.dimensionId)
                .eq(DimensionAttributePO::getIsDimDateField,true);
        List<DimensionAttributePO> dimensionAttributePOList=dimensionAttributeMapper.selectList(queryWrapper1);
        if (dimensionAttributePOList ==null || dimensionAttributePOList.size()==0)
        {
            return data;
        }
        data.dimensionAttributeId=dimensionAttributePOList.get(0).id;
        return data;
    }

    @Override
    public void updateDimensionPublishStatus(ModelPublishStatusDTO dto)
    {
        DimensionPO po=mapper.selectById(dto.id);
        if (po !=null)
        {
            //0:DW发布状态
            if (dto.type==0)
            {
                po.isPublish=dto.status;
            }else {
                po.dorisPublish=dto.status;
            }
            mapper.updateById(po);
        }
    }

}
