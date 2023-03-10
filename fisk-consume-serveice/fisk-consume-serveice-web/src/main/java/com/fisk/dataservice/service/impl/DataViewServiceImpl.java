package com.fisk.dataservice.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.constants.NifiConstants;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.TableNameGenerateUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.framework.redis.RedisKeyBuild;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.accessAndTask.DataTranDTO;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.BuildFactoryAccessHelper;
import com.fisk.common.service.dbBEBuild.factoryaccess.IBuildAccessSqlCommand;
import com.fisk.common.service.dbBEBuild.factoryaccess.dto.DataTypeConversionDTO;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.fisk.dataaccess.enums.SystemVariableTypeEnum;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.dataservice.dto.dataanalysisview.*;
import com.fisk.dataservice.entity.DataViewPO;
import com.fisk.dataservice.entity.DataViewRolePO;
import com.fisk.dataservice.entity.DataViewThemePO;
import com.fisk.dataservice.entity.ViewFieldsPO;
import com.fisk.dataservice.map.DataViewFieldsMap;
import com.fisk.dataservice.map.DataViewMap;
import com.fisk.dataservice.mapper.DataViewFieldsMapper;
import com.fisk.dataservice.mapper.DataViewMapper;
import com.fisk.dataservice.mapper.DataViewRoleMapper;
import com.fisk.dataservice.mapper.DataViewThemeMapper;
import com.fisk.dataservice.service.IDataViewFieldsService;
import com.fisk.dataservice.service.IDataViewService;
import com.fisk.dataservice.util.DbConnectionHelper;
import com.fisk.dataservice.util.PgsqlUtils;
import com.fisk.dataservice.util.SqlServerPlusUtils;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Service
@Slf4j
public class DataViewServiceImpl
        extends ServiceImpl<DataViewMapper, DataViewPO>
        implements IDataViewService {

    @Resource
    private DataViewMapper baseMapper;

    @Resource
    private DataViewThemeMapper dataViewThemeMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    private IDataViewFieldsService dataViewFieldsService;

    @Resource
    private DataViewFieldsMapper dataViewFieldsMapper;

    @Resource
    private DataViewRoleMapper dataViewRoleMapper;

    @Override
    public PageDTO<DataViewDTO> getViewList(Integer viewThemeId, Integer pageNum, Integer pageSize) {
        Page<DataViewPO> poPage = new Page<>(pageNum, pageSize);

        // 查询数据
        QueryWrapper<DataViewPO> qw = new QueryWrapper<>();
        qw.lambda().eq(DataViewPO::getViewThemeId, viewThemeId).eq(DataViewPO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue())
                .orderByDesc(DataViewPO::getCreateTime);
        baseMapper.selectPage(poPage, qw);

        List<DataViewPO> records = poPage.getRecords();
        PageDTO<DataViewDTO> pageDto = new PageDTO<>();
        pageDto.setTotal(poPage.getTotal());
        pageDto.setTotalPage(poPage.getPages());
        log.info(JSON.toJSONString(records));
        pageDto.setItems(DataViewMap.INSTANCES.dataViewPoToDto(records));
        return pageDto;
    }

    @Override
    public DataSourceViewDTO getDataSourceMeta(Integer viewThemeId) {
        // 校验数据源
        DataSourceDTO dataSourceDTO = verifyDataSource(dataViewThemeMapper.selectDbId(viewThemeId));
        if (Objects.isNull(dataSourceDTO)){
            throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
        }

        // 查询缓存里有没有redis的数据 TODO 该处测试语句需要删除
        redisUtil.del(RedisKeyBuild.buildViewThemeDataSourceKey(viewThemeId));
        boolean flag = redisUtil.hasKey(RedisKeyBuild.buildViewThemeDataSourceKey(viewThemeId));
        DataSourceViewDTO dataSourceViewDTO = null;
        if (!flag) {
            //将表和视图的结构存入redis
            setDataSourceMeta(viewThemeId, dataSourceDTO);
        }
        try{
            String datasourceMetaJson = redisUtil.get(RedisKeyBuild.buildViewThemeDataSourceKey(viewThemeId)).toString();
            if (!StringUtils.isEmpty(datasourceMetaJson)){
                dataSourceViewDTO = JSON.parseObject(datasourceMetaJson, DataSourceViewDTO.class);
            }
        }catch (Exception e){
            log.error("数据视图主题获取redis中数据表结构失败,", e);
        }
        return dataSourceViewDTO;
    }

    private DataSourceDTO verifyDataSource(Integer targetDbId){
        try{
            log.info("开始校验数据源信息");
            // 查询数据源是否存在
            ResultEntity<List<DataSourceDTO>> dsResult = userClient.getAllFiDataDataSource();
            if (dsResult.getCode() != ResultEnum.SUCCESS.getCode() || CollectionUtils.isEmpty(dsResult.data)){
                throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
            }
            // 过滤ods和dw数据源
            List<DataSourceDTO> dsList = dsResult.getData();
            List<DataSourceDTO> targetDbList = dsList.stream().filter(item ->
                    item.sourceBusinessTypeValue == SourceBusinessTypeEnum.DW.getValue()
                            || item.sourceBusinessTypeValue == SourceBusinessTypeEnum.ODS.getValue()
            ).collect(Collectors.toList());
            log.info("目标数据源集合,[{}]", JSON.toJSONString(targetDbList));
            DataSourceDTO dataSourceDTO = targetDbList.stream().filter(item -> item.sourceBusinessTypeValue == targetDbId).findFirst().orElse(null);
            if (Objects.isNull(dataSourceDTO)){
                throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
            }
            log.info("结束校验数据源信息");
            return dataSourceDTO;
        }catch (Exception e){
            log.error("数据分析视图调用userClient失败", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
    }

    @Override
    public List<TableStructureDTO> getSourceColumnMeta(Integer viewThemeId, String tableName, Integer queryType) {
        if (viewThemeId == 0 || StringUtils.isEmpty(tableName)){
            throw new FkException(ResultEnum.DA_VIEWTHEMEID_TABLENAME_ERROR);
        }
        // 查询数据源
        DataSourceDTO dsDto = verifyDataSource(dataViewThemeMapper.selectDbId(viewThemeId));

        if (DataSourceTypeEnum.SQLSERVER.getName().equalsIgnoreCase(dsDto.conType.toString())) {
            SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
            Connection conn = DbConnectionHelper.connection(dsDto.conStr, dsDto.conAccount,
                    dsDto.conPassword, DataSourceTypeEnum.SQLSERVER);
            return queryType == 1 ? sqlServerPlusUtils.getViewField(conn, tableName) : null;
        }else if (DataSourceTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(dsDto.conType.toString())){
            PgsqlUtils pgsqlUtils = new PgsqlUtils();
            Connection conn = DbConnectionHelper.connection(dsDto.conStr, dsDto.conAccount,
                    dsDto.conPassword, DataSourceTypeEnum.POSTGRESQL);
            return queryType == 1 ? pgsqlUtils.getTableColumnName(conn, tableName) : null;
        }
        return null;
    }

    @Override
    public OdsResultDTO getDataAccessQueryList(SelSqlResultDTO dto) {

        // 获取数据源
        // 查询targetDbId
        Integer targetDbId = dto.getTargetDbId();
        if (Objects.isNull(targetDbId)){
            throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
        }
        ResultEntity<List<DataSourceDTO>> result;
        try{
            result = userClient.getAllFiDataDataSource();
            if (result.getCode() != ResultEnum.SUCCESS.getCode()){
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
        }catch (Exception e){
            log.error("数据分析视图执行sql调用userClient失败", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }

        List<DataSourceDTO> dsList = result.getData();
        DataSourceDTO dsDto = dsList.stream().filter(item -> item.id.equals(targetDbId)).findFirst().orElse(null);
        log.info("数据主题视图获取执行sql结果信息的数据源，[{}]", JSON.toJSONString(dsDto));
        if (dsDto == null) {
            log.error(dto.viewThemeId + ":" + JSON.toJSONString(ResultEnum.DATASOURCE_INFORMATION_ISNULL));
            return null;
        }

        // 查询视图主题
        DataViewThemePO theme = dataViewThemeMapper.selectById(dto.viewThemeId);
        if (theme == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        OdsResultDTO array = new OdsResultDTO();
        Instant inst1 = Instant.now();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        DataSourceTypeEnum dataSourceTypeEnum = DataSourceTypeEnum.getEnum(dsDto.conType.getName().toUpperCase());
        try {
            AbstractCommonDbHelper helper = new AbstractCommonDbHelper();
            conn = helper.connection(dsDto.conStr, dsDto.conAccount, dsDto.conPassword, dataSourceTypeEnum);
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setMaxRows(10);

            assert st != null;
            Instant inst2 = Instant.now();
            log.info("流式设置执行时间 : " + Duration.between(inst1, inst2).toMillis());
            Instant inst3 = Instant.now();
            String tableName = TableNameGenerateUtils.buildTableName(dto.viewName, theme.getThemeAbbr(), theme.getWhetherSchema());
//            log.info("时间增量值:{}", JSON.toJSONString(query.deltaTimes));
            // 传参改动
            DataTranDTO dataTrandto = new DataTranDTO();
            dataTrandto.tableName = tableName;
            dataTrandto.querySql = dto.querySql;
            dataTrandto.driveType = dsDto.conType.getName();
//            dataTrandto.deltaTimes = JSON.toJSONString(query.deltaTimes);
            Map<String, String> converSql = publishTaskClient.converSql(dataTrandto).data;
            log.info("拼语句执行时间 : " + Duration.between(inst2, inst3).toMillis());

            String sql = converSql.get(SystemVariableTypeEnum.QUERY_SQL.getValue());
            rs = st.executeQuery(sql);
            Instant inst4 = Instant.now();
            log.info("执行sql时间 : " + Duration.between(inst3, inst4).toMillis());
            //获取数据集
            array = resultSetToJsonArrayDataAccess(rs);

            Instant inst5 = Instant.now();
            log.info("封装数据执行时间 : " + Duration.between(inst4, inst5).toMillis());

            array.sql = sql;
        } catch (Exception e) {
            log.error("数据接入执行自定义sql失败,ex:{}", e);
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR, e.getMessage());
        } finally {
            AbstractCommonDbHelper.closeResultSet(rs);
            AbstractCommonDbHelper.closeStatement(st);
            AbstractCommonDbHelper.closeConnection(conn);
        }
        Instant inst5 = Instant.now();
        System.out.println("最终执行时间 : " + Duration.between(inst1, inst5).toMillis());

        //数据类型转换
        typeConversion(dataSourceTypeEnum, array.fieldNameDTOList, dto.targetDbId);

        return array;
    }

    @Override
    public OdsResultDTO getPreviewData(Integer viewThemeId, String tableName) {
        if (viewThemeId == 0 || StringUtils.isEmpty(tableName)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        DataViewThemePO dataViewThemePO = dataViewThemeMapper.selectById(viewThemeId);
        if (dataViewThemePO == null){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        DataSourceDTO dataSourceDTO = verifyDataSource(dataViewThemePO.getTargetDbId());
        SelSqlResultDTO dto = new SelSqlResultDTO();
        dto.setViewThemeId(viewThemeId);
        dto.setViewName(tableName);
        dto.setQuerySql("select * from " + tableName);
        dto.setPageNum(1);
        dto.setPageSize(100);
        dto.setDataSourceTypeEnum(dataSourceDTO.conType.getName());
        dto.setTargetDbId(dataSourceDTO.getId());
        return this.getDataAccessQueryList(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addDataView(SaveDataViewDTO dto) {
        log.info("保存数据视图参数,[{}]", JSON.toJSONString(dto));
        // 查询当前视图主题下视图是否重复
        QueryWrapper<DataViewPO> qw = new QueryWrapper<>();
        qw.lambda().eq(DataViewPO::getViewThemeId, dto.getViewThemeId())
                .eq(DataViewPO::getName, dto.getName())
                .eq(DataViewPO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
        DataViewPO dataViewPO = baseMapper.selectOne(qw);
        if (!Objects.isNull(dataViewPO)){
            throw new FkException(ResultEnum.DS_DATA_VIEW_EXIST);
        }

        // 未创建架构是否存在重复
        String themeName = baseMapper.selectAbbrName(dto.getTargetDbId(), dto.getName());
        if (!StringUtils.isEmpty(themeName)){
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "视图主题：" + themeName + " 下已存在该视图名称");
        }

        // 校验数据源是否合法
        DataSourceDTO dsDto = verifyDataSource(dto.getTargetDbId());

        // 存储数据视图
        DataViewPO model = new DataViewPO();
        model.setViewThemeId(dto.getViewThemeId());
        model.setName(dto.getName());
        model.setDisplayName(dto.getDisplayName());
        model.setViewScript(dto.getViewScript());
        model.setViewDesc(dto.getViewDesc());
        int insert = baseMapper.insert(model);
        if (insert <= 0){
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        // 向目标数据库中创建视图
        createView(model, dsDto);

        // 查询主键
        QueryWrapper<DataViewPO> qw2 = new QueryWrapper<>();
        qw2.lambda().eq(DataViewPO::getViewThemeId, dto.getViewThemeId()).eq(DataViewPO::getName, dto.getName()).eq(DataViewPO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
        DataViewPO po = baseMapper.selectOne(qw2);

        // 存储字段信息
        dataViewFieldsService.saveViewFields(dto, (int)po.getId(), dsDto);

        // 为关联的主题角色授权视图权限
        relationGrant(model, dsDto);

        return ResultEnum.SUCCESS;
    }

    private void relationGrant(DataViewPO model, DataSourceDTO dataSourceDTO) {
        try {
            // 查询架构
            DataViewThemePO dataViewThemePO = dataViewThemeMapper.selectById(model.getViewThemeId());
            // 查询角色信息
            QueryWrapper<DataViewRolePO> qw = new QueryWrapper<>();
            qw.lambda().eq(DataViewRolePO::getThemeId, model.getViewThemeId());
            DataViewRolePO rolePo = dataViewRoleMapper.selectOne(qw);
            if (!dataViewThemePO.getWhetherSchema()){
                dataViewThemePO.setThemeAbbr("dbo");
                if (dataSourceDTO.conType.getName().contains(DataSourceTypeEnum.POSTGRESQL.getName())){
                    dataViewThemePO.setThemeAbbr("public");
                }
            }
            String viewName = dataViewThemePO.getThemeAbbr() + "." + model.getName();
            String sql = "grant select on " + viewName + " to " + rolePo.getRoleName();
            if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.POSTGRESQL.getName())){
                sql = "grant select on table " + viewName + " to " + rolePo.getRoleName();
            }

            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = null;
            if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.SQLSERVER.getName())){
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.SQLSERVER);
            }else if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.POSTGRESQL.getName())){
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.PG);
            }
            assert connection != null;
            abstractDbHelper.executeSql(sql, connection);
            log.info("数据库角色关联视图sql执行结束,[{}]", sql);
        } catch (SQLException e) {
            log.error("数据分析视图目标数据库执行sql失败,", e);
        }
    }

    @Override
    public ResultEnum removeDataView(Integer viewId) {
        // 查询数据视图是否存在
        DataViewPO model = baseMapper.selectById(viewId);
        if (Objects.isNull(model)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 校验数据源是否合法
        DataSourceDTO dsDto = verifyDataSource(dataViewThemeMapper.selectDbId(model.getViewThemeId()));

        // 删除数据视图
        log.info("数据视图信息，[{}]", JSON.toJSONString(model));
        int del = baseMapper.deleteById(viewId);
        if (del <= 0){
            throw new FkException(ResultEnum.DELETE_ERROR);
        }

        // 删除数据库视图
        removeView(model, dsDto);
        return ResultEnum.SUCCESS;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editDataView(EditDataViewDTO dto) {
        List<DataViewPO> allData = baseMapper.selectList(new QueryWrapper<>());
        if (CollectionUtils.isEmpty(allData)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        // 获取当前视图
        DataViewPO preModel = allData.stream().filter(item -> item.getId() == dto.getViewId()).findFirst().orElse(null);
        log.info("数据视图，[{}]", JSON.toJSONString(preModel));
        if (Objects.isNull(preModel)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        List<String> nameList = allData.stream().filter(item -> item.getViewThemeId().equals(preModel.getViewThemeId())
                && item.getId() != preModel.getId()).map(DataViewPO::getName).collect(Collectors.toList());
        if (nameList.contains(dto.getName())){
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "当前视图主题下已存在该视图名称");
        }
        List<String> displayNameList = allData.stream().filter(item -> item.getViewThemeId().equals(preModel.getViewThemeId())
                && item.getId() != preModel.getId()).map(DataViewPO::getDisplayName).collect(Collectors.toList());
        if (displayNameList.contains(dto.getDisplayName())){
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "当前视图主题下已存在该视图显示名称");
        }

        // 校验数据源
        DataViewThemePO dataViewThemePO = dataViewThemeMapper.selectById(preModel.getViewThemeId());
        DataSourceDTO dataSourceDTO = checkDataSource(dataViewThemePO.getTargetDbId());

        // 未创建架构是否存在重复
        String themeName = baseMapper.selectAbbrName(dataSourceDTO.id, dto.getName());
        if (!StringUtils.isEmpty(themeName)){
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "视图主题：" + themeName + " 下已存在该视图名称");
        }

        // 修改数据库中的数据视图
        if (!dto.getName().equals(preModel.getName())){
            updateView(dto.getName(), preModel, dataSourceDTO);
        }

        // 修改数据记录
        preModel.setName(dto.getName());
        preModel.setDisplayName(dto.getDisplayName());
        preModel.setViewDesc(dto.getViewDesc());
        if (baseMapper.updateById(preModel) <= 0){
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
        }

        // 修改角色权限
        relationGrant(preModel, dataSourceDTO);
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<DataViewFieldsDTO> getViewTableFields(Integer viewId) {
        QueryWrapper<ViewFieldsPO> qw = new QueryWrapper<>();
        qw.eq("view_id", viewId).eq("del_flag", DelFlagEnum.NORMAL_FLAG.getValue());
        return DataViewFieldsMap.INSTANCES.PoToDtoList(dataViewFieldsMapper.selectList(qw));
    }

    @Override
    public ResultEnum updateDataView(UpdateDataViewDTO dto) {
        log.info("修改数据视图参数,[{}]", JSON.toJSONString(dto));
        DataViewPO model = baseMapper.selectById(dto.getViewId());
        if (Objects.isNull(model)){
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
        }

        model.setViewScript(dto.getViewScript());
        if (baseMapper.updateById(model) <= 0){
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
        }

        // 修改数据库视图
        DataSourceDTO dataSourceDTO = checkDataSource(dto.getTargetDbId());
        removeView(model, dataSourceDTO);
        createView(model, dataSourceDTO);

        // 更新字段信息
        // 存储字段信息
        dataViewFieldsService.updateViewFields(model, dto.getViewThemeId(), dataSourceDTO);
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addBatchDataView(SaveBatchDataViewDTO dto) {
        if (CollectionUtils.isEmpty(dto.getTableNameList())){
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        // 处理数据源
        DataViewThemePO dataViewThemePO = dataViewThemeMapper.selectById(dto.getViewThemeId());
        DataSourceDTO dataSourceDTO = verifyDataSource(dataViewThemePO.getTargetDbId());

        // 存储数据视图
        List<String> tableNameList = dto.getTableNameList();
        for (String tableName : tableNameList){
            // 查询是否存在
            QueryWrapper<DataViewPO> qw = new QueryWrapper<>();
            qw.lambda().eq(DataViewPO::getViewThemeId, dto.getViewThemeId()).eq(DataViewPO::getName, tableName)
                    .eq(DataViewPO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
            DataViewPO dataViewPO = baseMapper.selectOne(qw);
            DataViewPO model = new DataViewPO();
            // 不存在当前视图则创建
            model.setViewThemeId(dto.getViewThemeId());
            String viewName = tableName;
            if (tableName.contains(".")){
                viewName = tableName.split("\\.")[1];
            }
            // 判断是否无架构
            if (!dataViewThemePO.getWhetherSchema()){
                viewName = "view_" + viewName;
            }
            model.setName(viewName);
            model.setDisplayName(viewName);
            String sql = "select * from " + tableName;
            model.setViewScript(sql);
            model.setViewDesc("");
            DataViewPO viewPo = baseMapper.selectId(dto.getViewThemeId(), viewName, DelFlagEnum.NORMAL_FLAG.getValue());
            if (viewPo != null){
                baseMapper.deleteById(viewPo.getId());
            }
            if (Objects.isNull(dataViewPO)){
                int insert = baseMapper.insert(model);
                if (insert <= 0){
                    throw new FkException(ResultEnum.SAVE_DATA_ERROR);
                }
            }
            model.setName(tableName);
            model.setDisplayName(tableName);

            // 向目标数据库中创建视图
            batchCreateView(model, dataSourceDTO, dataViewThemePO);

            // 为关联的主题角色授权视图权限
            model.setName(viewName);
            relationGrant(model, dataSourceDTO);

            // 查询主键
            DataViewPO po = baseMapper.selectId(dto.getViewThemeId(), viewName, DelFlagEnum.NORMAL_FLAG.getValue());

            // 存储字段信息
            dataViewFieldsService.saveBatchViewFields(model, (int) po.getId(), dataSourceDTO);
        }
        return ResultEnum.SUCCESS;
    }

    private void batchCreateView(DataViewPO model, DataSourceDTO dataSourceDTO, DataViewThemePO dataViewThemePO){
        // 删除历史视图
        removeBatchView(model, dataSourceDTO);

        // 创建新视图
        String viewName = model.getName();
        if (model.getName().contains(".")){
            viewName = model.getName().split("\\.")[1];
        }
        // 判断是否无架构
        if (!dataViewThemePO.getWhetherSchema()){
            viewName = "view_" + viewName;
        }
        if (!dataViewThemePO.getWhetherSchema()){
            dataViewThemePO.setThemeAbbr("dbo");
            if (dataSourceDTO.conType.getName().contains(DataSourceTypeEnum.POSTGRESQL.getName())){
                dataViewThemePO.setThemeAbbr("public");
            }
        }
        String createViewSql = "create view " + dataViewThemePO.getThemeAbbr() + "." + viewName + " as " + model.getViewScript();
        execSql(createViewSql, dataSourceDTO);
    }

    private DataSourceDTO checkDataSource(Integer targetDbId){
        // 校验数据源是否合法
        ResultEntity<List<DataSourceDTO>> result;
        try{
            result = userClient.getAllFiDataDataSource();
            if (result.getCode() != ResultEnum.SUCCESS.getCode()){
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
        }catch (Exception e){
            log.error("数据分析视图修改视图调用userClient失败", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }

        List<DataSourceDTO> dsList = result.getData();
        DataSourceDTO dsDto = dsList.stream().filter(item -> item.id.equals(targetDbId)).findFirst().orElse(null);
        log.info("数据主题视图图修改视图的数据源，[{}]", JSON.toJSONString(dsDto));
        if (dsDto == null) {
            throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
        }
        return dsDto;
    }

    private void updateView(String name, DataViewPO model, DataSourceDTO dataSourceDto){
        // 获取架构名称
        DataViewThemePO dataViewThemePO = dataViewThemeMapper.selectById(model.getViewThemeId());
        if (getView(name, dataViewThemePO, dataSourceDto)){
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "目标库中已存在该数据视图");
        }
        // 创建新视图
        DataViewPO view = new DataViewPO();
        view.setId(model.getId());
        view.setName(name);
        view.setViewScript(model.getViewScript());
        createView(view, dataSourceDto);

        // 删除旧视图
        removeView(model, dataSourceDto);
    }

    private boolean getView(String name, DataViewThemePO dataViewThemePO, DataSourceDTO dataSourceDTO){
        if (!dataViewThemePO.getWhetherSchema()){
            dataViewThemePO.setThemeAbbr("dbo");
            if (dataSourceDTO.conType.getName().contains(DataSourceTypeEnum.POSTGRESQL.getName())){
                dataViewThemePO.setThemeAbbr("public");
            }
        }
        String getViewSql = "SELECT * FROM " + dataViewThemePO.getThemeAbbr() + "." +  name;
        log.info("查询视图语句,[{}]", getViewSql);

        AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
        Connection connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.SQLSERVER);
        try {
            log.info("开始查询视图");
            abstractDbHelper.executeSql(getViewSql, connection);
            log.info("查询视图成功");
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    private void removeBatchView(DataViewPO model, DataSourceDTO dataSourceDTO){
        // 获取视图主题
        DataViewThemePO dataViewThemePO = dataViewThemeMapper.selectById(model.getViewThemeId());
        String viewName = model.getName();
        if (model.getName().contains(".")){
            viewName = model.getName().split("\\.")[1];
        }
        // 判断是否无架构
        if (!dataViewThemePO.getWhetherSchema()){
            viewName = "view_" + viewName;
        }
        if (!dataViewThemePO.getWhetherSchema()){
            dataViewThemePO.setThemeAbbr("dbo");
            if (dataSourceDTO.conType.getName().contains(DataSourceTypeEnum.POSTGRESQL.getName())){
                dataViewThemePO.setThemeAbbr("public");
            }
        }
        String removeViewSql = "DROP VIEW IF EXISTS " + dataViewThemePO.getThemeAbbr() + "." + viewName;
        execSql(removeViewSql, dataSourceDTO);
        log.info("删除视图成功");
    }

    private void removeView(DataViewPO model, DataSourceDTO dataSourceDTO){
        // 获取视图主题
        DataViewThemePO dataViewThemePO = dataViewThemeMapper.selectById(model.getViewThemeId());
        if (!dataViewThemePO.getWhetherSchema()){
            dataViewThemePO.setThemeAbbr("dbo");
            if (dataSourceDTO.conType.getName().contains(DataSourceTypeEnum.POSTGRESQL.getName())){
                dataViewThemePO.setThemeAbbr("public");
            }
        }
        String removeViewSql = "DROP VIEW IF EXISTS " + dataViewThemePO.getThemeAbbr() + "." + model.getName();
        execSql(removeViewSql, dataSourceDTO);
        log.info("删除视图成功");
    }

    private DataViewThemePO getViewThemeInfo(long viewId){
        String themeId = baseMapper.selectThemeId(viewId);
        return dataViewThemeMapper.selectById(themeId);
    }

    private void createView(DataViewPO model, DataSourceDTO dataSourceDTO){
        // 查询数据视图主题架构简称
        DataViewThemePO dataViewThemePO = getViewThemeInfo(model.getId());
        if (!dataViewThemePO.getWhetherSchema()){
            dataViewThemePO.setThemeAbbr("dbo");
            if (dataSourceDTO.conType.getName().contains(DataSourceTypeEnum.POSTGRESQL.getName())){
                dataViewThemePO.setThemeAbbr("public");
            }
        }

        String createViewSql = "create view " + dataViewThemePO.getThemeAbbr() + "." + model.getName() + " as " + model.getViewScript();
        execSql(createViewSql, dataSourceDTO);
        log.info("创建视图成功");
    }

    private void execSql(String sql, DataSourceDTO dataSourceDTO){
        log.info("sql执行语句,[{}]", sql);
        try {
            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = null;
            if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.SQLSERVER.getName())){
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.SQLSERVER);
            }else if (dataSourceDTO.conType.getName().equalsIgnoreCase(DataSourceTypeEnum.POSTGRESQL.getName())){
                connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                        dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.PG);
            }
            assert connection != null;
            abstractDbHelper.executeSql(sql, connection);
        } catch (SQLException e) {
            log.error("数据分析视图目标数据库执行sql失败,", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, e.getMessage());
        }
    }

    public void typeConversion(DataSourceTypeEnum dataSourceTypeEnum, List<FieldNameDTO> fieldList, Integer targetDbId) {

        //目标数据源
        ResultEntity<DataSourceDTO> targetDataSource = userClient.getFiDataDataSourceById(targetDbId);
        if (targetDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_OPS_CONFIG_EXISTS);
        }

        IBuildAccessSqlCommand command = BuildFactoryAccessHelper.getDBCommand(dataSourceTypeEnum);
        DataTypeConversionDTO dto = new DataTypeConversionDTO();
        for (FieldNameDTO field : fieldList) {
            dto.dataLength = field.fieldLength;
            dto.dataType = field.fieldType;
            dto.precision = field.sourceFieldPrecision;
            String[] data = command.dataTypeConversion(dto, targetDataSource.data.conType);
            field.fieldType = data[0].toUpperCase();
            field.fieldLength = data[1];
        }

    }

    public static OdsResultDTO resultSetToJsonArrayDataAccess(ResultSet rs) throws SQLException, JSONException {
        OdsResultDTO data = new OdsResultDTO();
        // json数组
        JSONArray array = new JSONArray();
        // 获取列数
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<FieldNameDTO> fieldNameDTOList = new ArrayList<>();
        // 遍历ResultSet中的每条数据
        int count = 1;
        // 预览展示10行
        int row = 10;
        while (rs.next() && count <= row) {
            JSONObject jsonObj = new JSONObject();
            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                //过滤ods表中pk和code默认字段
                String tableName = metaData.getTableName(i) + "key";
                if (NifiConstants.AttrConstants.FIDATA_BATCH_CODE.equals(columnName) || tableName.equals("ods_" + columnName)) {
                    continue;
                }
                //获取sql查询数据集合
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            count++;
            array.add(jsonObj);
        }
        //获取列名
        for (int i = 1; i <= columnCount; i++) {
            FieldNameDTO dto = new FieldNameDTO();
            dto.sourceTableName = metaData.getTableName(i);
            dto.sourceFieldName = metaData.getColumnLabel(i);
            dto.sourceFieldType = metaData.getColumnTypeName(i).toUpperCase();
            dto.sourceFieldPrecision = metaData.getScale(i);
            dto.fieldName = metaData.getColumnLabel(i);
            String tableName = metaData.getTableName(i) + "key";
            if (NifiConstants.AttrConstants.FIDATA_BATCH_CODE.equals(dto.fieldName)
                    || tableName.equals("ods_" + dto.fieldName)) {
                continue;
            }
            dto.fieldType = metaData.getColumnTypeName(i).toLowerCase();
            dto.fieldLength = "2147483647".equals(String.valueOf(metaData.getColumnDisplaySize(i))) ? "255" : String.valueOf(metaData.getColumnDisplaySize(i));
            fieldNameDTOList.add(dto);
        }
        data.fieldNameDTOList = fieldNameDTOList.stream().collect(Collectors.toList());
        data.dataArray = array;
        return data;
    }

    private void setDataSourceMeta(Integer viewThemeId, DataSourceDTO dsDTO){
        log.info("数据视图主题开始设置数据表信息");
        try {
            DataSourceViewDTO dto = new DataSourceViewDTO();

            if (dsDTO == null) {
                log.error(viewThemeId + ":" + JSON.toJSONString(ResultEnum.DATASOURCE_INFORMATION_ISNULL));
                return;
            }
            dto.appName = dsDTO.conDbname;
            dto.driveType = dsDTO.conType.toString();
            if (DataSourceTypeEnum.SQLSERVER.getName().equalsIgnoreCase(dsDTO.conType.toString())) {
                // sqlserver类型
                SqlServerPlusUtils sqlServerPlusUtils = new SqlServerPlusUtils();
                // 表结构
                dto.tableDtoList = sqlServerPlusUtils.getTableNameAndColumnsPlus(DbConnectionHelper.connection(dsDTO.conStr, dsDTO.conAccount,
                        dsDTO.conPassword, DataSourceTypeEnum.SQLSERVER), dsDTO.conDbname);
                // 视图结构
                /*
                dataSource.viewDtoList = sqlServerPlusUtils.loadViewDetails(DbConnectionHelper.connection(po.connectStr, po.connectAccount,
                        po.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER));
                */
            }else if (DataSourceTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(dsDTO.conType.toString())) {
                PgsqlUtils pgsqlUtils = new PgsqlUtils();
                // 表结构
                dto.tableDtoList = pgsqlUtils.getTableNameAndColumnsPlusView(DbConnectionHelper.connection(dsDTO.conStr, dsDTO.conAccount,
                        dsDTO.conPassword, DataSourceTypeEnum.POSTGRESQL));
                //视图结构
                //dataSource.viewDtoList = new ArrayList<>();
            }

            if (CollectionUtils.isNotEmpty(dto.tableDtoList)) {
                redisUtil.set(RedisKeyBuild.buildViewThemeDataSourceKey(viewThemeId), JSON.toJSONString(dto));
                log.info("数据视图主题设置数据结束，[{}]", JSON.toJSONString(dto));
            }
        } catch (Exception e) {
            log.error(viewThemeId + ":" + JSON.toJSONString(ResultEnum.DATASOURCE_INFORMATION_ISNULL));
        }
    }
}
