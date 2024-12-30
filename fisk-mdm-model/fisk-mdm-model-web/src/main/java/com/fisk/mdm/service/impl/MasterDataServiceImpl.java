package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.fisk.common.service.mdmBEBuild.CommonMethods;
import com.fisk.common.service.mdmBEBuild.IBuildSqlCommand;
import com.fisk.common.service.mdmBEBuild.dto.ImportDataPageDTO;
import com.fisk.common.service.mdmBEBuild.dto.InsertImportDataDTO;
import com.fisk.common.service.mdmBEBuild.dto.MasterDataPageDTO;
import com.fisk.common.service.mdmBEOperate.BuildCodeHelper;
import com.fisk.common.service.mdmBEOperate.IBuildCodeCommand;
import com.fisk.common.service.mdmBEOperate.dto.CodeRuleDTO;
import com.fisk.common.service.pageFilter.dto.FilterQueryDTO;
import com.fisk.common.service.pageFilter.dto.OperatorVO;
import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import com.fisk.mdm.dto.attributeGroup.AttributeGroupDTO;
import com.fisk.mdm.dto.file.FileDTO;
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
import com.fisk.mdm.service.CodeRuleService;
import com.fisk.mdm.service.EntityService;
import com.fisk.mdm.service.IMasterDataService;
import com.fisk.mdm.service.ProcessService;
import com.fisk.mdm.utils.mdmBEBuild.TableNameGenerateUtils;
import com.fisk.mdm.utlis.DataSynchronizationUtils;
import com.fisk.mdm.utlis.MasterDataFormatVerifyUtils;
import com.fisk.mdm.vo.attribute.AttributeColumnVO;
import com.fisk.mdm.vo.attributeGroup.AttributeGroupDropDownVO;
import com.fisk.mdm.vo.codeRule.CodeRuleVO;
import com.fisk.mdm.vo.entity.EntityVO;
import com.fisk.mdm.vo.masterdata.BathUploadMemberListVO;
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
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.fisk.common.service.mdmBEBuild.AbstractDbHelper.rollbackConnection;

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
    CodeRuleService  codeRuleService;

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
    @Resource
    ProcessService processService;

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
            "fidata_update_user," +
            "fidata_lock_tag";

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
//            e.getEntity().forEach(p -> p.getViewGroups().forEach(t -> t.displayName = t.name));
        });
        return data;
    }

    @Override
    public List<AttributeGroupDropDownVO> listAttributeGroup(Integer modelId, Integer entity) {
        return attributeGroupService.getAttributeGroupByModelId(modelId, entity);
    }

    /**
     * 连接Connection
     *
     * @return
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
        //过滤复杂数据类型
        List<AttributeInfoDTO> collect = attributeList.stream()
                .filter(e -> !e.getDataType().equals(DataTypeEnum.LATITUDE_COORDINATE.getName()))
                .filter(e -> !e.getDataType().equals(DataTypeEnum.FILE.getName()))
                .collect(Collectors.toList());
        List<String> columnList = collect.stream().map(e -> e.getDisplayName()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(columnList)) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        columnList.add(1, "新编码");
        vo.setHeaderDisplayList(columnList);
        vo.setFileName(entityPo.getDisplayName());
        return exportExcel(vo, response);
    }

    /**
     * 属性组、自定义视图查看主数据数据
     * @param dto
     * @param response
     * @return
     */
    @Override
    public ResultObjectVO getMasterDataPage(MasterDataQueryDTO dto, HttpServletResponse response) {
        if (dto.getViewId() != 0) {
            return getViewData(dto, response);
        } else if (!CollectionUtils.isEmpty(dto.getAttributeGroups())) {
            return getAttributeGroupData(dto, response);
        }
        return getMasterData(dto, response);
    }

    @Override
    public FileDTO downLoadList(MasterDataQueryDTO dto, HttpServletResponse response) {
        if (dto.getViewId() != 0) {
            return downloadViewData(dto, response);
        } else if (!CollectionUtils.isEmpty(dto.getAttributeGroups())) {
             return downloadAttributeGroupData(dto, response);
        }
        return downloadMasterData(dto, response);
    }

    @Override
    public void downLoadExcel(FileDTO dto, HttpServletResponse response) {
        String filePath = System.getProperty("user.dir")+ File.separator +"downloadExcel";
        File file = new File(filePath+File.separator+dto.getFilePath());
        File directory = new File(filePath);
        log.info("filePath:"+filePath);
        // 判断目录是否存在，不存在则创建目录
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 设置响应头，告诉浏览器这是一个文件下载
        response.setContentType("application/octet-stream;charset=UTF-8");
        // 设置文件大小
        response.setContentLengthLong(file.length());
        // 设置下载文件名
        response.setHeader("Content-Disposition", "attachment; filename=\"" + dto.getFileName() + ".xlsx\"");
        response.addHeader("Pargam", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        // 获取输入流读取文件
        try (InputStream inputStream = Files.newInputStream(file.toPath());
             OutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            // 将文件内容写入响应输出流，浏览器会收到文件并启动下载
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            // 确保所有数据都被写入到客户端
            outputStream.flush();

            // 文件下载成功后，删除原文件
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    // 如果删除失败，打印日志或记录警告信息
                    log.warn("删除文件失败: " + file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  // 如果发生错误，设置500状态码
            e.printStackTrace();
        }
    }

    /**
     * 查询实体视图数据
     *
     * @param dto
     * @param response
     * @return
     */
    public ResultObjectVO getMasterData(MasterDataQueryDTO dto, HttpServletResponse response) {
        //准备返回对象
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        EntityVO entityVo = entityService.getDataById(dto.getEntityId());
        if (entityVo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(ModelPO::getId, entityVo.getModelId());
        ModelPO modelPO = modelMapper.selectOne(modelLambdaQueryWrapper);
        if (modelPO == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //获得主数据表名
        String tableName = TableNameGenerateUtils.generateViwTableName(modelPO.getName(),entityVo.getName());
        //查询该实体下已发布的属性
        List<AttributeInfoDTO> attributeInfos = attributeService.listPublishedAttribute(dto.getEntityId());
        if (attributeInfos.isEmpty()) {
            throw new FkException(ResultEnum.ATTRIBUTE_NOT_EXIST);
        }
        //将查询到的属性集合添加装入结果对象
        List<AttributeColumnVO> attributeColumnVoList = AttributeMap.INSTANCES.dtoListToVoList(attributeInfos);
        attributeColumnVoList.stream().map(e -> e.attributeGroupIds = attributeGroupService.getAttributeGroupIdByAttributeId(e.getId()))
                .collect(Collectors.toList());
        List<ResultAttributeGroupVO> attributeGroupVoList = new ArrayList<>();
        ResultAttributeGroupVO attributeGroupVo = new ResultAttributeGroupVO();
        attributeGroupVo.setName("");
        attributeGroupVo.setAttributes(attributeColumnVoList);
        attributeGroupVoList.add(attributeGroupVo);
        resultObjectVO.setAttributes(attributeGroupVoList);
        return queryMasterData(resultObjectVO, dto, tableName, attributeColumnVoList, response);
    }

    /**
     * 查询实体视图数据
     *
     * @param dto
     * @param response
     * @return
     */
    public FileDTO downloadMasterData(MasterDataQueryDTO dto, HttpServletResponse response) {
        //准备返回对象
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        EntityVO entityVo = entityService.getDataById(dto.getEntityId());
        if (entityVo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(ModelPO::getId, entityVo.getModelId());
        ModelPO modelPO = modelMapper.selectOne(modelLambdaQueryWrapper);
        if (modelPO == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //获得主数据表名
        String tableName = TableNameGenerateUtils.generateViwTableName(modelPO.getName(),entityVo.getName());
        //查询该实体下已发布的属性
        List<AttributeInfoDTO> attributeInfos = attributeService.listPublishedAttribute(dto.getEntityId());
        if (attributeInfos.isEmpty()) {
            throw new FkException(ResultEnum.ATTRIBUTE_NOT_EXIST);
        }
        //将查询到的属性集合添加装入结果对象
        List<AttributeColumnVO> attributeColumnVoList = AttributeMap.INSTANCES.dtoListToVoList(attributeInfos);
        attributeColumnVoList.stream().map(e -> e.attributeGroupIds = attributeGroupService.getAttributeGroupIdByAttributeId(e.getId()))
                .collect(Collectors.toList());
        List<ResultAttributeGroupVO> attributeGroupVoList = new ArrayList<>();
        ResultAttributeGroupVO attributeGroupVo = new ResultAttributeGroupVO();
        attributeGroupVo.setName("");
        attributeGroupVo.setAttributes(attributeColumnVoList);
        attributeGroupVoList.add(attributeGroupVo);
        resultObjectVO.setAttributes(attributeGroupVoList);
       return downLoadData(resultObjectVO, dto, tableName, attributeColumnVoList, response);
    }

    public FileDTO downLoadData(ResultObjectVO resultObjectVO,
                                          MasterDataQueryDTO dto,
                                          String tableName,
                                          List<AttributeColumnVO> attributeColumnVoList,
                                          HttpServletResponse response) {
        FileDTO fileDTO = new FileDTO();
        //数据类型英文名称赋值
        attributeColumnVoList
                .stream()
                .map(e -> e.dataTypeEnDisplay = DataTypeEnum.getValue(e.getDataType()).name())
                .collect(Collectors.toList());
        //获得业务字段名
        List<AttributeColumnVO> newColumnList = queryAttributeData(attributeColumnVoList);
        if (resultObjectVO.getAttributes().size() == 1 && StringUtils.isEmpty(resultObjectVO.getAttributes().get(0).getName())) {
            resultObjectVO.getAttributes().get(0).setAttributes(newColumnList);
        }
        //准备主数据集合
        List<Map<String, Object>> data;
        //查询字段
        String businessColumnName = StringUtils.join(newColumnList.stream()
                .map(e -> "\""+e.getName()+"\"").collect(Collectors.toList()), ",");
        try {
            String conditions = null;
            if(dto.getValidity() == null || dto.getValidity() == 2){
                conditions = " and fidata_version_id=" + dto.getVersionId() + "";
            }else{
                conditions = " and fidata_version_id=" + dto.getVersionId() + " and fidata_del_flag= " +dto.getValidity()+" ";
            }
            if (!CollectionUtils.isEmpty(dto.getFilterQuery())) {
                conditions = getOperatorCondition(dto.getFilterQuery());
            }
            //获取总条数
            int rowCount = 0;
            IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
            //获取总条数sql
            String count = buildSqlCommand.buildQueryCount(tableName, conditions);
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
            dataPageDTO.setConditions(conditions);
            dataPageDTO.setValidity(dto.getValidity());
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            String sql = sqlBuilder.buildMasterDataPage(dataPageDTO);
            //执行sql，获得结果集
            log.info("执行sql: 【" + sql + "】");
            data = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
            //判断结果集是否为空
            if (CollectionUtils.isEmpty(data)) {
                resultObjectVO.setResultData(new ArrayList<>());
                //return resultObjectVO;
            }
            //创建人/更新人id替换为名称
            ReplenishUserInfo.replenishFiDataUserName(data, client, UserFieldEnum.USER_NAME);

            ExportResultVO vo = new ExportResultVO();
            List<String> nameList = newColumnList.stream().map(e -> e.getName()).collect(Collectors.toList());
            List<String> nameDisplayList = newColumnList.stream().map(e -> e.getDisplayName()).collect(Collectors.toList());
            vo.setHeaderList(nameList);
            vo.setDataArray(data);
            vo.setHeaderDisplayList(nameDisplayList);
            vo.setFileName(tableName);
            fileDTO = exportExcel1(vo);
        } catch (Exception e) {
            log.error("getMasterDataPage:", e.getMessage());
        }
        return fileDTO;
    }


    /**
     * 自定义视图筛选
     *
     * @param dto
     * @param response
     * @return
     */
    public ResultObjectVO getViewData(MasterDataQueryDTO dto, HttpServletResponse response) {
        //准备返回对象
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        ViwGroupVO viwGroupVO = viwGroupService.getDataByGroupId(dto.getViewId()).get(0);
        if (viwGroupVO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //判断视图下是否有属性
        if (CollectionUtils.isEmpty(viwGroupVO.getGroupDetailsList())) {
            throw new FkException(ResultEnum.VIEW_NO_EXIST_ATTRIBUTE);
        }
        List<Integer> attributeIds = viwGroupVO.getGroupDetailsList().stream()
                .map(e -> e.getAttributeId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attributeIds)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", attributeIds).orderByAsc("entity_id").orderByAsc("sort_wieght");
        List<AttributePO> poList = attributeMapper.selectList(queryWrapper);
        List<AttributeInfoDTO> attributeInfos = AttributeMap.INSTANCES.poToDtoList(poList);
        for (AttributeInfoDTO item : attributeInfos) {
            EntityPO entityPO = entityMapper.selectById(item.getEntityId());
            if (entityPO == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            item.setName((entityPO.getName() + "_" + item.getName()).toLowerCase());
            Optional<ViwGroupDetailsDTO> first = viwGroupVO.getGroupDetailsList()
                    .stream()
                    .filter(e -> e.getAttributeId().equals(item.getId()))
                    .findFirst();
            if (first.isPresent() && StringUtils.isEmpty(first.get().getAliasName())) {
                continue;
            }
            item.setDisplayName(first.get().getAliasName());
        }
        List<AttributeColumnVO> attributeColumnVoList = AttributeMap.INSTANCES.dtoListToVoList(attributeInfos);
        attributeColumnVoList.stream().map(e -> e.attributeGroupIds = attributeGroupService.getAttributeGroupIdByAttributeId(e.getId()))
                .collect(Collectors.toList());
        List<ResultAttributeGroupVO> attributeGroupVoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dto.getAttributeGroups())) {
            List<AttributeColumnVO> newAttributeColumnVoList = CommonMethods.deepCopy(attributeColumnVoList);
            newAttributeColumnVoList.stream()
                    .map(e -> e.dataTypeEnDisplay = DataTypeEnum.getValue(e.getDataType()).name())
                    .collect(Collectors.toList());
            for (Integer id : dto.getAttributeGroups()) {
                attributeGroupVoList.add(setAttributeGroup(id, dto.getEntityId(), newAttributeColumnVoList));
            }
            List<Integer> attributeGroupIds = new ArrayList<>();
            //获取所有实体属性id集合
            attributeGroupVoList.stream().forEach(e ->
                    attributeGroupIds.addAll(e.getAttributes()
                            .stream().map(p -> p.getId()).collect(Collectors.toList())));
            if (!CollectionUtils.isEmpty(attributeGroupIds)) {
                attributeColumnVoList = attributeColumnVoList.stream()
                        .filter(e -> attributeGroupIds.contains(e.getId()))
                        .collect(Collectors.toList());
            }
            resultObjectVO.setAttributes(attributeGroupVoList);
            return queryMasterData(resultObjectVO, dto, viwGroupVO.getColumnName(), attributeColumnVoList, response);
        }
        ResultAttributeGroupVO attributeGroupVo = new ResultAttributeGroupVO();
        attributeGroupVo.setName("");
        attributeGroupVo.setAttributes(attributeColumnVoList);
        attributeGroupVoList.add(attributeGroupVo);
        resultObjectVO.setAttributes(attributeGroupVoList);
        return queryMasterData(resultObjectVO, dto, viwGroupVO.getColumnName(), attributeColumnVoList, response);
    }

    /**
     * 自定义视图筛选
     *
     * @param dto
     * @param response
     * @return
     */
    public FileDTO downloadViewData(MasterDataQueryDTO dto, HttpServletResponse response) {
        //准备返回对象
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        ViwGroupVO viwGroupVO = viwGroupService.getDataByGroupId(dto.getViewId()).get(0);
        if (viwGroupVO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //判断视图下是否有属性
        if (CollectionUtils.isEmpty(viwGroupVO.getGroupDetailsList())) {
            throw new FkException(ResultEnum.VIEW_NO_EXIST_ATTRIBUTE);
        }
        List<Integer> attributeIds = viwGroupVO.getGroupDetailsList().stream()
                .map(e -> e.getAttributeId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attributeIds)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        QueryWrapper<AttributePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", attributeIds).orderByAsc("entity_id").orderByAsc("sort_wieght");
        List<AttributePO> poList = attributeMapper.selectList(queryWrapper);
        List<AttributeInfoDTO> attributeInfos = AttributeMap.INSTANCES.poToDtoList(poList);
        for (AttributeInfoDTO item : attributeInfos) {
            EntityPO entityPO = entityMapper.selectById(item.getEntityId());
            if (entityPO == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            item.setName((entityPO.getName() + "_" + item.getName()).toLowerCase());
            Optional<ViwGroupDetailsDTO> first = viwGroupVO.getGroupDetailsList()
                    .stream()
                    .filter(e -> e.getAttributeId().equals(item.getId()))
                    .findFirst();
            if (first.isPresent() && StringUtils.isEmpty(first.get().getAliasName())) {
                continue;
            }
            item.setDisplayName(first.get().getAliasName());
        }
        List<AttributeColumnVO> attributeColumnVoList = AttributeMap.INSTANCES.dtoListToVoList(attributeInfos);
        attributeColumnVoList.stream().map(e -> e.attributeGroupIds = attributeGroupService.getAttributeGroupIdByAttributeId(e.getId()))
                .collect(Collectors.toList());
        List<ResultAttributeGroupVO> attributeGroupVoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dto.getAttributeGroups())) {
            List<AttributeColumnVO> newAttributeColumnVoList = CommonMethods.deepCopy(attributeColumnVoList);
            newAttributeColumnVoList.stream()
                    .map(e -> e.dataTypeEnDisplay = DataTypeEnum.getValue(e.getDataType()).name())
                    .collect(Collectors.toList());
            for (Integer id : dto.getAttributeGroups()) {
                attributeGroupVoList.add(setAttributeGroup(id, dto.getEntityId(), newAttributeColumnVoList));
            }
            List<Integer> attributeGroupIds = new ArrayList<>();
            //获取所有实体属性id集合
            attributeGroupVoList.stream().forEach(e ->
                    attributeGroupIds.addAll(e.getAttributes()
                            .stream().map(p -> p.getId()).collect(Collectors.toList())));
            if (!CollectionUtils.isEmpty(attributeGroupIds)) {
                attributeColumnVoList = attributeColumnVoList.stream()
                        .filter(e -> attributeGroupIds.contains(e.getId()))
                        .collect(Collectors.toList());
            }
            resultObjectVO.setAttributes(attributeGroupVoList);
            downLoadData(resultObjectVO, dto, viwGroupVO.getColumnName(), attributeColumnVoList, response);
        }
        ResultAttributeGroupVO attributeGroupVo = new ResultAttributeGroupVO();
        attributeGroupVo.setName("");
        attributeGroupVo.setAttributes(attributeColumnVoList);
        attributeGroupVoList.add(attributeGroupVo);
        resultObjectVO.setAttributes(attributeGroupVoList);
        return downLoadData(resultObjectVO, dto, viwGroupVO.getColumnName(), attributeColumnVoList, response);
    }

    /**
     * 属性组筛选
     *
     * @param dto
     * @param response
     * @return
     */
    public ResultObjectVO getAttributeGroupData(MasterDataQueryDTO dto, HttpServletResponse response) {
        //准备返回对象
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        EntityVO entityVo = entityService.getDataById(dto.getEntityId());
        if (entityVo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(ModelPO::getId, entityVo.getModelId());
        ModelPO modelPO = modelMapper.selectOne(modelLambdaQueryWrapper);
        if (modelPO == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //获得主数据表名
        String tableName = TableNameGenerateUtils.generateViwTableName(modelPO.getName(), entityVo.getName());
        //查询该实体下发布的属性
        List<AttributeInfoDTO> attributeInfos = attributeService.listPublishedAttribute(dto.getEntityId());
        if (attributeInfos.isEmpty()) {
            throw new FkException(ResultEnum.ATTRIBUTE_NOT_EXIST);
        }
        //将查询到的属性集合添加装入结果对象
        List<AttributeColumnVO> attributeColumnVoList = AttributeMap.INSTANCES.dtoListToVoList(attributeInfos);
        List<ResultAttributeGroupVO> attributeGroupVoList = new ArrayList<>();
        //组装属性组数据
        List<AttributeColumnVO> newAttributeColumnVoList = CommonMethods.deepCopy(attributeColumnVoList);
        newAttributeColumnVoList.stream()
                .map(e -> e.dataTypeEnDisplay = DataTypeEnum.getValue(e.getDataType()).name())
                .collect(Collectors.toList());
        for (Integer id : dto.getAttributeGroups()) {
            attributeGroupVoList.add(setAttributeGroup(id, dto.getEntityId(), newAttributeColumnVoList));
        }
        List<Integer> attributeIdList = new ArrayList<>();
        //获取属性组实体属性id集合
        attributeGroupVoList.stream().forEach(e ->
                attributeIdList.addAll(e.getAttributes()
                        .stream().map(p -> p.getId()).collect(Collectors.toList())));
        if (!CollectionUtils.isEmpty(attributeIdList)) {
            attributeColumnVoList = attributeColumnVoList.stream()
                    .filter(e -> attributeIdList.contains(e.getId()))
                    .collect(Collectors.toList());
        }
        resultObjectVO.setAttributes(attributeGroupVoList);
        return queryMasterData(resultObjectVO, dto, tableName, attributeColumnVoList, response);
    }

    /**
     * 属性组筛选
     *
     * @param dto
     * @param response
     * @return
     */
    public FileDTO downloadAttributeGroupData(MasterDataQueryDTO dto, HttpServletResponse response) {
        //准备返回对象
        ResultObjectVO resultObjectVO = new ResultObjectVO();
        EntityVO entityVo = entityService.getDataById(dto.getEntityId());
        if (entityVo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(ModelPO::getId, entityVo.getModelId());
        ModelPO modelPO = modelMapper.selectOne(modelLambdaQueryWrapper);
        if (modelPO == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //获得主数据表名
        String tableName = TableNameGenerateUtils.generateViwTableName(modelPO.getName(), entityVo.getName());
        //查询该实体下发布的属性
        List<AttributeInfoDTO> attributeInfos = attributeService.listPublishedAttribute(dto.getEntityId());
        if (attributeInfos.isEmpty()) {
            throw new FkException(ResultEnum.ATTRIBUTE_NOT_EXIST);
        }
        //将查询到的属性集合添加装入结果对象
        List<AttributeColumnVO> attributeColumnVoList = AttributeMap.INSTANCES.dtoListToVoList(attributeInfos);
        List<ResultAttributeGroupVO> attributeGroupVoList = new ArrayList<>();
        //组装属性组数据
        List<AttributeColumnVO> newAttributeColumnVoList = CommonMethods.deepCopy(attributeColumnVoList);
        newAttributeColumnVoList.stream()
                .map(e -> e.dataTypeEnDisplay = DataTypeEnum.getValue(e.getDataType()).name())
                .collect(Collectors.toList());
        for (Integer id : dto.getAttributeGroups()) {
            attributeGroupVoList.add(setAttributeGroup(id, dto.getEntityId(), newAttributeColumnVoList));
        }
        List<Integer> attributeIdList = new ArrayList<>();
        //获取属性组实体属性id集合
        attributeGroupVoList.stream().forEach(e ->
                attributeIdList.addAll(e.getAttributes()
                        .stream().map(p -> p.getId()).collect(Collectors.toList())));
        if (!CollectionUtils.isEmpty(attributeIdList)) {
            attributeColumnVoList = attributeColumnVoList.stream()
                    .filter(e -> attributeIdList.contains(e.getId()))
                    .collect(Collectors.toList());
        }
        resultObjectVO.setAttributes(attributeGroupVoList);
        return downLoadData(resultObjectVO, dto, tableName, attributeColumnVoList, response);
    }

    public ResultObjectVO queryMasterData(ResultObjectVO resultObjectVO,
                                          MasterDataQueryDTO dto,
                                          String tableName,
                                          List<AttributeColumnVO> attributeColumnVoList,
                                          HttpServletResponse response) {
        //数据类型英文名称赋值
        attributeColumnVoList
                .stream()
                .map(e -> e.dataTypeEnDisplay = DataTypeEnum.getValue(e.getDataType()).name())
                .collect(Collectors.toList());
        //获得业务字段名
        List<AttributeColumnVO> newColumnList = queryAttributeData(attributeColumnVoList);
        if (resultObjectVO.getAttributes().size() == 1 && StringUtils.isEmpty(resultObjectVO.getAttributes().get(0).getName())) {
            resultObjectVO.getAttributes().get(0).setAttributes(newColumnList);
        }
        //准备主数据集合
        List<Map<String, Object>> data;
        //查询字段
        String businessColumnName = StringUtils.join(newColumnList.stream()
                .map(e -> "\""+e.getName()+"\"").collect(Collectors.toList()), ",");
        try {
            String conditions = null;
            if(dto.getValidity() == null || dto.getValidity() == 2){
                conditions = " and fidata_version_id=" + dto.getVersionId() + "";
            }else{
                conditions = " and fidata_version_id=" + dto.getVersionId() + " and fidata_del_flag= " +dto.getValidity()+" ";
            }
            if (!CollectionUtils.isEmpty(dto.getFilterQuery())) {
                conditions = getOperatorCondition(dto.getFilterQuery());
            }
            //获取总条数
            int rowCount = 0;
            IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
            //获取总条数sql
            String count = buildSqlCommand.buildQueryCount(tableName, conditions);
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
            dataPageDTO.setConditions(conditions);
            dataPageDTO.setValidity(dto.getValidity());
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            String sql = sqlBuilder.buildMasterDataPage(dataPageDTO);
            //执行sql，获得结果集
            log.info("执行sql: 【" + sql + "】");
            data = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
            //判断结果集是否为空
            if (CollectionUtils.isEmpty(data)) {
                resultObjectVO.setResultData(new ArrayList<>());
                //return resultObjectVO;
            }
            //创建人/更新人id替换为名称
            ReplenishUserInfo.replenishFiDataUserName(data, client, UserFieldEnum.USER_NAME);
            //是否导出
            if (dto.getExport()) {
                ExportResultVO vo = new ExportResultVO();
                List<String> nameList = newColumnList.stream().map(e -> e.getName()).collect(Collectors.toList());
                List<String> nameDisplayList = newColumnList.stream().map(e -> e.getDisplayName()).collect(Collectors.toList());
                vo.setHeaderList(nameList);
                vo.setDataArray(data);
                vo.setHeaderDisplayList(nameDisplayList);
                vo.setFileName(tableName);
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

    /**
     * 主数据维护列表，当为域字段时，需要展示域字段编码和名称
     *
     * @param attributeColumnVo
     * @return
     */
    public AttributeColumnVO getCodeAndName(AttributeColumnVO attributeColumnVo, DataTypeEnum dataTypeEnum) {
        AttributeColumnVO vo = new AttributeColumnVO();
        switch (dataTypeEnum) {
            case DOMAIN:
                vo.setDisplayName(TableNameGenerateUtils.generateDomainCodeDisplayName(attributeColumnVo.getDisplayName()));
                vo.setName(TableNameGenerateUtils.generateDomainCode(attributeColumnVo.getName()));
                break;
            case TEXT:
                vo.setDisplayName(attributeColumnVo.getDisplayName());
                vo.setName(attributeColumnVo.getName());
            default:
                break;
        }
        vo.setDataType(attributeColumnVo.getDataType());
        vo.setDataTypeEnDisplay(attributeColumnVo.getDataTypeEnDisplay());
        vo.setEnableRequired(attributeColumnVo.getEnableRequired());
        vo.setSortWieght(attributeColumnVo.getSortWieght());
        vo.setDisplayWidth(attributeColumnVo.getDisplayWidth());
        vo.setEntityId(attributeColumnVo.getEntityId());
        vo.setAttributeGroupIds(new ArrayList<>());
        return vo;
    }

    /**
     * 获取属性组以及属性组下实体属性
     *
     * @param attributeGroupId
     * @param entity
     * @param attributeColumnVoList
     * @return
     */
    public ResultAttributeGroupVO setAttributeGroup(Integer attributeGroupId,
                                                    Integer entity,
                                                    List<AttributeColumnVO> attributeColumnVoList) {
        List<Integer> attributes = attributeGroupService.getAttributeGroupAttribute(attributeGroupId, entity);
        //获取属性组名称
        AttributeGroupDTO attributeGroup = attributeGroupService.getAttributeGroup(attributeGroupId);
        ResultAttributeGroupVO attributeGroupVo = new ResultAttributeGroupVO();
        attributeGroupVo.setName(attributeGroup.getName());
        List<AttributeColumnVO> collect = attributeColumnVoList.stream()
                .filter(e -> attributes.contains(e.getId())).collect(Collectors.toList());
        collect.stream().map(e -> e.attributeGroupIds = attributeGroupService.getAttributeGroupIdByAttributeId(e.getId()))
                .collect(Collectors.toList());
        //获得业务字段名
        attributeGroupVo.setAttributes(queryAttributeData(collect));
        return attributeGroupVo;
    }

    /**
     * 不同实体属性类型，需添加默认字段
     *
     * @param attributeColumnVoList
     * @return
     */
    public List<AttributeColumnVO> queryAttributeData(List<AttributeColumnVO> attributeColumnVoList) {
        List<AttributeColumnVO> newColumnList = new ArrayList<>();
        for (AttributeColumnVO attributeColumnVo : attributeColumnVoList) {
            //域字段添加编码和名称表头
            if (attributeColumnVo.getDataType().equals(DataTypeEnum.DOMAIN.getName())) {
                newColumnList.add(getCodeAndName(attributeColumnVo, DataTypeEnum.DOMAIN));
                attributeColumnVo.setDisplayName(TableNameGenerateUtils.generateDomainNameDisplayName(attributeColumnVo.getDisplayName()));
                attributeColumnVo.setName(TableNameGenerateUtils.generateDomainName(attributeColumnVo.getName()));
            }
            newColumnList.add(attributeColumnVo);
        }
        return newColumnList;
    }

    /**
     * 拼接筛选条件
     *
     * @param operators
     * @return
     */
    public String getOperatorCondition(List<FilterQueryDTO> operators) {
        IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
        return sqlBuilder.buildOperatorCondition(operators);
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
    public int templateDataSubmitStg(List<Map<String, Object>> members, String tableName,
                                     String batchCode, int versionId, long userId,
                                     ImportTypeEnum importTypeEnum, boolean delete) {
        Connection conn = null;
        Statement stat = null;
        try{
            conn = getConnection();
            stat = conn.createStatement();
            conn.getAutoCommit();
            conn.setAutoCommit(false);
            InsertImportDataDTO dto = new InsertImportDataDTO();
            dto.setBatchCode(batchCode);
            dto.setImportType(importTypeEnum.getValue());
            dto.setVersionId(versionId);
            dto.setUserId(userId);
            dto.setMembers(members);
            dto.setTableName(tableName);
            dto.setDelete(delete);
            //调用生成批量insert语句方法
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            String sql = sqlBuilder.buildInsertImportData(dto);
            ////log.info("模板批量添加sql:", sql);
            stat.addBatch(sql);
            int[] flatCount = stat.executeBatch();
            conn.commit();
            return flatCount[0];
        } catch (SQLException e) {
            log.error("模板批量导入失败:", e);
            //关闭连接
            AbstractDbHelper.rollbackConnection(conn);
            throw new FkException(ResultEnum.DATA_SUBMIT_ERROR, e);
        }finally {
            //关闭连接
            AbstractDbHelper.closeStatement(stat);
            AbstractDbHelper.closeConnection(conn);
        }

    }

    @Override
    public ResultEnum importDataSubmit(ImportDataSubmitDTO dto) {

        EntityPO entityPO = entityMapper.selectById(dto.getEntityId());
        if (entityPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(ModelPO::getId, entityPO.getModelId());
        ModelPO modelPO = modelMapper.selectOne(modelLambdaQueryWrapper);
        if (modelPO == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        String tableName = TableNameGenerateUtils.generateStgTableName(modelPO.getName(), entityPO.getName());
        //where条件
        String queryConditions = " and fidata_batch_code ='" + dto.getKey() + "' and fidata_status=" + SyncStatusTypeEnum.UPLOADED_FAILED.getValue();
        IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
        //查询提交数据是否存在错误数据
        String sql = buildSqlCommand.buildQueryCount(tableName, queryConditions);
        List<Map<String, Object>> maps = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
        if (!CollectionUtils.isEmpty(maps) && Integer.valueOf(maps.get(0).get("totalnum").toString()).intValue() > 0) {
            throw new FkException(ResultEnum.EXISTS_INCORRECT_DATA);
        }
        ResultEnum resultEnum = dataSynchronizationUtils.stgDataSynchronize(dto.getEntityId(), dto.getKey());
        if (resultEnum.getCode() != ResultEnum.DATA_SYNCHRONIZATION_SUCCESS.getCode()) {
            return resultEnum;
        }
        return resultEnum == ResultEnum.DATA_SYNCHRONIZATION_SUCCESS ? ResultEnum.SUCCESS : resultEnum;
    }

    @Override
    public BathUploadMemberVO importDataQuery(ImportDataQueryDTO dto) {
        EntityPO entityPO = entityMapper.selectById(dto.getEntityId());
        if (entityPO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(ModelPO::getId, entityPO.getModelId());
        ModelPO modelPO = modelMapper.selectOne(modelLambdaQueryWrapper);
        if (modelPO == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        BathUploadMemberVO vo = new BathUploadMemberVO();
        vo.setEntityName(entityPO.getDisplayName());
        vo.setEntityId(dto.getEntityId());
        List<AttributeInfoDTO> attributeInfos = attributeService.listPublishedAttribute(dto.getEntityId());
        attributeInfos
                .stream()
                .map(e -> e.dataTypeEnDisplay = DataTypeEnum.getValue(e.getDataType()).name())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attributeInfos)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        try {
            String tableName = TableNameGenerateUtils.generateStgTableName(modelPO.getName(), entityPO.getName());
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
                vo.setCount(Integer.valueOf(resultMaps.get(0).get("totalnum").toString()).intValue());
                vo.setUpdateCount(Integer.valueOf(resultMaps.get(0).get("updatecount").toString()).intValue());
                vo.setAddCount(Integer.valueOf(resultMaps.get(0).get("addcount").toString()).intValue());
                vo.setSuccessCount(Integer.valueOf(resultMaps.get(0).get("successcount").toString()).intValue());
                vo.setErrorCount(Integer.valueOf(resultMaps.get(0).get("errorcount").toString()).intValue());
                vo.setSubmitSuccessCount(Integer.valueOf(resultMaps.get(0).get("submitsuccesscount").toString()).intValue());
                vo.setSubmitErrorCount(Integer.valueOf(resultMaps.get(0).get("submiterrorcount").toString()).intValue());
            }
            ImportDataPageDTO pageDataDTO = new ImportDataPageDTO();
            pageDataDTO.setPageIndex(dto.getPageIndex());
            pageDataDTO.setPageSize(dto.getPageSize());
            pageDataDTO.setBatchCode(dto.getKey());
            pageDataDTO.setStatus(dto.getStatus());
            pageDataDTO.setSyncType(dto.getSyncType());
            pageDataDTO.setTableName(TableNameGenerateUtils.generateStgTableName(modelPO.getName(), entityPO.getName()));
            //调用生成分页语句方法
            IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
            String sql = sqlBuilder.buildImportDataPage(pageDataDTO);
            List<Map<String, Object>> resultPageMaps = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
            AttributeInfoDTO infoDTO = new AttributeInfoDTO();
            infoDTO.setName("fidata_new_code");
            infoDTO.setDisplayName("新编码");
            infoDTO.setDataType(DataTypeEnum.TEXT.getName());
            infoDTO.setDataTypeEnDisplay(DataTypeEnum.TEXT.name());
            attributeInfos.add(1, infoDTO);
            vo.setAttribute(attributeInfos);
            vo.setMembers(resultPageMaps);
        } catch (Exception e) {
            log.error("importDataQuery:", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, e);
        }
        return vo;
    }

    @Override
    public BathUploadMemberListVO importTemplateData(ImportParamDTO dto, MultipartFile file) {
        //校验文件格式
        if (!file.getOriginalFilename().contains(".xlsx")) {
            throw new FkException(ResultEnum.FILE_NAME_ERROR);
        }
        //如果配置流程则走流程
        boolean flag;
        try {
            ResultEnum resultEnum = processService.verifyProcessApply(dto.getEntityId());
            switch (resultEnum){
                case VERIFY_APPROVAL:
                    flag = true;
                    break;
                case VERIFY_NOT_APPROVAL:
                    flag = false;
                    break;
                default:
                    throw new FkException(ResultEnum.VERIFY_PROCESS_APPLY_ERROR);
            }
        }catch (FkException e){
            throw new FkException(ResultEnum.VERIFY_PROCESS_APPLY_ERROR,e.getMessage());
        }
        EntityPO po = entityMapper.selectById(dto.getEntityId());
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(ModelPO::getId, po.getModelId());
        ModelPO modelPO = modelMapper.selectOne(modelLambdaQueryWrapper);
        if (modelPO == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //获取已发布的属性
        List<AttributeInfoDTO> list = attributeService.listPublishedAttribute(dto.getEntityId());
        //过滤复杂类型数据
        list = list.stream().filter(e -> !e.getDataType().equals(DataTypeEnum.LATITUDE_COORDINATE.getName()))
                .filter(e -> !e.getDataType().equals(DataTypeEnum.FILE.getName()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        BathUploadMemberListVO listVo = new BathUploadMemberListVO();
        BathUploadMemberVO result = new BathUploadMemberVO();
        result.setVersionId(dto.getVersionId());
        result.setEntityId(dto.getEntityId());
        result.setEntityName(po.getDisplayName());
        String tableName = TableNameGenerateUtils.generateStgTableName(modelPO.getName(),po.getName());
        //获取mdm表code列名
        Optional<AttributeInfoDTO> codeColumn = list.stream().filter(e -> e.getName().equals(MdmTypeEnum.CODE.getName())).findFirst();
        if (!codeColumn.isPresent()) {
            throw new FkException(ResultEnum.CODE_NOT_EXIST);
        }
        //获取mdm表code数据列表
        Map<String, String> codeMap = getCodeAndLockList(TableNameGenerateUtils.generateMdmTableName(modelPO.getName(), po.getName()), codeColumn.get().getColumnName(), dto.getVersionId());
        if (codeMap.size()>0){
            List<Object> collect = codeMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)){
                throw new FkException(ResultEnum.PROCESS_APPLY_EXIST);
            }
        }
        String batchNumber = UUID.randomUUID().toString();
        //解析Excel数据集合
        CopyOnWriteArrayList<Map<String, Object>> objectArrayList = new CopyOnWriteArrayList<>();
        //添加条数、修改条数
        AtomicInteger addCount = new AtomicInteger(0);
        AtomicInteger updateCount = new AtomicInteger(0);
        //成功条数、错误条数
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        //获取code编码规则
        List<CodeRuleVO> codeRuleVOS= codeRuleService.getDataByEntityId(dto.getEntityId());
        List<CodeRuleDTO> codeRuleDTO=null;
        if (CollectionUtils.isNotEmpty(codeRuleVOS)){
            codeRuleDTO= codeRuleVOS.get(0).getGroupDetailsList();
        }
        //code生成规则
        IBuildCodeCommand buildCodeCommand = BuildCodeHelper.getCodeCommand();
        //用户id
        long userId = userHelper.getLoginUserInfo().id;
        List<AttributeInfoDTO> attributePoList = new ArrayList<>();
        //获得总行数
        int rowNum;
        //列数
        int columnNum;
        Sheet sheet;
        try {
            //创建工作簿
            Workbook workbook;
            workbook = WorkbookFactory.create(file.getInputStream());
            sheet = workbook.getSheetAt(0);
            if (sheet.getRow(0) == null) {
                throw new FkException(ResultEnum.EMPTY_FORM);
            }
            //列数
            columnNum = sheet.getRow(0).getPhysicalNumberOfCells();
            //获得总行数
            rowNum = sheet.getPhysicalNumberOfRows();
            Row row1 = sheet.getRow(0);
            //获取表头
            for (int col = 0; col < columnNum; col++) {
                Cell cell = row1.getCell(col);
                if (cell.getStringCellValue().equals("新编码")) {
                    AttributeInfoDTO newCode = new AttributeInfoDTO();
                    newCode.setName("fidata_new_code");
                    attributePoList.add(newCode);
                    continue;
                }
                //判断是否为基于域字段,基于域字段存在两列，xxxx_编码 xxxx_名称
                AttributeInfoDTO domainAttributeCode = list.stream().filter(e -> e.getDataType().equals(DataTypeEnum.DOMAIN.getName())&&(e.getDisplayName() + "_编码").equals(cell.getStringCellValue())).findFirst().orElse(null);
                if(domainAttributeCode!=null){
                    AttributeInfoDTO domainColumnCode = new AttributeInfoDTO();
                    domainColumnCode.setName(domainAttributeCode.getName());
                    attributePoList.add(domainColumnCode);
                    continue;
                }
                AttributeInfoDTO domainAttributeName = list.stream().filter(e -> e.getDataType().equals(DataTypeEnum.DOMAIN.getName())&&(e.getDisplayName() + "_名称").equals(cell.getStringCellValue())).findFirst().orElse(null);
                if(domainAttributeName!=null){
                    attributePoList.add(null);
                    continue;
                }
                Optional<AttributeInfoDTO> data = list.stream().filter(e -> cell.getStringCellValue().equals(e.getDisplayName())).findFirst();
                if (!data.isPresent()) {
                    throw new FkException(ResultEnum.EXIST_INVALID_COLUMN);
                }
                attributePoList.add(data.get());
            }
            if(flag) {
                //添加工单
                processService.addProcessApply(dto.getEntityId(), null, batchNumber, EventTypeEnum.IMPORT);
            }
//            if (list.size() != columnNum - 1) {
//                throw new FkException(ResultEnum.CHECK_TEMPLATE_IMPORT_FAILURE);
//            }
        } catch (Exception e) {
            log.error("导入模板错误:", e);
            throw new FkException(ResultEnum.CHECK_TEMPLATE_IMPORT_FAILURE);
        }
        result.setAttribute(attributePoList);
        result.setCount(rowNum - 1);
        //每个线程执行条数
        final int threadHandleNumber = MdmConstants.THREAD_EXECUTE_NUMBER;
        //计算线程数
        int truncInt = (int) Math.rint((rowNum / threadHandleNumber));
        int threadCount = rowNum % threadHandleNumber == 0 ? rowNum / threadHandleNumber : truncInt + 1;
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        try {
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
                                    if(attributePoList.get(col)!=null){
                                        //判断字段类型
                                        if (cell != null) {
                                            ImportDataVerifyDTO cellDataDTO = MasterDataFormatVerifyUtils.getCellDataType(cell,
                                                    attributePoList.get(col).getDisplayName(),
                                                    attributePoList.get(col).getDataType());
                                            value = cellDataDTO.getValue();
                                            errorMsg += cellDataDTO.getSuccess() ? "" : cellDataDTO.getErrorMsg();
                                        }
                                        jsonObj.put(attributePoList.get(col).getName(), dto.getRemoveSpace() ? value.trim() : value);
                                    }
                                }
                                //验证code
                                ImportDataVerifyDTO verifyDTO = MasterDataFormatVerifyUtils.verifyCode(jsonObj);
                                errorMsg += verifyDTO.getSuccess() ? "" : verifyDTO.getErrorMsg();
                                if (StringUtils.isEmpty(jsonObj.get("code").toString())
                                        && StringUtils.isEmpty(jsonObj.get("fidata_new_code").toString())) {
                                    jsonObj.put("code", buildCodeCommand.createCode());
                                }
                                //上传逻辑：1 修改 2 新增
                                if (codeMap.containsKey(jsonObj.get("code"))) {
                                    jsonObj.put("fidata_syncy_type", SyncTypeStatusEnum.UPDATE.getValue());
                                    updateCount.incrementAndGet();
                                } else {
                                    jsonObj.put("fidata_syncy_type", SyncTypeStatusEnum.INSERT.getValue());
                                    addCount.incrementAndGet();
                                }
                                Date date = new Date();
                                jsonObj.put("fidata_create_time", CommonMethods.getFormatDate(date));
                                jsonObj.put("fidata_create_user", userId);
                                jsonObj.put("fidata_update_time", CommonMethods.getFormatDate(date));
                                jsonObj.put("fidata_update_user", userId);
                                jsonObj.put("fidata_error_msg", errorMsg);
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
        } catch (Exception e) {
            log.error("解析Excel表格存在错误格式数据:", e);
            throw new FkException(ResultEnum.EXISTS_INCORRECT_DATA);
        }
        if (CollectionUtils.isEmpty(objectArrayList)) {
            throw new FkException(ResultEnum.FORM_NO_VALID_DATA);
        }
        int flatCount = templateDataSubmitStg(objectArrayList, tableName, batchNumber, dto.getVersionId(), userId, ImportTypeEnum.EXCEL_IMPORT, false);
        if (flatCount == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        //添加批次
        setStgBatch(batchNumber, dto.getEntityId(), dto.getVersionId(), result.getCount(), result.getCount() - flatCount, addCount.get(), updateCount.get(), flatCount > 0 ? 0 : 1);
        //校验重复code
        verifyRepeatCode(tableName, batchNumber);
        result.setMembers(objectArrayList);
        result.setAddCount(addCount.get());
        result.setUpdateCount(updateCount.get());
        List<BathUploadMemberVO> bathUploadMemberVOList = new ArrayList<>();
        bathUploadMemberVOList.add(result);
        listVo.setKey(batchNumber);
        listVo.setList(bathUploadMemberVOList);
        return listVo;
    }

    @Override
    public List<OperatorVO> getSearchOperator() {
        IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
        return sqlBuilder.getOperatorList();
    }

    @Override
    public List<Map<String, Object>> listEntityCodeAndName(MasterDataBaseDTO dto) {
        EntityPO entityPo = entityMapper.selectById(dto.getEntityId());
        if (entityPo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(ModelPO::getId, entityPo.getModelId());
        ModelPO modelPO = modelMapper.selectOne(modelLambdaQueryWrapper);
        if (modelPO == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //查询该实体下发布的属性
        List<AttributeInfoDTO> attributeInfos = attributeService.listPublishedAttribute(dto.getEntityId());
        if (attributeInfos.isEmpty()) {
            throw new FkException(ResultEnum.ATTRIBUTE_NOT_EXIST);
        }
        Optional<AttributeInfoDTO> codeColumn = attributeInfos.stream().filter(e -> MdmTypeEnum.CODE.getName().equals(e.getName())).findFirst();
        Optional<AttributeInfoDTO> nameColumn = attributeInfos.stream().filter(e -> MdmTypeEnum.NAME.getName().equals(e.getName())).findFirst();
        if (!codeColumn.isPresent() || !nameColumn.isPresent()) {
            throw new FkException(ResultEnum.EXIST_INVALID_COLUMN);
        }
        String tableName = TableNameGenerateUtils.generateMdmTableName(modelPO.getName(), entityPo.getName());
        IBuildSqlCommand sqlBuilder = BuildFactoryHelper.getDBCommand(type);
        //生成查询语句
        String selectSql = sqlBuilder.buildQueryCodeAndName(tableName, codeColumn.get().getColumnName(), nameColumn.get().getColumnName(), dto.getVersionId());
        return AbstractDbHelper.execQueryResultMaps(selectSql, getConnection());
    }

    @Override
    public ResultEnum updateImportData(UpdateImportDataDTO dto) {
        try {
            EntityPO entityPO = entityMapper.selectById(dto.getEntityId());
            if (entityPO == null) {
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
            modelLambdaQueryWrapper.eq(ModelPO::getId, entityPO.getModelId());
            ModelPO modelPO = modelMapper.selectOne(modelLambdaQueryWrapper);
            if (modelPO == null){
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
            String tableName = TableNameGenerateUtils.generateStgTableName(modelPO.getName(),entityPO.getName());
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
            int versionId = Integer.parseInt(dto.getData().get("fidata_version_id").toString());
            //获取mdm表code数据列表
            Map<String, String> codeMap = getCodeAndLockList(TableNameGenerateUtils.generateMdmTableName(modelPO.getName(), entityPO.getName()), attributePO.getColumnName(), versionId);
            //判断上传逻辑
            dto.getData().put("fidata_syncy_type", SyncTypeStatusEnum.INSERT.getValue());
            if (codeMap.containsKey(dto.getData().get("code"))) {
                if (codeMap.get(dto.getData().get("code")) == null){
                    dto.getData().put("fidata_syncy_type", SyncTypeStatusEnum.UPDATE.getValue());
                }else {
                    throw new FkException(ResultEnum.PROCESS_APPLY_EXIST);
                }
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

    @Override
    public ResultEnum addMasterData(MasterDataDTO dto) {
        return OperateMasterData(dto, EventTypeEnum.SAVE);
    }

    @Override
    public ResultEnum delMasterData(MasterDataDTO dto) {
        return OperateMasterData(dto, EventTypeEnum.DELETE);
    }

    @Override
    public ResultEnum updateMasterData(MasterDataDTO dto) {
        return OperateMasterData(dto, EventTypeEnum.UPDATE);
    }

    /**
     * 单条数据维护公共方法
     *
     * @param dto
     * @param eventTypeEnum
     * @return
     */
    public ResultEnum OperateMasterData(MasterDataDTO dto, EventTypeEnum eventTypeEnum) {
        //如果配置流程则走流程
        boolean flag;
        try {
            ResultEnum resultEnum = processService.verifyProcessApply(dto.getEntityId());
            switch (resultEnum){
                case VERIFY_APPROVAL:
                    flag = true;
                    break;
                case VERIFY_NOT_APPROVAL:
                    flag = false;
                    break;
                default:
                    throw new FkException(ResultEnum.VERIFY_PROCESS_APPLY_ERROR);
            }
        }catch (FkException e){
            throw new FkException(ResultEnum.VERIFY_PROCESS_APPLY_ERROR,e.getMessage());
        }
        EntityPO entityPO = entityMapper.selectById(dto.getEntityId());
        if (entityPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        LambdaQueryWrapper<ModelPO> modelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        modelLambdaQueryWrapper.eq(ModelPO::getId, entityPO.getModelId());
        ModelPO modelPO = modelMapper.selectOne(modelLambdaQueryWrapper);
        if (modelPO == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        String tableName = TableNameGenerateUtils.generateStgTableName(modelPO.getName(),entityPO.getName());
        List<Map<String, Object>> members = new ArrayList<>();
        List<Map<String, Object>> mapDatasDto=dto.getMembers();
        Long userId = userHelper.getLoginUserInfo().id;
        for(Map<String, Object> mapDataItem: mapDatasDto){
            Map<String, Object> mapData = new HashMap<>();
            mapData.putAll(mapDataItem);
            Date date = new Date();
            if (eventTypeEnum == EventTypeEnum.SAVE) {
                //校验code
                ImportDataVerifyDTO verifyResult = MasterDataFormatVerifyUtils.verifyCode(mapData);
                if (!verifyResult.getSuccess()) {
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR, verifyResult.getErrorMsg());
                }
                mapData.put("fidata_status", SyncStatusTypeEnum.UPLOADED_SUCCESSFULLY.getValue());
                mapData.put("fidata_syncy_type", SyncTypeStatusEnum.INSERT.getValue());
                mapData.put("fidata_create_time", CommonMethods.getFormatDate(date));
                mapData.put("fidata_create_user", userId);
                if (StringUtils.isEmpty(mapData.get("code") == null ? "" : mapData.get("code").toString())) {
                    //code生成规则
                    IBuildCodeCommand buildCodeCommand = BuildCodeHelper.getCodeCommand();
                    String code = buildCodeCommand.createCode();
                    mapData.put("code", code);
                    mapDataItem.put("code", code);
                } else {
                    //获取code列名
                    String columnName = getEntityCodeName(dto.getEntityId());
                    //code是否验证已存在
                    Map<String, String> codeList = getCodeAndLockList(TableNameGenerateUtils.generateMdmTableName(modelPO.getName(), entityPO.getName()), columnName, dto.getVersionId());
                    if (codeList.containsKey(mapData.get("code"))) {
                        throw new FkException(ResultEnum.CODE_EXIST,"code:"+mapData.get("code"));
                    }
                }
            }else {
                String mdmTableName = TableNameGenerateUtils.generateMdmTableName(modelPO.getName(), entityPO.getName());
                StringBuilder queryConditions = new StringBuilder();
                queryConditions.append(" and fidata_id ='")
                        .append(mapDataItem.get("fidataId"))
                        .append("' and fidata_lock_tag ='1'");
                IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
                String sql = buildSqlCommand.buildQueryData(mdmTableName, queryConditions.toString());
                Connection connection = getConnection();
                List<Map<String, Object>> maps = AbstractDbHelper.execQueryResultMaps(sql, connection);
                if(!org.springframework.util.CollectionUtils.isEmpty(maps)){
                    throw new FkException(ResultEnum.PROCESS_APPLY_EXIST);
                }
                if(flag){
                    StringBuilder update = new StringBuilder();
                    update.append("update \"")
                            .append(mdmTableName)
                            .append("\" set fidata_lock_tag = 1 where fidata_id ='")
                            .append(mapDataItem.get("fidataId")).append("'");
                    PreparedStatement statement = null;
                    try {
                        statement = getConnection().prepareStatement(update.toString());
                        statement.execute();
                    } catch (SQLException ex) {
                        // 回滚事务
                        rollbackConnection(connection);
                        // 记录日志
                        log.error(ResultEnum.FACT_ATTRIBUTE_FAILD.getMsg() + "【SQL:】" + update + "【原因:】" + ex.getMessage());
                        throw new FkException(ResultEnum.FACT_ATTRIBUTE_FAILD);
                    }finally {
                        AbstractDbHelper.closeConnection(connection);
                        AbstractDbHelper.closeStatement(statement);
                    }
                }
            }
            mapData.put("fidata_update_time", CommonMethods.getFormatDate(date));
            mapData.put("fidata_update_user", userId);
            mapData.remove("fidataId");
            //生成批次号
            members.add(mapData);
        }
        String batchNumber = UUID.randomUUID().toString();
        boolean delete = eventTypeEnum == EventTypeEnum.DELETE ? true : false;
        int flat = templateDataSubmitStg(members, tableName, batchNumber, dto.getVersionId(),
                userId, ImportTypeEnum.MANUALLY_ENTER, delete);
        if (flat == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        if(flag){
            //添加工单
            return processService.addProcessApply(dto.getEntityId(),dto.getDescription(),batchNumber,eventTypeEnum);
        }
        ResultEnum resultEnum = dataSynchronizationUtils.stgDataSynchronize(dto.getEntityId(), batchNumber);
        if (resultEnum.getCode() != ResultEnum.DATA_SYNCHRONIZATION_SUCCESS.getCode()) {
            //where条件
            String queryConditions = " and fidata_batch_code ='" + batchNumber + "'";
            IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
            String sql = buildSqlCommand.buildQueryData(tableName, queryConditions);
            List<Map<String, Object>> maps = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
            //返回错误信息
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, maps.get(0).get("fidata_error_msg").toString());
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 获取mdm表code列名称
     *
     * @param entityId
     * @return
     */
    public String getEntityCodeName(Integer entityId) {
        List<AttributeInfoDTO> list = attributeService.listPublishedAttribute(entityId);
        if (CollectionUtils.isEmpty(list)) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        //获取mdm表code数据列表
        Optional<AttributeInfoDTO> codeColumn = list.stream().filter(e -> e.getName().equals(MdmTypeEnum.CODE.getName())).findFirst();
        if (!codeColumn.isPresent()) {
            throw new FkException(ResultEnum.CODE_NOT_EXIST);
        }
        return codeColumn.get().getColumnName();
    }

    /**
     * 导出Excel
     *
     * @param vo
     * @param response
     * @return
     */
    public ResultEnum exportExcel(ExportResultVO vo, HttpServletResponse response) {
        Workbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("sheet1");
        Row  row1 = sheet.createRow(0);
        if (CollectionUtils.isEmpty(vo.getHeaderDisplayList())) {
            ResultEntityBuild.build(ResultEnum.CODE_NOT_EXIST);
        }
        //添加表头
        for (int i = 0; i < vo.getHeaderDisplayList().size(); i++) {
            row1.createCell(i).setCellValue(vo.getHeaderDisplayList().get(i));
        }
        if (!CollectionUtils.isEmpty(vo.getDataArray())) {
            for (int i = 0; i < vo.getDataArray().size(); i++) {
                Row  row =  sheet.createRow(i + 1);
                Map<String, Object> jsonObject = vo.getDataArray().get(i);
                for (int j = 0; j < vo.getHeaderList().size(); j++) {
                    Cell cell =  row.createCell(j);
                    Object value = jsonObject.get(vo.getHeaderList().get(j));
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue()); // 或者使用其他Number类型
                    } else {
                        cell.setCellValue(value == null ? "" : value.toString());
                    }
                }
            }
        }

        // 将文件输出到 ByteArrayOutputStream，计算内容长度
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //将文件存到指定位置
        try {
            // 将Excel内容写入输出流
            workbook.write(byteArrayOutputStream);
            byte[] content = byteArrayOutputStream.toByteArray();

            //输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
//            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.addHeader("Content-Disposition", "attachment;filename="+vo.getFileName()+".xlsx");
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");

            // 设置文件的长度
            response.setContentLength(content.length);
            // 将内容写入响应
            output.write(content);
            output.close();
            byteArrayOutputStream.close();
        } catch (Exception e) {
            log.error("export excel error:", e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        }
        return ResultEnum.SUCCESS;
    }


    public FileDTO exportExcel1(ExportResultVO vo) {
        FileDTO fileDTO = new FileDTO();

        SXSSFWorkbook workbook = new SXSSFWorkbook(1000);
        Sheet sheet = workbook.createSheet("sheet1");
        Row row1 = sheet.createRow(0);

        // 如果表头为空，返回错误
        if (CollectionUtils.isEmpty(vo.getHeaderDisplayList())) {
            return fileDTO;
        }

        // 添加表头
        for (int i = 0; i < vo.getHeaderDisplayList().size(); i++) {
            row1.createCell(i).setCellValue(vo.getHeaderDisplayList().get(i));
        }
        String fileName =  System.currentTimeMillis() + ".xlsx";
        // 临时文件路径和文件名
        String filePath = System.getProperty("user.dir")+ File.separator +"downloadExcel";
        log.info("filePath:{}",filePath);
        File directory = new File(filePath);
        // 判断目录是否存在，不存在则创建目录
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String tmpFilePath = filePath + File.separator + fileName;  // 使用当前时间戳防止文件名冲突
        File tmpFile = new File(tmpFilePath);

        try (FileOutputStream fileOut = new FileOutputStream(tmpFile)) {
            // 批量写入数据，避免一次性加载到内存
            for (int i = 0; i < vo.getDataArray().size(); i++) {
                Map<String, Object> jsonObject = vo.getDataArray().get(i);
                Row row = sheet.createRow(i + 1);

                for (int j = 0; j < vo.getHeaderList().size(); j++) {
                    Cell cell = row.createCell(j);
                    Object value = jsonObject.get(vo.getHeaderList().get(j));

                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value == null ? "" : value.toString());
                    }
                }
            }
            fileDTO.setFileName(vo.getFileName());
            fileDTO.setFilePath(fileName);
            // 最终写入文件
            workbook.write(fileOut);
        } catch (Exception e) {
            log.error("export excel error:", e);
            throw new FkException(ResultEnum.SQL_ANALYSIS);
        } finally {
            try {
                workbook.close();
                workbook.dispose();
            } catch (IOException e) {
                log.error("Error closing workbook", e);
            }
        }

        // 返回文件路径及文件名
        return fileDTO;
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
//            AbstractDbHelper.rollbackConnection(conn);
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
     * 获取表指定列名数据
     *
     * @param tableName
     * @return
     */
    public List<String> getCodeList(String tableName, String codeColumnName, int versionId) {
        List<String> codeList = new ArrayList<>();
        try {
            IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
            //查询code列sql
            String sql = buildSqlCommand.buildQueryOneColumn(tableName, codeColumnName, versionId);
            List<Map<String, Object>> maps = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
            codeList.addAll(maps.stream().map(e -> e.get("columns").toString()).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("getCodeList:", e);
        }
        return codeList;
    }


    /**
     * 获取表指定列名数据
     *
     * @param tableName
     * @return
     */
    public Map<String, String> getCodeAndLockList(String tableName, String codeColumnName, int versionId) {
        Map<String, String> maps = new HashMap<>();
        try {
            IBuildSqlCommand buildSqlCommand = BuildFactoryHelper.getDBCommand(type);
            //查询code列sql
            String sql = buildSqlCommand.buildQueryCodeAndLock(tableName, codeColumnName, versionId);
            List<Map<String, Object>> list = AbstractDbHelper.execQueryResultMaps(sql, getConnection());
            for (Map<String, Object> stringObjectMap : list) {
                maps.put((String)stringObjectMap.get("code"),(String)stringObjectMap.get("lock"));
            }
        } catch (Exception e) {
            log.error("getCodeAndLockList:", e);
        }
        return maps;
    }


}
