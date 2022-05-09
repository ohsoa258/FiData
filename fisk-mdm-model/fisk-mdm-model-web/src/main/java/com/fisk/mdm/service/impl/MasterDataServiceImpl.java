package com.fisk.mdm.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.masterdata.ImportDataQueryDTO;
import com.fisk.mdm.dto.masterdata.ImportDataSubmitDTO;
import com.fisk.mdm.dto.masterdata.ImportParamDTO;
import com.fisk.mdm.dto.stgbatch.StgBatchDTO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.enums.AttributeSyncStatusEnum;
import com.fisk.mdm.enums.ImportTypeEnum;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.utlis.DataSynchronizationUtils;
import com.fisk.mdm.vo.masterdata.BathUploadMemberListVo;
import com.fisk.mdm.vo.masterdata.BathUploadMemberVO;
import com.fisk.mdm.vo.masterdata.ExportResultVO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.map.AttributeMap;
import com.fisk.mdm.mapper.AttributeMapper;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.IMasterDataService;
import com.fisk.mdm.vo.attribute.AttributeColumnVO;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.mdm.vo.resultObject.ResultObjectVO;
import com.google.common.base.Joiner;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * 主数据服务impl
 *
 * @author ChenYa
 * @date 2022/04/25
 */
@Slf4j
@Service
public class MasterDataServiceImpl implements IMasterDataService {

    @Resource
    EntityService entityService;
    /*@Resource
    DataSynchronizationUtils dataSynchronizationUtils;*/
    @Resource
    StgBatchServiceImpl stgBatchService;

    @Resource
    AttributeMapper attributeMapper;
    @Resource
    EntityMapper entityMapper;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    UserHelper userHelper;

    String connectionStr = "jdbc:postgresql://192.168.1.250:5432/dmp_mdm?stringtype=unspecified";
    String acc = "postgres";
    String pwd = "Password01!";

    /**
     * 系统字段
     */
    String systemColumnName = ",fidata_id," +
            "fidata_version_id," +
            "fidata_create_time," +
            "fidata_create_user," +
            "fidata_update_time," +
            "fidata_update_user";

    static {
        //加载pg数据库驱动
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据实体id查询主数据
     *
     * @param entityId 实体id
     */
    @Override
    public ResultEntity<ResultObjectVO> getAll(Integer entityId, Integer modelVersionId){

        if(entityId == null || modelVersionId == null){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS,ResultEnum.DATA_NOTEXISTS.getMsg());
        }

        //准备返回对象
        ResultObjectVO resultObjectVO = new ResultObjectVO();

        EntityVO entityVo = entityService.getDataById(entityId);
        if(entityVo == null){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS,ResultEnum.DATA_NOTEXISTS.getMsg());
        }
        //获得主数据表名
        String tableName = "viw_"+entityService.getDataById(entityId).getModelId()+"_"+entityId;

        //查询该实体下发布的属性
        QueryWrapper<AttributePO> attributeColumnWrapper = new QueryWrapper<>();
        attributeColumnWrapper.lambda().eq(AttributePO::getStatus, AttributeStatusEnum.SUBMITTED)
                .eq(AttributePO::getEntityId,entityId);
        List<AttributePO> attributePoList = attributeMapper.selectList(attributeColumnWrapper);
        if(attributePoList.isEmpty()){
            return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS,ResultEnum.DATA_NOTEXISTS.getMsg());
        }

        //将查询到的属性集合添加装入结果对象
        List<AttributeColumnVO> attributeColumnVoList = AttributeMap.INSTANCES.poToColumnVoList(attributePoList);
        resultObjectVO.setAttributeColumnVoList(attributeColumnVoList);

        //获得业务字段名
        List<String> list = new ArrayList<>();
        for (AttributeColumnVO attributeColumnVo:attributeColumnVoList){
            list.add(attributeColumnVo.getName());
        }
        String businessColumnName = StringUtils.join(list, ",");

        //拼接sql语句
        String sql = "select "+ businessColumnName + systemColumnName  + " from "+tableName + " view " +
                "where fidata_del_flag = 1 and fidata_version_id = " + modelVersionId;

        //准备主数据集合
        List<Map<String,Object>> data = new ArrayList<>();

        try {
            //获得工厂
            Connection connection = getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            //执行sql，获得结果集
            log.info("执行sql: 【" + sql + "】");
            ResultSet resultSet = statement.executeQuery(sql);
            //判断结果集是否为空
            if(!resultSet.next()){
                return ResultEntityBuild.build(ResultEnum.DATA_NOTEXISTS,ResultEnum.DATA_NOTEXISTS.getMsg());
            }
            //获取结果集的结构信息
            ResultSetMetaData metaData = resultSet.getMetaData();
            //重置结果集游标，遍历结果集，取出数据
            resultSet.beforeFirst();
            while (resultSet.next()){
                //用map接收对象
                Map<String,Object> map = new HashMap<>();
                //遍历每一行数据，取出每一个字段名与其对应值
                for (int i = 1 ; i <=metaData.getColumnCount() ; i++){
                    map.put(metaData.getColumnName(i),resultSet.getString(metaData.getColumnName(i)));
                }
                //将接收到的对象放入主数据集合中
                data.add(map);
            }
            //将主数据集合添加装入结果对象
            resultObjectVO.setResultData(data);
            //释放资源
            release(resultSet,statement,connection);
            return ResultEntityBuild.build(ResultEnum.SUCCESS,resultObjectVO);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS,resultObjectVO);
    }

    /**
     * 获得连接
     *
     * @return {@link Connection}
     */
    public Connection getConnection(){
        try {
            Connection connection = DriverManager.getConnection(connectionStr, acc, pwd);
            log.info("【connection】数据库连接成功, 连接信息【" + connectionStr + "】");
            return connection;
        } catch (SQLException e) {
            log.error("【connection】数据库连接获取失败, ex", e);
            throw new FkException(ResultEnum.VISUAL_CONNECTION_ERROR, e.getLocalizedMessage());
        }
    }

    /**
     * 释放资源
     *
     * @param rs   ResultSet
     * @param stmt Statement
     * @param conn Connection
     */
    public static void release(ResultSet rs , Statement stmt , Connection conn){
        if(rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            rs=null;
        }
        if(stmt!=null){
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            stmt=null;
        }
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            conn=null;
        }
    }

    /**
     * 执行查询sql
     *
     * @param sql        sql
     * @param connection 连接
     * @throws SQLException sqlexception异常
     */
    public ResultSet executeSelectSql(String sql,Connection connection) {
        try {
            Statement statement =connection.createStatement();
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            log.info("执行sql: 【" + sql + "】");
            return statement.executeQuery(sql);
        }
        catch (SQLException e)
        {
            log.error("executeSelectSql:",e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR,e);
        }
    }

    /***
     * 下载模板
     * @param entityId
     * @param response
     * @return
     */
    @Override
    public ResultEnum downloadTemplate(int entityId, HttpServletResponse response)
    {
        EntityPO entityPo=entityMapper.selectById(entityId);
        if (entityPo==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        ExportResultVO vo=new ExportResultVO();
        QueryWrapper<AttributePO> queryWrapper=new QueryWrapper<>();
        //发布状态、字段状态均为成功
        queryWrapper.select("display_name").lambda()
                .eq(AttributePO::getEntityId,entityId)
                .eq(AttributePO::getStatus,AttributeStatusEnum.SUBMITTED.getValue())
                .eq(AttributePO::getSyncStatus, AttributeSyncStatusEnum.SUCCESS.getValue());
        vo.headerList=(List)attributeMapper.selectObjs(queryWrapper);
        vo.headerList.add(2,"新编码");
        vo.fileName=entityPo.getDisplayName();
        return exportExcel(vo,response);
    }

    /**
     * 导出Excel
     * @param vo
     * @param response
     * @return
     */
    public ResultEnum exportExcel(ExportResultVO vo,HttpServletResponse response)
    {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet1");
        XSSFRow row1 = sheet.createRow(0);
        if (CollectionUtils.isEmpty(vo.headerList))
        {
            ResultEntityBuild.build(ResultEnum.VISUAL_QUERY_ERROR);
        }
        for (int i = 0; i < vo.headerList.size(); i++) {
            row1.createCell(i).setCellValue(vo.headerList.get(i));
        }
        if (!CollectionUtils.isEmpty(vo.dataArray))
        {
            for (int i=0;i<vo.dataArray.size();i++)
            {
                XSSFRow row = sheet.createRow(i+1);
                JSONObject jsonObject = JSONObject.parseObject(vo.dataArray.get(i).toString());
                for (int j = 0; j < vo.headerList.size(); j++)
                {
                    row.createCell(j).setCellValue(jsonObject.get(vo.headerList.get(j)).toString());
                }
            }
        }
        //将文件存到指定位置
        try {
            //输出Excel文件
            OutputStream output=response.getOutputStream();
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.addHeader("Content-Disposition", "attachment;filename=fileName" + ".xlsx");
            workbook.write(output);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new FkException(ResultEnum.SQL_ANALYSIS,e);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public BathUploadMemberListVo importTemplateData(ImportParamDTO dto, MultipartFile file)
    {
        EntityPO po=entityMapper.selectById(dto.entityId);
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        QueryWrapper<AttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(AttributePO::getEntityId,dto.entityId);
        List<AttributePO> list=attributeMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list))
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        BathUploadMemberListVo listVo=new BathUploadMemberListVo();
        BathUploadMemberVO result=new BathUploadMemberVO();
        result.attribute=AttributeMap.INSTANCES.poListToVoList(list);
        result.versionId=dto.versionId;
        result.entityId=dto.entityId;
        result.entityName=po.getDisplayName();
        List<JSONObject> objectArrayList=new ArrayList<>();
        ////List<String> codeList = getCodeList(po.getTableName().replace("mdm","stg"));
        //创建工作簿
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(file.getInputStream());
            //获取sheet数量
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++)
            {
                //获取当前工作表
                Sheet sheet = workbook.getSheetAt(i);
                //列数
                int columnNum=sheet.getRow(0).getPhysicalNumberOfCells();
                //获得总行数
                int rowNum=sheet.getPhysicalNumberOfRows();
                Row row1 = sheet.getRow(0);
                List<AttributeInfoDTO> attributePoList=new ArrayList<>();
                for (int col=0;col<columnNum;col++)
                {
                    Cell cell = row1.getCell(col);
                    if (cell.getStringCellValue().equals("新编码"))
                    {
                        AttributeInfoDTO newCode=new AttributeInfoDTO();
                        newCode.setName("fidata_new_code");
                        attributePoList.add(newCode);
                        continue;
                    }
                    Optional<AttributePO> data = list.stream().filter(e -> cell.getStringCellValue().equals(e.getDisplayName())).findFirst();
                    if (!data.isPresent())
                    {
                        throw new FkException(ResultEnum.EXIST_INVALID_COLUMN);
                    }
                    attributePoList.add(AttributeMap.INSTANCES.poToInfoDto(data.get()));
                }
                for (int row=1;row<rowNum;row++)
                {
                    JSONObject jsonObj = new JSONObject();
                    List<String> errorAttribute=new ArrayList<>();
                    Row nowRow = sheet.getRow(row);
                    String errorMsg="";
                    result.count+=1;
                    for (int col=0;col<columnNum;col++)
                    {
                        Cell cell = nowRow.getCell(col);
                        String value="";
                        //判断字段类型
                        if (cell !=null)
                        {
                            value=getCellDataType(cell);
                        }
                        jsonObj.put(attributePoList.get(col).getName(),value);
                    }
                    jsonObj.put("UploadStatus","2");
                    jsonObj.put("ErrorMsg",errorMsg);
                    jsonObj.put("ErrorStatus",errorAttribute.size() > 0 ? 1 : 2);
                    jsonObj.put("internalId","");
                    jsonObj.put("ErrorAttribute",errorAttribute);
                    if (errorAttribute.size()>0)
                    {
                        result.errorCount+=1;
                    }else {
                        result.successCount+=1;
                    }
                    objectArrayList.add(jsonObj);
                }
                result.members=objectArrayList;
            }
            String guid=UUID.randomUUID().toString();
            List<BathUploadMemberVO> bathUploadMemberVOList=new ArrayList<>();
            bathUploadMemberVOList.add(result);
            listVo.key=guid;
            listVo.list=bathUploadMemberVOList;
            redisTemplate.opsForValue().set("importTemplateData:"+guid,JSONArray.toJSON(listVo).toString());
            return listVo;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.SQL_ANALYSIS,e);
        }
    }

    @Override
    public BathUploadMemberVO importDataQuery(ImportDataQueryDTO dto)
    {
        Boolean exist = redisTemplate.hasKey("importTemplateData:"+dto.key);
        if (exist) {
            String jsonStr = redisTemplate.opsForValue().get("importTemplateData:"+dto.key).toString();
            BathUploadMemberListVo data=JSONObject.parseObject(jsonStr,BathUploadMemberListVo.class);

            Optional<BathUploadMemberVO> first = data.list.stream().filter(e -> dto.entityId == e.entityId).findFirst();
            if (!first.isPresent())
            {
                throw new FkException(ResultEnum.KEY_DATA_NOT_FOUND);
            }
            first.get().members=startPage(first.get().members,dto.pageIndex,dto.pageSize);
            return first.get();
        }
        throw new FkException(ResultEnum.KEY_DATA_NOT_FOUND);
    }

    @Override
    public ResultEnum importDataSubmit(ImportDataSubmitDTO dto) throws SQLException {
        Connection conn=null;
        Statement stat=null;
        try {
            Boolean exist = redisTemplate.hasKey("importTemplateData:"+dto.key);
            if (!exist) {
                return ResultEnum.KEY_DATA_NOT_FOUND;
            }
            String jsonStr = redisTemplate.opsForValue().get("importTemplateData:"+dto.key).toString();
            BathUploadMemberListVo data=JSONObject.parseObject(jsonStr,BathUploadMemberListVo.class);
            if (data==null || CollectionUtils.isEmpty(data.list))
            {
                return ResultEnum.KEY_DATA_NOT_FOUND;
            }
            Date date=new Date();
            String batchCode=UUID.randomUUID().toString();
            for(BathUploadMemberVO item:data.list){
                if (CollectionUtils.isEmpty(item.members))
                {
                    continue;
                }
                item.members=item.members.stream().filter(e->e.getString("ErrorStatus").equals("2")).collect(Collectors.toList());
                EntityPO entityPO=entityMapper.selectById(item.entityId);
                if (entityPO==null)
                {
                    continue;
                }
                List<JSONObject> addMemberList= item.members.stream()
                        .filter(e->e.getString("UploadStatus").equals("2"))
                        .collect(Collectors.toList());
                conn = getConnection();
                stat = conn.createStatement();
                if (!CollectionUtils.isEmpty(addMemberList))
                {
                    StringBuilder str=new StringBuilder();
                    for (int i=0;i<addMemberList.size();i++)
                    {

                        if (i==0)
                        {
                            str.append("insert into "+entityPO.getTableName().replace("mdm","stg"));
                            str.append("("+getColumnNameAndValue(addMemberList.get(i),0));
                            str.append(",fidata_import_type,fidata_batch_code,fidata_status,fidata_version_id," +
                                    "fidata_create_time,fidata_create_user,fidata_del_flag");
                            str.append(")");
                            str.append(" values("+getColumnNameAndValue(addMemberList.get(i),1)+","
                                    + ImportTypeEnum.EXCEL_IMPORT.getValue()+",'"+batchCode+"',"+0+","
                                    +item.versionId+",'"+getFormatDate(date)+"',"+userHelper.getLoginUserInfo().id+",1"+")");
                            continue;
                        }
                        str.append(",("+getColumnNameAndValue(addMemberList.get(i),1)+","
                                + ImportTypeEnum.EXCEL_IMPORT.getValue()+",'"+batchCode+"',"+0+","
                                +item.versionId+",'"+getFormatDate(date)+"',"+userHelper.getLoginUserInfo().id+",1"+")");
                    }
                    log.info("模板批量添加sql:",str.toString());
                    stat.addBatch(str.toString());
                    stat.executeBatch();
                    //String aa="";
                }
                //setStgBatch();
                //dataSynchronizationUtils.stgDataSynchronize(item.entityId,"batchCode");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new FkException(ResultEnum.SQL_ANALYSIS,e);
        }
        finally {
            stat.close();
            conn.close();
        }
        redisTemplate.delete("importTemplateData:"+dto.key);
        return ResultEnum.SUCCESS;
    }

    /**
     * 获取对象键值对
     * @param member
     * @param type
     * @return
     */
    public String getColumnNameAndValue(JSONObject member,int type)
    {
        List<String> columnList=new ArrayList<>();
        Iterator iter = member.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name=entry.getKey().toString();if (name.equals("ErrorStatus")
                    || name.equals("internalId")
                    || name.equals("UploadStatus")
                    || name.equals("ErrorStatus")
                    || name.equals("ErrorAttribute")
                    || name.equals("ErrorMsg"))
            {
                continue;
            }
            //获取列名
            if (type==0)
            {
                columnList.add(name);
            }
            //拼接value
            else{
                columnList.add("'"+entry.getValue().toString()+"'");
            }
        }
        return Joiner.on(",").join(columnList);
    }

    /**
     * 获取Excel表格数据类型
     * @param cell
     * @return
     */
    public String getCellDataType(Cell cell){
        String value="";
        switch (cell.getCellType()) {
            //字符串
            case Cell.CELL_TYPE_STRING:
                value=cell.getStringCellValue();
                break;
            //公式
            case Cell.CELL_TYPE_FORMULA:
                break;
            //数字
            case Cell.CELL_TYPE_NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell))
                {
                    value=getFormatDate(cell.getDateCellValue());
                }else {
                    value=String.valueOf(cell.getNumericCellValue());
                }
                break;
            //空白
            case Cell.CELL_TYPE_BLANK:
                break;
            //布尔值
            case Cell.CELL_TYPE_BOOLEAN:
                value=String.valueOf(cell.getBooleanCellValue());
                break;
            //错误值=CELL_TYPE_ERROR
            default:
                //return ResultEnum.EXIST_ERROR_DATA;
        }
        return value;
    }

    /**
     * 时间格式化
     * @param date
     * @return
     */
    public static String getFormatDate(Date date) {
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return myFormat.format(date);
    }

    /**
     * 添加批次日志
     * @param batchCode
     * @param entityId
     * @param versionId
     * @param totalCount
     * @param errorCount
     */
    public void setStgBatch(String batchCode,int entityId,int versionId,int totalCount,int errorCount){
        StgBatchDTO stgBatchDto=new StgBatchDTO();
        stgBatchDto.batchCode=batchCode;
        stgBatchDto.entityId=entityId;
        stgBatchDto.versionId=versionId;
        stgBatchDto.status=0;
        stgBatchDto.totalCount=totalCount;
        stgBatchDto.errorCount=errorCount;
        stgBatchService.addStgBatch(stgBatchDto);
    }

    /**
     * list集合分页
     * @param list
     * @param pageNum
     * @param pageSize
     * @return
     */
    public static List startPage(List list, Integer pageNum,Integer pageSize) {
        if (list == null) {
            return null;
        }
        if (list.size() == 0) {
            return null;
        }
        Integer count = list.size(); // 记录总数
        Integer pageCount = 0; // 页数
        if (count % pageSize == 0) {
            pageCount = count / pageSize;
        } else {
            pageCount = count / pageSize + 1;
        }
        int fromIndex = 0; // 开始索引
        int toIndex = 0; // 结束索引

        if (!pageNum.equals(pageCount)) {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = fromIndex + pageSize;
        } else {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = count;
        }
        List pageList = list.subList(fromIndex, toIndex);
        return pageList;
    }

    /**
     * 获取stg表code数据集合
     * @param tableName
     * @return
     */
    public List<String> getCodeList(String tableName) {
        List<String> codeList=new ArrayList<>();
        try {
            String sql = "SELECT code  from " + tableName;
            Connection conn = getConnection();
            ResultSet rs = executeSelectSql(sql, conn);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    //获取sql查询数据集合
                    codeList.add(rs.getString("code"));
                }
            }
        }
        catch (SQLException e)
        {
            log.error("getCodeList:",e);
        }
        return codeList;
    }

}
