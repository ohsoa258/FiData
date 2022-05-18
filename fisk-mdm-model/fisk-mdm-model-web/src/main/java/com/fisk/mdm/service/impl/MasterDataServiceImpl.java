package com.fisk.mdm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.constants.MdmConstants;
import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.common.service.mdmBEBuild.BuildFactoryHelper;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.common.service.mdmBEBuild.dto.ImportDataPageDTO;
import com.fisk.common.service.mdmBEBuild.dto.InsertImportDataDTO;
import com.fisk.common.service.mdmBEBuild.dto.MasterDataPageDTO;
import com.fisk.common.service.mdmBEOperate.BuildCodeHelper;
import com.fisk.common.service.mdmBEOperate.IBuildCodeCommand;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.masterdata.*;
import com.fisk.mdm.dto.stgbatch.StgBatchDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.enums.*;
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
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
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
     * 时间格式化
     *
     * @param date
     * @return
     */
    public static String getFormatDate(Date date, String dataType) {
        if (DataTypeEnum.DATE.getName().equals(dataType)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(date);
        } else if (DataTypeEnum.TIMESTAMP.getName().equals(dataType)) {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateTimeFormat.format(date);
        } else if (DataTypeEnum.TIME.getName().equals(dataType)) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            return timeFormat.format(date);
        } else {
            return "";
        }
    }

    @Override
    public List<ModelDropDownVO> getModelEntityVersionStruct() {
        QueryWrapper<ModelPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<ModelPO> modelPoList = modelMapper.selectList(queryWrapper);
        List<ModelDropDownVO> data = ModelMap.INSTANCES.poListToDropDownVoList(modelPoList);
        data.stream().forEach(e -> {
            e.versions = modelVersionServiceImpl.getModelVersionDropDown(e.id);
            e.versions.stream().map(p -> p.displayName = p.name).collect(Collectors.toList());
            e.children = entityServiceImpl.getEntityDropDown(e.id);
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
    public static void release(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            rs = null;
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            stmt = null;
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            conn = null;
        }
    }

    /**
     * 执行查询sql
     *
     * @param sql        sql
     * @param connection 连接
     * @throws SQLException sqlexception异常
     */
    public ResultSet executeSelectSql(String sql, Connection connection) {
        try {
            Statement statement = connection.createStatement();
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            log.info("执行sql: 【" + sql + "】");
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            log.error("executeSelectSql:", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, e);
        }
    }

    /***
     * 下载模板
     * @param entityId
     * @param response
     * @return
     */
    @Override
    public ResultEnum downloadTemplate(int entityId, HttpServletResponse response) {
        EntityPO entityPo = entityMapper.selectById(entityId);
        if (entityPo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        ExportResultVO vo = new ExportResultVO();
        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        //发布状态、字段状态均为成功
        queryWrapper.select("display_name").lambda()
                .eq(AttributePO::getEntityId, entityId)
                .eq(AttributePO::getStatus, AttributeStatusEnum.SUBMITTED.getValue())
                .eq(AttributePO::getSyncStatus, AttributeSyncStatusEnum.SUCCESS.getValue());
        vo.headerList = (List) attributeMapper.selectObjs(queryWrapper);
        vo.headerList.add(1, "新编码");
        vo.fileName = entityPo.getDisplayName();
        return exportExcel(vo, response);
    }

    /**
     * 根据实体id查询主数据
     *
     * @param dto 实体id
     */
    @Override
    public ResultObjectVO getMasterDataPage(MasterDataQueryDTO dto) {
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
            if (attributeColumnVo.getDataType().equals(DataTypeEnum.DOMAIN.getName())) {
                list.add(attributeColumnVo.getName() + "_code");
                list.add(attributeColumnVo.getName() + "_name");
                continue;
            }
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
            String getTotalSql = "select count(*) as totalNum from " + tableName + " view ";
            ResultSet rSet = statement.executeQuery(getTotalSql);
            int rowCount = 0;
            if (rSet.next()) {
                rowCount = rSet.getInt("totalNum");
            }
            rSet.close();
            resultObjectVO.setTotal(rowCount);
            //获取分页sql
            MasterDataPageDTO dataPageDTO = new MasterDataPageDTO();
            dataPageDTO.setColumnNames(businessColumnName + systemColumnName);
            dataPageDTO.setVersionId(dto.getVersionId());
            dataPageDTO.setPageIndex(dto.getPageIndex());
            dataPageDTO.setPageSize(dto.getPageSize());
            dataPageDTO.setTableName(tableName);
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            String sql = sqlBuilder.buildMasterDataPage(dataPageDTO);
            //执行sql，获得结果集
            log.info("执行sql: 【" + sql + "】");
            ResultSet resultSet = statement.executeQuery(sql);
            //判断结果集是否为空
            if (!resultSet.next()) {
                resultObjectVO.setResultData(new ArrayList<>());
                return resultObjectVO;
            }
            //获取结果集的结构信息
            ResultSetMetaData metaData = resultSet.getMetaData();
            //重置结果集游标，遍历结果集，取出数据
            resultSet.beforeFirst();
            while (resultSet.next()) {
                //用map接收对象
                Map<String, Object> map = new HashMap<>();
                //遍历每一行数据，取出每一个字段名与其对应值
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    map.put(metaData.getColumnName(i), resultSet.getString(metaData.getColumnName(i)));
                }
                //将接收到的对象放入主数据集合中
                data.add(map);
            }
            //创建人/更新人id替换为名称
            ReplenishUserInfo.replenishFiDataUserName(data, client, UserFieldEnum.USER_NAME);
            //将主数据集合添加装入结果对象
            resultObjectVO.setResultData(data);
            //释放资源
            release(resultSet, statement, connection);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getMasterDataPage:", e);
            resultObjectVO.setErrorMsg(e.getMessage());
        }
        return resultObjectVO;
    }

    @Override
    public BathUploadMemberListVo importTemplateData(ImportParamDTO dto, MultipartFile file) {
        if (!file.getOriginalFilename().contains(".xlsx")) {
            throw new FkException(ResultEnum.FILE_NAME_ERROR);
        }
        EntityPO po = entityMapper.selectById(dto.getEntityId());
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AttributePO::getEntityId, dto.getEntityId());
        List<AttributePO> list = attributeMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        BathUploadMemberListVo listVo = new BathUploadMemberListVo();
        BathUploadMemberVO result = new BathUploadMemberVO();
        result.versionId = dto.getVersionId();
        result.entityId = dto.getEntityId();
        result.entityName = po.getDisplayName();
        String tableName = po.getTableName().replace("mdm", "stg");
        //获取mdm表code数据列表
        List<String> codeList = getCodeList(tableName);
        String batchNumber = UUID.randomUUID().toString();
        //解析Excel数据集合
        CopyOnWriteArrayList<JSONObject> objectArrayList = new CopyOnWriteArrayList<>();
        //添加条数、修改条数
        AtomicInteger addCount = new AtomicInteger(0);
        AtomicInteger updateCount = new AtomicInteger(0);
        //成功条数、错误条数
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        //code生成规则
        IBuildCodeCommand buildCodeCommand = BuildCodeHelper.getCodeCommand();
        //用户id
        long userId = userHelper.getLoginUserInfo().id;
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
                result.count += rowNum - 1;
                //每个线程执行条数
                final int threadHandleNumber = MdmConstants.THREAD_EXECUTE_NUMBER;
                //线程数
                int truncInt = (int) Math.rint((rowNum / threadHandleNumber));
                int threadCount = rowNum % threadHandleNumber == 0 ? rowNum / threadHandleNumber : truncInt + 1;
                final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
                for (int thread = 0; thread < threadCount; thread++) {
                    int start = thread * threadHandleNumber + 1;
                    int end = (thread + 1) * threadHandleNumber > rowNum ? rowNum : ((thread + 1) * threadHandleNumber) + 1;
                    new Thread(new Runnable() {
                        @SneakyThrows
                        @Override
                        public void run() {
                            try {
                                List<JSONObject> objectList = new ArrayList<>();
                                for (int row = start; row < end; row++) {
                                    JSONObject jsonObj = new JSONObject();
                                    Row nowRow = sheet.getRow(row);
                                    String errorMsg = "";
                                    for (int col = 0; col < columnNum; col++) {
                                        Cell cell = nowRow.getCell(col);
                                        String value = "";
                                        //判断字段类型
                                        if (cell != null) {
                                            ImportDataVerifyDTO cellDataDTO = getCellDataType(cell,
                                                    attributePoList.get(col).getDisplayName(),
                                                    attributePoList.get(col).getDataType());
                                            value = cellDataDTO.getValue();
                                            errorMsg += cellDataDTO.getSuccess() ? "" : cellDataDTO.getErrorMsg();
                                        }
                                        jsonObj.put(attributePoList.get(col).getName(), dto.removeSpace ? value.trim() : value);
                                    }
                                    if (StringUtils.isEmpty(jsonObj.get("code").toString())
                                            && !StringUtils.isEmpty(jsonObj.get("fidata_new_code").toString())) {
                                        errorMsg += "输入新编码时，编码列不能为空";
                                    }
                                    if (StringUtils.isEmpty(jsonObj.get("code").toString())
                                            && StringUtils.isEmpty(jsonObj.get("fidata_new_code").toString())) {
                                        jsonObj.put("code", buildCodeCommand.createCode());
                                    }
                                    //上传逻辑：1 修改 2 新增
                                    if (codeList.contains(jsonObj.get("code"))) {
                                        jsonObj.put("fidata_syncy_type", SyncTypeStatusEnum.UPDATE.getValue());
                                        updateCount.incrementAndGet();
                                    } else {
                                        jsonObj.put("fidata_syncy_type", SyncTypeStatusEnum.INSERT.getValue());
                                        addCount.incrementAndGet();
                                    }
                                    jsonObj.put("fidata_error_msg", errorMsg);
                                    jsonObj.put("internalId", "");
                                    //0：上传成功（数据进入stg表） 1：提交成功（数据进入mdm表） 2：提交失败（数据进入mdm表失败）
                                    if (StringUtils.isEmpty(errorMsg)) {
                                        jsonObj.put("fidata_status", SyncStatusTypeEnum.UPLOADED_SUCCESSFULLY.getValue());
                                        successCount.incrementAndGet();
                                    } else {
                                        jsonObj.put("fidata_status", SyncStatusTypeEnum.UPLOADED_FAILED.getValue());
                                        errorCount.incrementAndGet();
                                    }
                                    objectList.add(jsonObj);
                                }
                                if (!CollectionUtils.isEmpty(objectList)) {
                                    objectArrayList.addAll(objectList);
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
            int flatCount = 0;
            if (!CollectionUtils.isEmpty(objectArrayList)) {
                flatCount = templateDataSubmitStg(objectArrayList, tableName, batchNumber, dto.getVersionId(), userId);
            }
            //添加批次
            setStgBatch(batchNumber, dto.getEntityId(), dto.getVersionId(), result.count, result.count - flatCount, addCount.get(), updateCount.get(), flatCount > 0 ? 0 : 1);
            result.members = objectArrayList;
            result.addCount = addCount.get();
            result.updateCount = updateCount.get();
            List<BathUploadMemberVO> bathUploadMemberVOList = new ArrayList<>();
            bathUploadMemberVOList.add(result);
            listVo.key = batchNumber;
            listVo.list = bathUploadMemberVOList;
            return listVo;
        } catch (Exception e) {
            log.error("importTemplateData", e);
            throw new FkException(ResultEnum.CHECK_TEMPLATE_IMPORT_FAILURE, e);
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
                                     long userId) {
        try {
            Connection conn = getConnection();
            Statement stat = conn.createStatement();
            InsertImportDataDTO dto = new InsertImportDataDTO();
            dto.setBatchCode(batchCode);
            dto.setImportType(ImportTypeEnum.EXCEL_IMPORT.getValue());
            dto.setVersionId(versionId);
            dto.setUserId(userId);
            dto.setMembers(members);
            dto.setTableName(tableName);
            //调用生成批量insert语句方法
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            String sql = sqlBuilder.buildInsertImportData(dto);
            log.info("模板批量添加sql:", sql);
            stat.addBatch(sql);
            int[] flatCount = stat.executeBatch();
            //关闭连接
            AbstractDbHelper.closeStatement(stat);
            AbstractDbHelper.rollbackConnection(conn);
            return flatCount[0];
        } catch (SQLException e) {
            log.error("templateDataSubmitStg:", e);
            throw new FkException(ResultEnum.DATA_SUBMIT_ERROR, e);
        }

    }

    @Override
    public ResultEnum importDataSubmit(ImportDataSubmitDTO dto) {
        try {
            EntityPO entityPO = entityMapper.selectById(dto.entityId);
            if (entityPO == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            Connection connection = getConnection();
            Statement st = connection.createStatement();
            String tableName = entityPO.getTableName().replace("mdm", "stg");
            String sql = "SELECT COUNT(*) AS totalNum FROM " + tableName + " WHERE fidata_batch_code ='"
                    + dto.key + "' and fidata_status=" + SyncStatusTypeEnum.UPLOADED_FAILED.getValue();
            ResultSet rSet = st.executeQuery(sql);
            int totalNum = 0;
            if (rSet.next()) {
                totalNum = rSet.getInt("totalNum");
            }
            if (totalNum > 0) {
                throw new FkException(ResultEnum.EXISTS_INCORRECT_DATA);
            }
            return dataSynchronizationUtils.stgDataSynchronize(dto.entityId, dto.key);
        } catch (SQLException e) {
            log.error("importDataSubmit:", e);
            throw new FkException(ResultEnum.SUBMIT_FAILURE);
        }

    }

    @Override
    public BathUploadMemberVO importDataQuery(ImportDataQueryDTO dto) {
        EntityPO entityPO = entityMapper.selectById(dto.getEntityId());
        if (entityPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        BathUploadMemberVO vo = new BathUploadMemberVO();
        vo.entityName = entityPO.getDisplayName();
        vo.entityId = dto.getEntityId();
        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AttributePO::getEntityId, dto.getEntityId());
        List<AttributePO> list = attributeMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        try {
            Connection connection = getConnection();
            Statement st = connection.createStatement();
            String tableName = entityPO.getTableName().replace("mdm", "stg");
            //获取总条数、新增条数、编辑条数、成功条数、失败条数
            StringBuilder getTotalSql = new StringBuilder();
            getTotalSql.append("select count(*) as totalNum");
            getTotalSql.append(",sum( case fidata_status when " + SyncStatusTypeEnum.SUBMITTED_SUCCESSFULLY.getValue() + " then 1 else 0 end) as submitSuccessCount");
            getTotalSql.append(",sum( case fidata_status when " + SyncStatusTypeEnum.SUBMISSION_FAILED.getValue() + " then 1 else 0 end) as submitErrorCount");
            getTotalSql.append(",sum( case fidata_status when " + SyncStatusTypeEnum.UPLOADED_SUCCESSFULLY.getValue() + " then 1 else 0 end) as successCount");
            getTotalSql.append(",sum( case fidata_status when " + SyncStatusTypeEnum.UPLOADED_FAILED.getValue() + " then 1 else 0 end) as errorCount");
            getTotalSql.append(",sum( case fidata_syncy_type when " + SyncTypeStatusEnum.UPDATE.getValue() + " then 1 else 0 end) as updateCount");
            getTotalSql.append(",sum( case fidata_syncy_type when " + SyncTypeStatusEnum.INSERT.getValue() + " then 1 else 0 end) as addCount");
            getTotalSql.append(" from " + tableName + " where fidata_batch_code='" + dto.getKey() + "'");
            if (!CollectionUtils.isEmpty(dto.getStatus())) {
                getTotalSql.append(" and fidata_status in(" + Joiner.on(",").join(dto.getStatus()) + ")");
            }
            if (!CollectionUtils.isEmpty(dto.getSyncType())) {
                getTotalSql.append(" and fidata_syncy_type in(" + Joiner.on(",").join(dto.getSyncType()) + ")");
            }
            ResultSet rSet = st.executeQuery(getTotalSql.toString());
            if (rSet.next()) {
                vo.count = rSet.getInt("totalNum");
                vo.updateCount = rSet.getInt("updateCount");
                vo.addCount = rSet.getInt("addCount");
                vo.successCount = rSet.getInt("successCount");
                vo.errorCount = rSet.getInt("errorCount");
                vo.submitSuccessCount = rSet.getInt("submitSuccessCount");
                vo.submitErrorCount = rSet.getInt("submitErrorCount");
            }
            rSet.close();
            ImportDataPageDTO pageDataDTO = new ImportDataPageDTO();
            pageDataDTO.setPageIndex(dto.getPageIndex());
            pageDataDTO.setPageSize(dto.getPageSize());
            pageDataDTO.setBatchCode(dto.getKey());
            pageDataDTO.setStatus(dto.getStatus());
            pageDataDTO.setSyncType(dto.getSyncType());
            pageDataDTO.setTableName(entityPO.getTableName().replace("mdm", "stg"));
            //调用生成分页语句方法
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            String sql = sqlBuilder.buildImportDataPage(pageDataDTO);
            ResultSet rs = st.executeQuery(sql);
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
            AttributeInfoDTO infoDTO = new AttributeInfoDTO();
            infoDTO.setName("fidata_new_code");
            infoDTO.setDisplayName("新编码");
            vo.attribute.add(1, infoDTO);
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

    @Override
    public ResultEnum updateImportData(UpdateImportDataDTO dto) {
        try {
            EntityPO entityPO = entityMapper.selectById(dto.getEntityId());
            if (entityPO == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            Connection conn = getConnection();
            Statement stat = conn.createStatement();
            //验证code
            ImportDataVerifyDTO verifyDTO = verifyCode(dto.getData());
            dto.getData().put("fidata_error_msg", verifyDTO.getErrorMsg());
            if (verifyDTO.getSuccess()) {
                dto.getData().put("fidata_status", SyncStatusTypeEnum.UPLOADED_SUCCESSFULLY.getValue());
            }
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            //生成update语句
            String updateSql = sqlBuilder.buildUpdateImportData(dto.getData(),
                    entityPO.getTableName().replace("mdm", "stg"),
                    ImportTypeEnum.EXCEL_IMPORT.getValue());
            if (StringUtils.isEmpty(updateSql)) {
                return ResultEnum.PARAMTER_ERROR;
            }
            int flat = stat.executeUpdate(updateSql);
            //关闭连接
            AbstractDbHelper.closeStatement(stat);
            AbstractDbHelper.rollbackConnection(conn);
            return flat > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        } catch (SQLException e) {
            log.error("updateImportData:", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
    }

    /**
     * 验证code
     *
     * @param data
     * @return
     */
    public ImportDataVerifyDTO verifyCode(Map<String, Object> data) {
        try {
            ImportDataVerifyDTO dto = new ImportDataVerifyDTO();
            dto.setSuccess(true);
            if (StringUtils.isEmpty(data.get("code").toString())
                    && !StringUtils.isEmpty(data.get("fidata_new_code").toString())) {
                dto.setSuccess(false);
                dto.setErrorMsg("输入新编码时，编码列不能为空");
            }
            return dto;
        } catch (Exception e) {
            log.error("verifyCode:", e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
    }

    /**
     * 导出Excel
     *
     * @param vo
     * @param response
     * @return
     */
    public ResultEnum exportExcel(ExportResultVO vo, HttpServletResponse response) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet1");
        XSSFRow row1 = sheet.createRow(0);
        if (CollectionUtils.isEmpty(vo.headerList)) {
            ResultEntityBuild.build(ResultEnum.VISUAL_QUERY_ERROR);
        }
        for (int i = 0; i < vo.headerList.size(); i++) {
            row1.createCell(i).setCellValue(vo.headerList.get(i));
        }
        if (!CollectionUtils.isEmpty(vo.dataArray)) {
            for (int i = 0; i < vo.dataArray.size(); i++) {
                XSSFRow row = sheet.createRow(i + 1);
                JSONObject jsonObject = JSONObject.parseObject(vo.dataArray.get(i).toString());
                for (int j = 0; j < vo.headerList.size(); j++) {
                    row.createCell(j).setCellValue(jsonObject.get(vo.headerList.get(j)).toString());
                }
            }
        }
        //将文件存到指定位置
        try {
            //输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.addHeader("Content-Disposition", "attachment;filename=fileName" + ".xlsx");
            workbook.write(output);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new FkException(ResultEnum.SQL_ANALYSIS, e);
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 获取行数据
     *
     * @param rs
     * @param metaData
     * @param columnCount
     * @return
     */
    public List<JSONObject> columnDataList(ResultSet rs, ResultSetMetaData metaData, int columnCount) {
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
                    jsonObj.put(columnName, value == null ? "" : value);
                }
                list.add(jsonObj);
            }
            return list;
        } catch (Exception e) {
            throw new FkException(ResultEnum.DATAACCESS_GETTABLE_ERROR, e);
        }
    }


    /**
     * 获取Excel表格数据类型并校验
     *
     * @param cell
     * @return
     */
    public ImportDataVerifyDTO getCellDataType(Cell cell, String columnDisplay, String dataType) {
        ImportDataVerifyDTO dto = new ImportDataVerifyDTO();
        dto.setSuccess(true);
        dto.setValue("");
        switch (cell.getCellType()) {
            //字符串
            case Cell.CELL_TYPE_STRING:
                dto.setValue(cell.getStringCellValue());
                break;
            //公式
            case Cell.CELL_TYPE_FORMULA:
                dto.setSuccess(false);
                dto.setValue(columnDisplay + "列存在公式,解析错误");
                break;
            //数字
            case Cell.CELL_TYPE_NUMERIC:
                //时间格式
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    String value = getFormatDate(cell.getDateCellValue(), dataType);
                    if (StringUtils.isEmpty(value)) {
                        dto.setSuccess(false);
                        dto.setErrorMsg(columnDisplay + "列存在错误时间格式");
                        break;
                    }
                    dto.setValue(value);
                } else {
                    //数字格式
                    if (DataTypeEnum.FLOAT.getName().equals(dataType)) {
                        dto.setValue(String.valueOf(cell.getNumericCellValue()));
                    } else {
                        DecimalFormat df = new DecimalFormat("#");
                        dto.setValue(df.format(cell.getNumericCellValue()));
                    }
                }
                break;
            //空白
            case Cell.CELL_TYPE_BLANK:
                dto.setValue("");
                break;
            //布尔值
            case Cell.CELL_TYPE_BOOLEAN:
                dto.setValue(String.valueOf(cell.getBooleanCellValue()));
                break;
            //错误值=CELL_TYPE_ERROR
            default:
                dto.setSuccess(false);
                dto.setErrorMsg(columnDisplay + "列存在不能解析的数据");
                break;

        }
        return dto;
    }

    /**
     * 添加批次日志
     *
     * @param batchCode
     * @param entityId
     * @param versionId
     * @param totalCount
     * @param errorCount
     * @param addCount
     * @param updateCount
     */
    public void setStgBatch(String batchCode,
                            int entityId,
                            int versionId,
                            int totalCount,
                            int errorCount,
                            int addCount,
                            int updateCount,
                            int status) {
        StgBatchDTO stgBatchDto = new StgBatchDTO();
        stgBatchDto.setBatchCode(batchCode);
        stgBatchDto.setEntityId(entityId);
        stgBatchDto.setVersionId(versionId);
        //status:0成功,1失败
        stgBatchDto.setStatus(status);
        stgBatchDto.setTotalCount(totalCount);
        stgBatchDto.setErrorCount(errorCount);
        stgBatchDto.setAddCount(addCount);
        stgBatchDto.setUpdateCount(updateCount);
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
