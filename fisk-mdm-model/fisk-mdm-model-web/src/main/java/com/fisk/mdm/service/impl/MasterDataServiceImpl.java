package com.fisk.mdm.service.impl;

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
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDTO;
import com.fisk.mdm.dto.masterdata.*;
import com.fisk.mdm.dto.stgbatch.StgBatchDTO;
import com.fisk.mdm.dto.viwGroup.ViwGroupDetailsDTO;
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
import com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils;
import com.fisk.mdm.utlis.DataSynchronizationUtils;
import com.fisk.mdm.utlis.MasterDataFormatVerifyUtils;
import com.fisk.mdm.vo.attribute.AttributeColumnVO;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.mdm.vo.masterdata.BathUploadMemberListVo;
import com.fisk.mdm.vo.masterdata.BathUploadMemberVO;
import com.fisk.mdm.vo.masterdata.ExportResultVO;
import com.fisk.mdm.vo.model.ModelDropDownVO;
import com.fisk.mdm.vo.resultObject.ResultAttributeGroupVO;
import com.fisk.mdm.vo.resultObject.ResultObjectVO;
import com.fisk.mdm.vo.viwGroup.ViwGroupVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.relenish.ReplenishUserInfo;
import com.fisk.system.relenish.UserFieldEnum;
import com.google.common.base.Joiner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
    AttributeServiceImpl attributeService;
    @Resource
    AttributeGroupServiceImpl attributeGroupService;
    @Resource
    ViwGroupServiceImpl viwGroupService;

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

    @Override
    public List<ModelDropDownVO> getModelEntityVersionStruct() {
        QueryWrapper<ModelPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<ModelPO> modelPoList = modelMapper.selectList(queryWrapper);
        List<ModelDropDownVO> data = ModelMap.INSTANCES.poListToDropDownVoList(modelPoList);
        data.stream().forEach(e -> {
            e.setVersions(modelVersionServiceImpl.getModelVersionDropDown(e.id));
            e.getVersions().stream().map(p -> p.displayName = p.name).collect(Collectors.toList());
            e.setEntity(entityServiceImpl.getEntityDropDown(e.id));
            e.getEntity().forEach(p -> p.setViewGroups(viwGroupService.getViewGroupByEntityId(p.id)));
            e.getEntity().forEach(p -> p.getViewGroups().forEach(t -> t.displayName = t.name));
            e.setAttributeGroups(attributeGroupService.getAttributeGroupByModelId(e.id));
            e.getAttributeGroups().stream().map(p -> p.displayName = p.name).collect(Collectors.toList());
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
        //获取已发布的实体属性
        List<AttributeInfoDTO> attributeList = attributeService.listPublishedAttribute(entityId);
        List<String> columnList = attributeList.stream().map(e -> e.getDisplayName()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(columnList)) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        columnList.add(1, "新编码");
        vo.setHeaderList(columnList);
        vo.setFileName(entityPo.getDisplayName());
        return exportExcel(vo, response);
    }

    /**
     * 根据实体id查询主数据
     *
     * @param dto 实体id
     */
    @Override
    public ResultObjectVO getMasterDataPage(MasterDataQueryDTO dto, HttpServletResponse response) {
        //准备返回对象
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        EntityVO entityVo = entityService.getDataById(dto.getEntityId());
        if (entityVo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //获得主数据表名
        String tableName = TableNameGenerateUtils.generateViwTableName(entityVo.getModelId(), dto.getEntityId());
        //查询该实体下发布的属性
        List<AttributeInfoDTO> attributeInfos = attributeService.listPublishedAttribute(dto.getEntityId());
        if (attributeInfos.isEmpty()) {
            throw new FkException(ResultEnum.ATTRIBUTE_NOT_EXIST);
        }
        //将查询到的属性集合添加装入结果对象
        List<AttributeColumnVO> attributeColumnVoList = AttributeMap.INSTANCES.dtoListToVoList(attributeInfos);
        //数据类型英文名称赋值
        attributeColumnVoList
                .stream()
                .map(e -> e.dataTypeEnDisplay = DataTypeEnum.getValue(e.getDataType()).name())
                .collect(Collectors.toList());
        //获得业务字段名
        List<AttributeColumnVO> newColumnList = new ArrayList<>();
        for (AttributeColumnVO attributeColumnVo : attributeColumnVoList) {
            //域字段添加编码和名称表头
            if (attributeColumnVo.getDataType().equals(DataTypeEnum.DOMAIN.getName())) {
                newColumnList.add(getCodeAndName(attributeColumnVo));
                attributeColumnVo.setDisplayName(TableNameGenerateUtils.generateDomainCodeDisplayName(attributeColumnVo.getDisplayName()));
                attributeColumnVo.setName(TableNameGenerateUtils.generateDomainCode(attributeColumnVo.getName()));
            }
            newColumnList.add(attributeColumnVo);
        }
        attributeColumnVoList = newColumnList;
        List<ResultAttributeGroupVO> attributeGroupVoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dto.getAttributeGroups())) {
            List<Integer> attributeIds = new ArrayList<>();
            attributeGroupVoList = attributeGroupAttribute(dto.getAttributeGroups(), dto.getEntityId(), attributeColumnVoList);
            //获取所有实体属性id集合
            attributeGroupVoList.stream().forEach(e ->
                    attributeIds.addAll(e.getAttributes()
                            .stream().map(p -> p.getId()).collect(Collectors.toList())));
            if (!CollectionUtils.isEmpty(attributeIds)) {
                attributeColumnVoList = attributeColumnVoList.stream()
                        .filter(e -> attributeIds.contains(e.getId()))
                        .collect(Collectors.toList());
            }
        } else {
            ResultAttributeGroupVO attributeGroupVo = new ResultAttributeGroupVO();
            attributeGroupVo.setName("属性组");
            attributeGroupVo.setAttributes(attributeColumnVoList);
            attributeGroupVoList.add(attributeGroupVo);
        }
        resultObjectVO.setAttributes(attributeGroupVoList);
        String businessColumnName = StringUtils.join(attributeColumnVoList.stream()
                .map(e -> e.getName()).collect(Collectors.toList()), ",");
        //准备主数据集合
        List<Map<String, Object>> data;
        try {
            //获取总条数
            int rowCount = 0;
            IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
            //查询提交数据是否存在错误数据
            String count = buildSqlCommand.buildQueryCount(tableName, null);
            List<Map<String, Object>> columnCount = AbstractDbHelper.execQueryResultMaps(count, getConnection());
            if (!CollectionUtils.isEmpty(columnCount)) {
                rowCount = Integer.valueOf(columnCount.get(0).get("totalnum").toString()).intValue();
            }
            resultObjectVO.setTotal(rowCount);
            //获取分页sql
            MasterDataPageDTO dataPageDTO = new MasterDataPageDTO();
            dataPageDTO.setColumnNames(businessColumnName + systemColumnName);
            dataPageDTO.setVersionId(dto.getVersionId());
            dataPageDTO.setPageIndex(dto.getPageIndex());
            dataPageDTO.setPageSize(dto.getPageSize());
            dataPageDTO.setTableName(tableName);
            dataPageDTO.setExport(dto.getExport());
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            String sql = sqlBuilder.buildMasterDataPage(dataPageDTO);
            //执行sql，获得结果集
            log.info("执行sql: 【" + sql + "】");
            data = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
            //判断结果集是否为空
            if (CollectionUtils.isEmpty(data)) {
                resultObjectVO.setResultData(new ArrayList<>());
                return resultObjectVO;
            }
            //创建人/更新人id替换为名称
            ReplenishUserInfo.replenishFiDataUserName(data, client, UserFieldEnum.USER_NAME);
            //是否导出
            if (dto.getExport()) {
                ExportResultVO vo = new ExportResultVO();
                List<String> nameList = attributeColumnVoList.stream().map(e -> e.getName()).collect(Collectors.toList());
                List<String> nameDisplayList = attributeColumnVoList.stream().map(e -> e.getDisplayName()).collect(Collectors.toList());
                vo.setHeaderList(nameList);
                vo.setDataArray(data);
                vo.setHeaderDisplayList(nameDisplayList);
                vo.setFileName(entityVo.getTableName());
                exportExcel(vo, response);
                return resultObjectVO;
            }
            //将主数据集合添加装入结果对象
            resultObjectVO.setResultData(data);
        } catch (Exception e) {
            log.error("getMasterDataPage:", e);
            resultObjectVO.setErrorMsg(e.getMessage());
        }
        return resultObjectVO;
    }

    public void vie(MasterDataQueryDTO dto) {
        //准备返回对象
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        ViwGroupVO viwGroupVO = viwGroupService.getDataByGroupId(dto.getViewId());
        if (viwGroupVO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        List<Integer> attributeIds = viwGroupVO.getGroupDetailsList().stream()
                .map(e -> e.getAttributeId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attributeIds)) {
        }
        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", attributeIds);
        List<AttributePO> poList = attributeMapper.selectList(queryWrapper);
        List<AttributeInfoDTO> attributeInfos = AttributeMap.INSTANCES.poToDtoList(poList);
        for (AttributeInfoDTO item : attributeInfos) {
            Optional<ViwGroupDetailsDTO> first = viwGroupVO.getGroupDetailsList()
                    .stream()
                    .filter(e -> e.getAttributeId().equals(item.getId()))
                    .findFirst();
            if (!first.isPresent() && StringUtils.isEmpty(first.get().getAliasName())) {
                continue;
            }
            item.setName(first.get().getAliasName());
        }


    }

    /**
     * 根据属性组下所有实体属性
     *
     * @param attributeGroups       属性组集合
     * @param entityId              实体id
     * @param attributeColumnVoList 实体下属性集合
     * @return
     */
    public List<ResultAttributeGroupVO> attributeGroupAttribute(List<Integer> attributeGroups,
                                                                Integer entityId,
                                                                List<AttributeColumnVO> attributeColumnVoList) {
        List<ResultAttributeGroupVO> attributeGroupVoList = new ArrayList<>();
        for (Integer id : attributeGroups) {
            List<Integer> attributes = attributeGroupService.getAttributeGroupAttribute(id, entityId);
            //获取属性组名称
            AttributeGroupDTO attributeGroup = attributeGroupService.getAttributeGroup(id);
            if (CollectionUtils.isEmpty(attributes) || attributeGroup == null) {
                continue;
            }
            ResultAttributeGroupVO attributeGroupVo = new ResultAttributeGroupVO();
            attributeGroupVo.setName(attributeGroup.getName());
            List<AttributeColumnVO> collect = attributeColumnVoList.stream()
                    .filter(e -> attributes.contains(e.getId())).collect(Collectors.toList());
            attributeGroupVo.setAttributes(collect);
            attributeGroupVoList.add(attributeGroupVo);
        }
        return attributeGroupVoList;
    }

    /**
     * 主数据维护列表，当为域字段时，需要展示域字段编码和名称
     *
     * @param attributeColumnVo
     * @return
     */
    public AttributeColumnVO getCodeAndName(AttributeColumnVO attributeColumnVo) {
        AttributeColumnVO vo = new AttributeColumnVO();
        vo.setDisplayName(TableNameGenerateUtils.generateDomainNameDisplayName(attributeColumnVo.getDisplayName()));
        vo.setName(TableNameGenerateUtils.generateDomainName(attributeColumnVo.getName()));
        vo.setDataType(attributeColumnVo.getDataType());
        vo.setDataTypeEnDisplay(attributeColumnVo.getDataTypeEnDisplay());
        vo.setEnableRequired(attributeColumnVo.getEnableRequired());
        vo.setSortWieght(attributeColumnVo.getSortWieght());
        vo.setDisplayWidth(attributeColumnVo.getDisplayWidth());
        return vo;
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
    public int templateDataSubmitStg(List<Map<String, Object>> members, String tableName, String batchCode, int versionId, long userId) {
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

        EntityPO entityPO = entityMapper.selectById(dto.entityId);
        if (entityPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        String tableName = TableNameGenerateUtils.generateStgTableName(entityPO.getModelId(), dto.getEntityId());
        //where条件
        String queryConditions = " and fidata_batch_code ='" + dto.key + "' and fidata_status=" + SyncStatusTypeEnum.UPLOADED_FAILED.getValue();
        IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
        //查询提交数据是否存在错误数据
        String sql = buildSqlCommand.buildQueryCount(tableName, queryConditions);
        List<Map<String, Object>> maps = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
        if (!CollectionUtils.isEmpty(maps) && Integer.valueOf(maps.get(0).get("totalnum").toString()).intValue() > 0) {
            throw new FkException(ResultEnum.EXISTS_INCORRECT_DATA);
        }
        return dataSynchronizationUtils.stgDataSynchronize(dto.entityId, dto.key);
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
        List<AttributeInfoDTO> attributeInfos = attributeService.listPublishedAttribute(dto.getEntityId());
        if (CollectionUtils.isEmpty(attributeInfos)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        try {
            String tableName = TableNameGenerateUtils.generateStgTableName(entityPO.getModelId(), dto.getEntityId());
            //获取总条数、新增条数、编辑条数、成功条数、失败条数
            StringBuilder conditions = new StringBuilder();
            conditions.append(" and fidata_batch_code='" + dto.getKey() + "'");
            if (!CollectionUtils.isEmpty(dto.getStatus())) {
                conditions.append(" and fidata_status in(" + Joiner.on(",").join(dto.getStatus()) + ")");
            }
            if (!CollectionUtils.isEmpty(dto.getSyncType())) {
                conditions.append(" and fidata_syncy_type in(" + Joiner.on(",").join(dto.getSyncType()) + ")");
            }
            IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
            //生成查询sql
            String countSql = buildSqlCommand.buildExportDataCount(tableName, conditions.toString());
            List<Map<String, Object>> resultMaps = AbstractDbHelper.execQueryResultMaps(countSql, getConnection());
            if (!CollectionUtils.isEmpty(resultMaps)) {
                vo.count = Integer.valueOf(resultMaps.get(0).get("totalnum").toString()).intValue();
                vo.updateCount = Integer.valueOf(resultMaps.get(0).get("updatecount").toString()).intValue();
                vo.addCount = Integer.valueOf(resultMaps.get(0).get("addcount").toString()).intValue();
                vo.successCount = Integer.valueOf(resultMaps.get(0).get("successcount").toString()).intValue();
                vo.errorCount = Integer.valueOf(resultMaps.get(0).get("errorcount").toString()).intValue();
                vo.submitSuccessCount = Integer.valueOf(resultMaps.get(0).get("submitsuccesscount").toString()).intValue();
                vo.submitErrorCount = Integer.valueOf(resultMaps.get(0).get("submiterrorcount").toString()).intValue();
            }
            ImportDataPageDTO pageDataDTO = new ImportDataPageDTO();
            pageDataDTO.setPageIndex(dto.getPageIndex());
            pageDataDTO.setPageSize(dto.getPageSize());
            pageDataDTO.setBatchCode(dto.getKey());
            pageDataDTO.setStatus(dto.getStatus());
            pageDataDTO.setSyncType(dto.getSyncType());
            pageDataDTO.setTableName(TableNameGenerateUtils.generateStgTableName(entityPO.getModelId(), dto.getEntityId()));
            //调用生成分页语句方法
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            String sql = sqlBuilder.buildImportDataPage(pageDataDTO);
            List<Map<String, Object>> resultPageMaps = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
            vo.attribute = attributeInfos;
            AttributeInfoDTO infoDTO = new AttributeInfoDTO();
            infoDTO.setName("fidata_new_code");
            infoDTO.setDisplayName("新编码");
            vo.attribute.add(1, infoDTO);
            vo.members = resultPageMaps;
        } catch (Exception e) {
            log.error("importDataQuery:", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, e);
        }
        return vo;
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
        //获取已发布的属性
        List<AttributeInfoDTO> list = attributeService.listPublishedAttribute(dto.getEntityId());
        if (CollectionUtils.isEmpty(list)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        BathUploadMemberListVo listVo = new BathUploadMemberListVo();
        BathUploadMemberVO result = new BathUploadMemberVO();
        result.versionId = dto.getVersionId();
        result.entityId = dto.getEntityId();
        result.entityName = po.getDisplayName();
        String tableName = TableNameGenerateUtils.generateStgTableName(dto.getModelId(), dto.getEntityId());
        //获取mdm表code数据列表
        Optional<AttributeInfoDTO> codeColumn = list.stream().filter(e -> e.getName().equals(MdmTypeEnum.CODE.getName())).findFirst();
        if (!codeColumn.isPresent()) {
            throw new FkException(ResultEnum.CODE_NOT_EXIST);
        }
        List<String> codeList = getCodeList(TableNameGenerateUtils.generateMdmTableName(dto.getModelId(), dto.getEntityId()), codeColumn.get().getColumnName());
        String batchNumber = UUID.randomUUID().toString();
        //解析Excel数据集合
        CopyOnWriteArrayList<Map<String, Object>> objectArrayList = new CopyOnWriteArrayList<>();
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
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getRow(0) == null) {
                throw new FkException(ResultEnum.EMPTY_FORM);
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
                    Optional<AttributeInfoDTO> data = list.stream().filter(e -> cell.getStringCellValue().equals(e.getDisplayName())).findFirst();
                    if (!data.isPresent()) {
                        throw new FkException(ResultEnum.EXIST_INVALID_COLUMN);
                    }
                    attributePoList.add(data.get());
                }
            if (list.size() != columnNum - 1) {
                throw new FkException(ResultEnum.CHECK_TEMPLATE_IMPORT_FAILURE);
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
                                List<Map<String, Object>> objectList = new ArrayList<>();
                                for (int row = start; row < end; row++) {
                                    Map<String, Object> jsonObj = new HashMap<>();
                                    Row nowRow = sheet.getRow(row);
                                    String errorMsg = "";
                                    for (int col = 0; col < columnNum; col++) {
                                        Cell cell = nowRow.getCell(col);
                                        String value = "";
                                        //判断字段类型
                                        if (cell != null) {
                                            ImportDataVerifyDTO cellDataDTO = MasterDataFormatVerifyUtils.getCellDataType(cell,
                                                    attributePoList.get(col).getDisplayName(),
                                                    attributePoList.get(col).getDataType());
                                            value = cellDataDTO.getValue();
                                            errorMsg += cellDataDTO.getSuccess() ? "" : cellDataDTO.getErrorMsg();
                                        }
                                        jsonObj.put(attributePoList.get(col).getName(), dto.removeSpace ? value.trim() : value);
                                    }
                                    //验证code
                                    ImportDataVerifyDTO verifyDTO = MasterDataFormatVerifyUtils.verifyCode(jsonObj);
                                    errorMsg += verifyDTO.getErrorMsg();
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
            int flatCount = 0;
            if (!CollectionUtils.isEmpty(objectArrayList)) {
                flatCount = templateDataSubmitStg(objectArrayList, tableName, batchNumber, dto.getVersionId(), userId);
            }
            //添加批次
            setStgBatch(batchNumber, dto.getEntityId(), dto.getVersionId(), result.count, result.count - flatCount, addCount.get(), updateCount.get(), flatCount > 0 ? 0 : 1);
            //校验重复code
            verifyRepeatCode(tableName, batchNumber);
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

    @Override
    public ResultEnum updateImportData(UpdateImportDataDTO dto) {
        try {
            EntityPO entityPO = entityMapper.selectById(dto.getEntityId());
            if (entityPO == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            String tableName = TableNameGenerateUtils.generateStgTableName(entityPO.getModelId(), dto.getEntityId());
            //验证code
            ImportDataVerifyDTO verifyDTO = MasterDataFormatVerifyUtils.verifyCode(dto.getData());
            dto.getData().put("fidata_status", SyncStatusTypeEnum.UPLOADED_FAILED.getValue());
            dto.getData().put("fidata_error_msg", verifyDTO.getErrorMsg());
            if (verifyDTO.getSuccess()) {
                dto.getData().put("fidata_status", SyncStatusTypeEnum.UPLOADED_SUCCESSFULLY.getValue());
                if (StringUtils.isEmpty(dto.getData().get("code").toString())) {
                    //code生成规则
                    IBuildCodeCommand buildCodeCommand = BuildCodeHelper.getCodeCommand();
                    dto.getData().put("code", buildCodeCommand.createCode());
                }
            }
            QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(AttributePO::getEntityId, dto.getEntityId())
                    .eq(AttributePO::getName, "code");
            AttributePO attributePO = attributeMapper.selectOne(queryWrapper);
            if (attributePO == null) {
                throw new FkException(ResultEnum.CODE_NOT_EXIST);
            }
            //获取mdm表code数据列表
            List<String> codeList = getCodeList(TableNameGenerateUtils.generateMdmTableName(entityPO.getModelId(), dto.getEntityId()), attributePO.getColumnName());
            //判断上传逻辑
            dto.getData().put("fidata_syncy_type", SyncTypeStatusEnum.INSERT.getValue());
            if (codeList.contains(dto.getData().get("code"))) {
                dto.getData().put("fidata_syncy_type", SyncTypeStatusEnum.UPDATE.getValue());
            }
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            //生成update语句
            String updateSql = sqlBuilder.buildUpdateImportData(dto.getData(), tableName);
            if (StringUtils.isEmpty(updateSql)) {
                return ResultEnum.PARAMTER_ERROR;
            }
            Connection conn = getConnection();
            Statement stat = conn.createStatement();
            int flat = stat.executeUpdate(updateSql);
            //验证code是否重复
            verifyRepeatCode(tableName, dto.getData().get("fidata_batch_code").toString());
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
        if (CollectionUtils.isEmpty(vo.getHeaderList())) {
            ResultEntityBuild.build(ResultEnum.VISUAL_QUERY_ERROR);
        }
        if (!CollectionUtils.isEmpty(vo.getHeaderDisplayList())) {
            for (int i = 0; i < vo.getHeaderDisplayList().size(); i++) {
                row1.createCell(i).setCellValue(vo.getHeaderDisplayList().get(i));
            }
        } else {
            for (int i = 0; i < vo.getHeaderList().size(); i++) {
                row1.createCell(i).setCellValue(vo.getHeaderList().get(i));
            }
        }
        if (!CollectionUtils.isEmpty(vo.getDataArray())) {
            for (int i = 0; i < vo.getDataArray().size(); i++) {
                XSSFRow row = sheet.createRow(i + 1);
                Map<String, Object> jsonObject = vo.getDataArray().get(i);
                for (int j = 0; j < vo.getHeaderList().size(); j++) {
                    row.createCell(j).setCellValue(jsonObject.get(vo.getHeaderList().get(j)) == null ? "" : jsonObject.get(vo.getHeaderList().get(j)).toString());
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
     * 验证code是否重复
     *
     * @param tableName
     * @param batchCode
     */
    public void verifyRepeatCode(String tableName, String batchCode) {
        try {
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            String sql = sqlBuilder.buildVerifyRepeatCode(tableName, batchCode);
            Connection conn = getConnection();
            Statement stat = conn.createStatement();
            stat.executeUpdate(sql);
            //关闭连接
            AbstractDbHelper.closeStatement(stat);
            AbstractDbHelper.rollbackConnection(conn);
        } catch (SQLException e) {
            log.error("verifyRepeatCode:", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
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
     *
     * @param tableName
     * @return
     */
    public List<String> getCodeList(String tableName, String codeColumnName) {
        List<String> codeList = new ArrayList<>();
        try {
            IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
            //查询code列sql
            String sql = buildSqlCommand.buildQueryOneColumn(tableName, codeColumnName);
            List<Map<String, Object>> maps = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
            codeList.addAll(maps.stream().map(e -> e.get("columnName").toString()).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("getCodeList:", e);
        }
        return codeList;
    }

}
