package com.fisk.mdm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.masterdata.ImportDataQueryDTO;
import com.fisk.mdm.dto.masterdata.ImportDataSubmitDTO;
import com.fisk.mdm.dto.masterdata.ImportParamDTO;
import com.fisk.mdm.dto.masterdata.MasterDataQueryDTO;
import com.fisk.mdm.dto.stgbatch.StgBatchDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.enums.AttributeStatusEnum;
import com.fisk.mdm.enums.AttributeSyncStatusEnum;
import com.fisk.mdm.enums.DataTypeEnum;
import com.fisk.mdm.enums.ImportTypeEnum;
import com.fisk.mdm.map.AttributeMap;
import com.fisk.mdm.map.ModelMap;
import com.fisk.mdm.mapper.AttributeMapper;
import com.fisk.mdm.mapper.EntityMapper;
import com.fisk.mdm.mapper.ModelMapper;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.IMasterDataService;
import com.fisk.mdm.utlis.DataSynchronizationUtils;
import com.fisk.mdm.vo.attribute.AttributeColumnVO;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.mdm.vo.masterdata.BathUploadMemberListVo;
import com.fisk.mdm.vo.masterdata.BathUploadMemberVO;
import com.fisk.mdm.vo.masterdata.ExportResultVO;
import com.fisk.mdm.vo.model.ModelDropDownVO;
import com.fisk.mdm.vo.resultObject.ResultAttributeGroupVO;
import com.fisk.mdm.vo.resultObject.ResultObjectVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.userinfo.UserDropDTO;
import com.google.common.base.Joiner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.CountDownLatch;
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
    @Resource
    DataSynchronizationUtils dataSynchronizationUtils;
    @Resource
    StgBatchServiceImpl stgBatchService;
    @Resource
    ModelVersionServiceImpl modelVersionServiceImpl;
    @Resource
    EntityServiceImpl entityServiceImpl;

    @Resource
    AttributeMapper attributeMapper;
    @Resource
    EntityMapper entityMapper;
    @Resource
    ModelMapper modelMapper;
    @Resource
    UserHelper userHelper;
    @Resource
    UserClient client;

    @Value("${pgsql-mdm.type}")
    private DataSourceTypeEnum type;
    @Value("${pgsql-mdm.url}")
    private String url;
    @Value("${pgsql-mdm.username}")
    private String username;
    @Value("${pgsql-mdm.password}")
    private String password;

    /**
     * 系统字段
     */
    String systemColumnName = ",fidata_id," +
            "fidata_version_id," +
            "fidata_create_time," +
            "fidata_create_user," +
            "fidata_update_time," +
            "fidata_update_user";

    /**
     * 根据实体id查询主数据
     *
     * @param dto 实体id
     */
    @Override
    public ResultObjectVO getMasterDataPage(MasterDataQueryDTO dto){
        //准备返回对象
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        EntityVO entityVo = entityService.getDataById(dto.getEntityId());
        if(entityVo == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //获得主数据表名
        String tableName = "viw_"+entityVo.getModelId()+"_"+dto.getEntityId();
        //查询该实体下发布的属性
        QueryWrapper<AttributePO> attributeColumnWrapper = new QueryWrapper<>();
        attributeColumnWrapper.lambda().eq(AttributePO::getStatus, AttributeStatusEnum.SUBMITTED)
                .eq(AttributePO::getEntityId,dto.getEntityId());
        List<AttributePO> attributePoList = attributeMapper.selectList(attributeColumnWrapper);
        if(attributePoList.isEmpty()){
            throw new FkException(ResultEnum.ATTRIBUTE_NOT_EXIST);
        }
        //将查询到的属性集合添加装入结果对象
        List<AttributeColumnVO> attributeColumnVoList = AttributeMap.INSTANCES.poToColumnVoList(attributePoList);
        //数据类型英文名称赋值
        attributeColumnVoList
                .stream()
                .map(e->e.dataTypeEnDisplay =DataTypeEnum.getValue(e.getDataType()).name())
                .collect(Collectors.toList());
        List<ResultAttributeGroupVO> attributeGroupVoList=new ArrayList<>();
        ResultAttributeGroupVO attributeGroupVo=new ResultAttributeGroupVO();
        attributeGroupVo.setName("属性1");
        attributeGroupVo.setAttributes(attributeColumnVoList);
        attributeGroupVoList.add(attributeGroupVo);
        resultObjectVO.setAttributes(attributeGroupVoList);
        //获得业务字段名
        List<String> list = new ArrayList<>();
        for (AttributeColumnVO attributeColumnVo:attributeColumnVoList){
            list.add(attributeColumnVo.getName());
        }
        String businessColumnName = StringUtils.join(list, ",");
        //准备主数据集合
        List<Map<String,Object>> data = new ArrayList<>();
        try {
            //获得工厂
            Connection connection = getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            //获取总条数
            String getTotalSql = "select count(*) as totalNum from "+tableName + " view ";
            ResultSet rSet = statement.executeQuery(getTotalSql);
            int rowCount = 0;
            if (rSet.next()) {
                rowCount = rSet.getInt("totalNum");
            }
            rSet.close();
            resultObjectVO.setTotal(rowCount);
            //分页获取数据
            int offset = (dto.getPageIndex() - 1) * dto.getPageSize();
            //拼接sql语句
            StringBuilder str=new StringBuilder();
            str.append("select "+businessColumnName + systemColumnName);
            str.append(" from "+tableName + " view ");
            str.append("where fidata_del_flag = 1 and fidata_version_id = " + dto.getVersionId());
            str.append(" order by fidata_create_time,fidata_id desc ");
            str.append(" limit "+ dto.getPageSize() + " offset " + offset);
            //执行sql，获得结果集
            log.info("执行sql: 【" + str.toString() + "】");
            ResultSet resultSet = statement.executeQuery(str.toString());
            //判断结果集是否为空
            if(!resultSet.next()){
                resultObjectVO.setResultData(new ArrayList<>());
                return resultObjectVO;
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
            resultObjectVO.setResultData(getUserName(data));
            //释放资源
            release(resultSet,statement,connection);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getMasterDataPage:", e);
            resultObjectVO.setErrorMsg(e.getMessage());
        }
        return resultObjectVO;
    }

    /**
     * 获取创建人/更新人
     * @param data
     * @return
     */
    public List<Map<String,Object>>  getUserName(List<Map<String,Object>> data){
        ResultEntity<List<UserDropDTO>> resultData = client.listUserDrops();
        if (resultData.code!=ResultEnum.SUCCESS.getCode())
        {
            log.error("getUserName:",resultData.msg);
            return data;
        }
        for (Map<String,Object> item:data) {
            String createUserId = item.get("fidata_create_user") == null ? "" : item.get("fidata_create_user").toString();
            Optional<UserDropDTO> createUser = resultData.data.stream().filter(e -> createUserId.equals(String.valueOf(e.id))).findFirst();
            if (createUser.isPresent()) {
                item.put("fidata_create_user", createUser.get().username);
            }
            String updateUserId = item.get("fidata_update_user") == null ? "" : item.get("fidata_update_user").toString();
            Optional<UserDropDTO> updateUser = resultData.data.stream().filter(e -> updateUserId.equals(String.valueOf(e.id))).findFirst();
            if (updateUser.isPresent()) {
                item.put("fidata_update_user", updateUser.get().username);
            }
        }
        return data;
    }

    @Override
    public List<ModelDropDownVO> getModelEntityVersionStruct()
    {
        QueryWrapper<ModelPO> queryWrapper=new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<ModelPO> modelPoList=modelMapper.selectList(queryWrapper);
        List<ModelDropDownVO> data= ModelMap.INSTANCES.poListToDropDownVoList(modelPoList);
        data.stream().forEach(e->{
            e.versions= modelVersionServiceImpl.getModelVersionDropDown(e.id);
            e.versions.stream().map(p->p.displayName=p.name).collect(Collectors.toList());
            e.children=entityServiceImpl.getEntityDropDown(e.id);
        });
        return data;
    }

    /**
     * 连接Connection
     *
     * @return {@link Connection}
     */
    public Connection getConnection() {
        AbstractDbHelper dbHelper = new AbstractDbHelper();
        Connection connection = dbHelper.connection(url, username,
                password, type);
        return connection;
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
        vo.headerList.add(1,"新编码");
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
        if (!file.getOriginalFilename().contains(".xlsx"))
        {
            throw new FkException(ResultEnum.FILE_NAME_ERROR);
        }
        EntityPO po=entityMapper.selectById(dto.getEntityId());
        if (po==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        QueryWrapper<AttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(AttributePO::getEntityId,dto.getEntityId());
        List<AttributePO> list=attributeMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list))
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        BathUploadMemberListVo listVo = new BathUploadMemberListVo();
        BathUploadMemberVO result = new BathUploadMemberVO();
        result.versionId = dto.getVersionId();
        result.entityId = dto.getEntityId();
        result.entityName = po.getDisplayName();
        List<JSONObject> objectArrayList = new ArrayList<>();
        String tableName = po.getTableName().replace("mdm", "stg");
        List<String> codeList = getCodeList(tableName);
        String batchCode = UUID.randomUUID().toString();
        List<Integer> successCountList = new ArrayList<>();
        //创建工作簿
        Workbook workbook;
        try {
            workbook = WorkbookFactory.create(file.getInputStream());
            //获取sheet数量
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                //获取当前工作表
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet.getRow(0) == null) {
                    continue;
                }
                //列数
                int columnNum = sheet.getRow(0).getPhysicalNumberOfCells();
                //获得总行数
                int rowNum = sheet.getPhysicalNumberOfRows();
                Row row1 = sheet.getRow(0);
                List<AttributeInfoDTO> attributePoList = new ArrayList<>();
                //获取表头
                for (int col = 0; col < columnNum; col++) {
                    Cell cell = row1.getCell(col);
                    if (cell.getStringCellValue().equals("新编码")) {
                        AttributeInfoDTO newCode = new AttributeInfoDTO();
                        newCode.setName("fidata_new_code");
                        attributePoList.add(newCode);
                        continue;
                    }
                    Optional<AttributePO> data = list.stream().filter(e -> cell.getStringCellValue().equals(e.getDisplayName())).findFirst();
                    if (!data.isPresent()) {
                        throw new FkException(ResultEnum.EXIST_INVALID_COLUMN);
                    }
                    attributePoList.add(AttributeMap.INSTANCES.poToInfoDto(data.get()));
                }
                result.attribute = attributePoList;
                result.count = rowNum - 1;
                //每个线程执行条数
                final int threadHandleNumber = 1000;
                //线程数
                int threadCount = rowNum / threadHandleNumber == 0 ? rowNum / threadHandleNumber : (rowNum / threadHandleNumber) + 1;
                final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
                long userId = userHelper.getLoginUserInfo().id;
                for (int thread = 0; thread < threadCount; thread++) {
                    int start = thread * threadHandleNumber + 1;
                    int end = (thread + 1) * threadHandleNumber > rowNum ? rowNum : ((thread + 1) * threadHandleNumber) + 1;
                    List<JSONObject> objectList = new ArrayList<>();
                    new Thread(new Runnable() {
                        @SneakyThrows
                        @Override
                        public void run() {
                            try {
                                for (int row = start; row < end; row++) {
                                    JSONObject jsonObj = new JSONObject();
                                    Row nowRow = sheet.getRow(row);
                                    for (int col = 0; col < columnNum; col++) {
                                        Cell cell = nowRow.getCell(col);
                                        String value = "";
                                        //判断字段类型
                                        if (cell != null) {
                                            value = getCellDataType(cell,
                                                    attributePoList.get(col).getDataType(),
                                                    attributePoList.get(col).getDataTypeDecimalLength() == null ? 0 : attributePoList.get(col).getDataTypeDecimalLength());
                                        }
                                        if ("code".equals(attributePoList.get(col).getName()) && StringUtils.isEmpty(value)) {
                                            value = UUID.randomUUID().toString();
                                        }
                                        jsonObj.put(attributePoList.get(col).getName(), dto.removeSpace == true ? value.trim() : value);
                                    }
                                    //上传逻辑：1 修改 2 新增
                                    if (codeList.contains(jsonObj.get("code"))) {
                                        jsonObj.put("fidata_syncy_type", "1");
                                        //result.updateCount+=1;
                                    } else {
                                        jsonObj.put("fidata_syncy_type", "2");
                                        //result.addCount+=1;
                                    }
                                    jsonObj.put("fidata_error_msg", "");
                                    jsonObj.put("internalId", "");
                                    //0：上传成功（数据进入stg表） 1：提交成功（数据进入mdm表） 2：提交失败（数据进入mdm表失败）
                                    jsonObj.put("fidata_status", "0");
                                    objectList.add(jsonObj);
                                }
                                if (!CollectionUtils.isEmpty(objectList)) {
                                    objectArrayList.addAll(objectList);
                                    int flatCount = templateDataSubmitStg(objectList, tableName, batchCode, dto.getVersionId(), userId);
                                    successCountList.add(flatCount);
                                }
                            } catch (Exception e) {
                                log.error("importTemplateData thread:", e);
                            } finally {
                                countDownLatch.countDown();
                            }
                        }
                    }).start();
                }
                //等待所有线程执行完毕
                countDownLatch.await();
            }
            Integer successCount = successCountList.stream().reduce(Integer::sum).orElse(0);
            //添加批次
            setStgBatch(batchCode, dto.getEntityId(), dto.getVersionId(), result.count, result.count - successCount, successCount > 0 ? 0 : 1);
            result.members = objectArrayList;
            List<BathUploadMemberVO> bathUploadMemberVOList = new ArrayList<>();
            bathUploadMemberVOList.add(result);
            listVo.key = batchCode;
            listVo.list = bathUploadMemberVOList;
            return listVo;
        } catch (Exception e) {
            log.error("importTemplateData", e);
            throw new FkException(ResultEnum.SQL_ANALYSIS, e);
        }
    }

    /**
     * 模板数据提交到stg
     *
     * @param members
     * @param tableName
     * @param batchCode
     * @param versionId
     * @param userId
     * @return
     * @throws SQLException
     */
    public int templateDataSubmitStg(List<JSONObject> members,
                                     String tableName,
                                     String batchCode,
                                     int versionId,
                                     long userId) throws SQLException {
        Connection conn = null;
        Statement stat = null;
        try {
            Date date = new Date();
            StringBuilder str = new StringBuilder();
            conn = getConnection();
            stat = conn.createStatement();
            str.append("insert into " + tableName);
            str.append("(" + getColumnNameAndValue(members.get(0), 0));
            str.append(",fidata_import_type,fidata_batch_code,fidata_version_id,");
            str.append("fidata_create_time,fidata_create_user,fidata_update_time,fidata_update_user,fidata_del_flag");
            str.append(")");
            str.append(" values(" + getColumnNameAndValue(members.get(0), 1) + ","
                    + ImportTypeEnum.EXCEL_IMPORT.getValue() + ",'" + batchCode + "'," + versionId + ",'");
            str.append(getFormatDate(date, DataTypeEnum.TIMESTAMP.getName()) + "'," + userId + ",'");
            str.append(getFormatDate(date, DataTypeEnum.TIMESTAMP.getName()) + "'," + userId + ",1" + ")");
            if (members.size() > 1) {
                for (int i = 1; i < members.size(); i++) {
                    str.append(",(" + getColumnNameAndValue(members.get(i), 1) + ","
                            + ImportTypeEnum.EXCEL_IMPORT.getValue() + ",'" + batchCode + "',"
                            + versionId + ",'");
                    str.append(getFormatDate(date, DataTypeEnum.TIMESTAMP.getName()) + "'," + userId + ",'");
                    str.append(getFormatDate(date, DataTypeEnum.TIMESTAMP.getName()) + "'," + userId + ",1" + ")");
                }
            }
            log.info("模板批量添加sql:", str.toString());
            stat.addBatch(str.toString());
            int[] flatCount = stat.executeBatch();
            return flatCount[0];
        } catch (Exception e) {
            log.error("templateDataSubmitStg:", e);
            throw new FkException(ResultEnum.DATA_SUBMIT_ERROR, e);
        } finally {
            stat.close();
            conn.close();
        }

    }

    @Override
    public BathUploadMemberVO importDataQuery(ImportDataQueryDTO dto)
    {
        EntityPO entityPO=entityMapper.selectById(dto.entityId);
        if (entityPO==null)
        {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        BathUploadMemberVO vo=new BathUploadMemberVO();
        vo.entityName=entityPO.getDisplayName();
        vo.entityId=dto.entityId;
        QueryWrapper<AttributePO> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(AttributePO::getEntityId,dto.entityId);
        List<AttributePO> list=attributeMapper.selectList(queryWrapper);
        try {
            Connection connection = getConnection();
            Statement st = connection.createStatement();
            String tableName=entityPO.getTableName().replace("mdm","stg");
            //获取总条数
            String getTotalSql = "select count(*) as totalNum from " + tableName+" where fidata_batch_code='"+dto.key+"'";
            ResultSet rSet = st.executeQuery(getTotalSql);
            int rowCount = 0;
            if (rSet.next()) {
                rowCount = rSet.getInt("totalNum");
            }
            rSet.close();
            vo.count=rowCount;
            //分页获取数据
            int offset = (dto.pageIndex - 1) * dto.pageSize;
            StringBuilder str=new StringBuilder();
            str.append("select * from "+entityPO.getTableName().replace("mdm","stg"));
            str.append(" where fidata_batch_code='"+dto.key+"'");
            str.append(" limit "+ dto.pageSize + " offset " + offset);
            ResultSet rs = st.executeQuery(str.toString());
            // 获取列数
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            //获取列名
            List<AttributeInfoDTO> attributes = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Optional<AttributePO> first = list.stream().filter(e -> columnName.equals(e.getName())).findFirst();
                if (!first.isPresent()) {
                    continue;
                }
                attributes.add(AttributeMap.INSTANCES.poToInfoDto(first.get()));
            }
            vo.attribute = attributes;
            vo.members = columnDataList(rs, metaData, columnCount);
            //释放资源
            release(rs, st, connection);
        }
        catch (SQLException e)
        {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR,e);
        }
        return vo;
    }

    /**
     * 获取行数据
     * @param rs
     * @param metaData
     * @param columnCount
     * @return
     */
    public List<JSONObject> columnDataList(ResultSet rs, ResultSetMetaData metaData, int columnCount){
        try {
            // json数组
            List<JSONObject> list = new ArrayList<>();
            while (rs.next()) {
                JSONObject jsonObj = new JSONObject();
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    //获取sql查询数据集合
                    String value = rs.getString(columnName);
                    jsonObj.put(columnName, value==null?"":value);
                    jsonObj.put("internalId","");
                }
                list.add(jsonObj);
            }
            return list;
        }
        catch (Exception e)
        {
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR,e);
        }
    }

    @Override
    public ResultEnum importDataSubmit(ImportDataSubmitDTO dto) {
        dataSynchronizationUtils.stgDataSynchronize(dto.entityId,dto.key);
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
            String name=entry.getKey().toString();
            if ( name.equals("internalId") || name.equals("ErrorAttribute"))
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
                if (StringUtils.isEmpty(entry.getValue().toString()))
                {
                    columnList.add("null");
                }
                else {
                    columnList.add("'"+entry.getValue().toString()+"'");
                }
            }
        }
        return Joiner.on(",").join(columnList);
    }

    /**
     * 获取Excel表格数据类型
     * @param cell
     * @return
     */
    public String getCellDataType(Cell cell, String dataType,int decimalLength){
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
                //时间格式
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    value = getFormatDate(cell.getDateCellValue(), dataType);
                } else {
                    //数字格式
                    if (DataTypeEnum.NUMERICAL.getName().equals(dataType)) {
                        DecimalFormat df = new DecimalFormat("#");
                        value = df.format(cell.getNumericCellValue());
                    } else if (DataTypeEnum.FLOAT.getName().equals(dataType)) {
                        DecimalFormat df = new DecimalFormat(createDecimalLength(decimalLength));
                        value = df.format(cell.getNumericCellValue());
                    }
                }
                break;
            //空白
            case Cell.CELL_TYPE_BLANK:
                break;
            //布尔值
            case Cell.CELL_TYPE_BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            //错误值=CELL_TYPE_ERROR
            default:
                //return ResultEnum.EXIST_ERROR_DATA;
        }
        return value;
    }

    /**
     * 浮点型转字符串生成格式
     *
     * @param decimalLength
     * @return
     */
    public String createDecimalLength(int decimalLength) {
        String decimalFormat = "0.";
        for (int i = 0; i < decimalLength; i++) {
            decimalFormat += "0";
        }
        return decimalFormat;
    }

    /**
     * 时间格式化
     *
     * @param date
     * @return
     */
    public static String getFormatDate(Date date, String dataType) {
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //日期
        SimpleDateFormat dates = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        if (DataTypeEnum.DATE.getName().equals(dataType))
        {
            return dates.format(date);
        }
        else if (DataTypeEnum.TIMESTAMP.getName().equals(dataType))
        {
            return myFormat.format(date);
        }
        else if (DataTypeEnum.TIME.getName().equals(dataType)){
            return time.format(date);
        }
        else {
            return "";
        }
    }

    /**
     * 添加批次日志
     * @param batchCode
     * @param entityId
     * @param versionId
     * @param totalCount
     * @param errorCount
     */
    public void setStgBatch(String batchCode,int entityId,int versionId,int totalCount,int errorCount,int status){
        StgBatchDTO stgBatchDto=new StgBatchDTO();
        stgBatchDto.batchCode=batchCode;
        stgBatchDto.entityId=entityId;
        stgBatchDto.versionId=versionId;
        //status:0成功,1失败
        stgBatchDto.status=status;
        stgBatchDto.totalCount=totalCount;
        stgBatchDto.errorCount=errorCount;
        stgBatchService.addStgBatch(stgBatchDto);
    }

    /**
     * 获取stg表code数据集合
     * @param tableName
     * @return
     */
    public List<String> getCodeList(String tableName) {
        List<String> codeList=new ArrayList<>();
        try {
            String sql = "select distinct code  from " + tableName;
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
