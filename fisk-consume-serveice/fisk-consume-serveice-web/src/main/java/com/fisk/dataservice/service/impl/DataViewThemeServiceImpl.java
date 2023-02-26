package com.fisk.dataservice.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.CreateSchemaSqlUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.common.service.mdmBEBuild.AbstractDbHelper;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.dataservice.dto.dataanalysisview.DataViewAccountDTO;
import com.fisk.dataservice.dto.dataanalysisview.DataViewThemeDTO;
import com.fisk.dataservice.entity.DataViewAccountPO;
import com.fisk.dataservice.entity.DataViewPO;
import com.fisk.dataservice.entity.DataViewThemePO;
import com.fisk.dataservice.enums.AccountJurisdictionEnum;
import com.fisk.dataservice.map.DataViewMap;
import com.fisk.dataservice.mapper.DataViewAccountMapper;
import com.fisk.dataservice.mapper.DataViewMapper;
import com.fisk.dataservice.mapper.DataViewThemeMapper;
import com.fisk.dataservice.service.IDataViewThemeService;
import com.fisk.dataservice.vo.dataanalysisview.DataSourceVO;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
public class DataViewThemeServiceImpl
        extends ServiceImpl<DataViewThemeMapper, DataViewThemePO>
        implements IDataViewThemeService {

    @Resource
    private DataViewThemeMapper baseMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private DataViewAccountMapper dataViewAccountMapper;

    @Resource
    private DataViewMapper dataViewMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addViewTheme(DataViewThemeDTO dto) {
        log.info("保存数据视图主题参数，[{}]", JSON.toJSONString(dto));
        // 查询视图主题是否已经存在
        QueryWrapper<DataViewThemePO> qw = new QueryWrapper<>();
        qw.lambda().eq(DataViewThemePO::getThemeName, dto.getThemeName())
                .eq(DataViewThemePO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
        DataViewThemePO prePo = baseMapper.selectOne(qw);
        if (!Objects.isNull(prePo)){
            throw new FkException(ResultEnum.DS_VIEW_THEME_EXISTS);
        }

        // 校验数据源
        DataSourceDTO dataSourceDTO = verifyDataSource(dto.getTargetDbId());

        // 校验架构是否已经存在，不包含则创建架构
        boolean schemaFlag = false;
        List<String> abbrList = baseMapper.getAbbreviation(DelFlagEnum.NORMAL_FLAG.getValue());
        if (!abbrList.contains(dto.getThemeAbbr())){
            schemaFlag = true;
        }

        // dto -> po
        DataViewThemePO model = DataViewMap.INSTANCES.dtoToPo(dto);
        boolean save = this.save(model);
        if (!save){
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 创建架构
        if (schemaFlag){
            createSchema(model.getThemeAbbr(), model.getTargetDbId());
        }

        // 处理关联账号
        if (!CollectionUtils.isEmpty(dto.getRelAccountList())){
            Integer viewThemeId = baseMapper.selectViewThemeId(dto.getThemeName(), DelFlagEnum.NORMAL_FLAG.getValue());
            saveRelationAccount(dto.getRelAccountList(), viewThemeId, dataSourceDTO);
        }

        return ResultEnum.SUCCESS;
    }

    private void saveRelationAccount(List<DataViewAccountDTO> dtoList, Integer viewThemeId, DataSourceDTO dataSourceDTO){
        log.info("视图主题id，[{}, 账号集合，{}]", viewThemeId, JSON.toJSONString(dtoList));
        for (DataViewAccountDTO dto : dtoList){
            if (StringUtils.isEmpty(dto.getAccountName()) || StringUtils.isEmpty(dto.getAccountDesc())){
                throw new FkException(ResultEnum.DS_VIEW_THEME_ACCOUNT_ERROR);
            }
            DataViewAccountPO po = new DataViewAccountPO();
            po.setViewThemeId(viewThemeId);
            po.setAccountName(dto.getAccountName());
            po.setAccountDesc(dto.getAccountDesc());
            po.setAccountPsd(dto.getAccountPsd());
            po.setJurisdiction(AccountJurisdictionEnum.READ_ONLY.getName());
            int insert = dataViewAccountMapper.insert(po);
            if (insert <= 0){
                throw new FkException(ResultEnum.DS_VIEW_THEME_ACCOUNT_SAVE);
            }

            // 向数据库中添加账号信息
            String sql = null;
            if (DataSourceTypeEnum.SQLSERVER.getName().equalsIgnoreCase(dataSourceDTO.conType.getName())){
                sql = "CREATE LOGIN " + po.getAccountName() + " with " + " PASSWORD=" + "'" + po.getAccountPsd() + "'";
            }else if (DataSourceTypeEnum.POSTGRESQL.getName().equalsIgnoreCase(dataSourceDTO.conType.getName())){
                sql = "CREATE USER "+ po.getAccountName() + " WITH PASSWORD " + "'" + po.getAccountPsd() + "'";
            }
            execSql(sql, dataSourceDTO);
        }
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

    @Override
    public List<DataSourceVO> getTargetDbList() {
        ResultEntity<List<DataSourceDTO>> result;
        try{
            result = userClient.getAllFiDataDataSource();
            if (result.getCode() != ResultEnum.SUCCESS.getCode()){
                throw new FkException(ResultEnum.DATA_NOTEXISTS);
            }
        }catch (Exception e){
            log.error("数据分析视图调用userClient失败", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }

        List<DataSourceDTO> dsList = result.getData();
        List<DataSourceDTO> targetDbList = dsList.stream().filter(item -> item.sourceBusinessTypeValue == SourceBusinessTypeEnum.DW.getValue()
                || item.sourceBusinessTypeValue == SourceBusinessTypeEnum.ODS.getValue()).collect(Collectors.toList());

        List<DataSourceVO> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(targetDbList)){
            targetDbList.stream().filter(item -> {
                DataSourceVO model = new DataSourceVO();
                model.setId(item.getId());
                model.setName(item.getName());
                list.add(model);
                return false;
            }).collect(Collectors.toList());
        }
        return list;
    }

    @Override
    public ResultEnum removeViewTheme(Integer viewThemeId) {
        // 查询数据
        DataViewThemePO model = baseMapper.selectById(viewThemeId);
        if (Objects.isNull(model)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 删除视图主题
        boolean removeFlag = this.removeById(viewThemeId);
        if (!removeFlag){
            throw new FkException(ResultEnum.DELETE_ERROR);
        }

        // 删除不使用的架构
        QueryWrapper<DataViewThemePO> qw = new QueryWrapper<>();
        qw.lambda().ne(DataViewThemePO::getId, viewThemeId).eq(DataViewThemePO::getThemeAbbr, model.getThemeAbbr())
                .eq(DataViewThemePO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
        Integer schemaCount = baseMapper.selectCount(qw);
        if (schemaCount <= 0){
            removeSchema(model.getThemeAbbr(), model.getTargetDbId());
        }

        // 删除数据视图
        QueryWrapper<DataViewPO> dqw = new QueryWrapper<>();
        dqw.lambda().eq(DataViewPO::getViewThemeId, viewThemeId);
        dataViewMapper.delete(dqw);

        // 删除视图主题关联账号
        QueryWrapper<DataViewAccountPO> accountQw = new QueryWrapper<>();
        accountQw.lambda().eq(DataViewAccountPO::getViewThemeId, viewThemeId).eq(DataViewAccountPO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
        List<DataViewAccountPO> accountList = dataViewAccountMapper.selectList(accountQw);
        if (!CollectionUtils.isEmpty(accountList)){
            int flag = dataViewAccountMapper.deleteBatchIds(accountList.stream().map(DataViewAccountPO::getId).collect(Collectors.toList()));
            if (flag <= 0){
                throw new FkException(ResultEnum.DELETE_ERROR);
            }
        }
        return ResultEnum.SUCCESS;
    }

    private void removeSchema(String themeAbbr, Integer targetDbId){
        try {
            String sql = "DROP SCHEMA IF EXISTS " + themeAbbr;
            log.info("删除架构语句,[{}]", sql);

            DataSourceDTO dataSourceDTO = verifyDataSource(targetDbId);

            AbstractDbHelper abstractDbHelper = new AbstractDbHelper();
            Connection connection = abstractDbHelper.connection(dataSourceDTO.conStr, dataSourceDTO.conAccount,
                    dataSourceDTO.conPassword, com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum.SQLSERVER);
            abstractDbHelper.executeSql(sql, connection);
        } catch (SQLException e) {
            log.error("删除数据库架构失败,", e);
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "目标数据库删除结果失败，或架构不存在");
        }
        log.info("删除架构执行成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum updateViewTheme(DataViewThemeDTO dto) {
        // 查询数据视图主题
        Integer themeId = dto.getId();
        if (themeId == null || themeId <= 0){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        DataViewThemePO model = baseMapper.selectById(themeId);
        if (Objects.isNull(model)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 判断视图主题名称是否重复
        QueryWrapper<DataViewThemePO> qw = new QueryWrapper<>();
        qw.lambda().ne(DataViewThemePO::getId, dto.getId()).eq(DataViewThemePO::getThemeName, dto.getThemeName())
                .eq(DataViewThemePO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue());
        Integer count = baseMapper.selectCount(qw);
        if (count > 0){
            throw new FkException(ResultEnum.DS_VIEW_THEME_NAME_EXIST);
        }

        // 校验数据源
        verifyDataSource(dto.getTargetDbId());

        // 数据转换
        DataViewThemePO po = DataViewMap.INSTANCES.dtoToPo(dto);
        int flag = baseMapper.updateById(po);
        if (flag <= 0){
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
        }

        // todo 修改架构信息

        // 更新账号信息
        List<DataViewAccountDTO> relAccountList = dto.getRelAccountList();
        if (!CollectionUtils.isEmpty(relAccountList)){
            updateRelAccountList(relAccountList);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public PageDTO<DataViewThemeDTO> getViewThemeList(Integer pageNum, Integer pageSize) {
        Page<DataViewThemePO> poPage = new Page<>(pageNum, pageSize);

        // 查询数据
        QueryWrapper<DataViewThemePO> qw = new QueryWrapper<>();
        qw.lambda().eq(DataViewThemePO::getDelFlag, DelFlagEnum.NORMAL_FLAG.getValue())
                .orderByDesc(DataViewThemePO::getCreateTime);
        baseMapper.selectPage(poPage, qw);

        List<DataViewThemePO> records = poPage.getRecords();
        PageDTO<DataViewThemeDTO> pageDTO = new PageDTO<>();
        if (!CollectionUtils.isEmpty(records)){
            pageDTO.setTotal(poPage.getTotal());
            pageDTO.setTotalPage(poPage.getPages());
            List<DataViewThemeDTO> dtoRecords = DataViewMap.INSTANCES.poToDto(records);

            // 查询关联账号
            List<Integer> themeIds = dtoRecords.stream().map(DataViewThemeDTO::getId).collect(Collectors.toList());
            QueryWrapper<DataViewAccountPO> aqw = new QueryWrapper<>();
            aqw.lambda().in(DataViewAccountPO::getViewThemeId, themeIds);
            List<DataViewAccountPO> poList = dataViewAccountMapper.selectList(aqw);
            for (DataViewThemeDTO parent : dtoRecords){
                List<DataViewAccountPO> accList = poList.stream().filter(item -> item.getViewThemeId().equals(parent.getId())).collect(Collectors.toList());
                parent.setRelAccountList(DataViewMap.INSTANCES.accountListPoToDto(accList));
            }
            pageDTO.setItems(dtoRecords);
        }
        return pageDTO;
    }

    @Override
    public DataSourceVO getDataSourceByViewThemeId(Integer viewThemeId) {
        // 查询targetDbId
        DataSourceDTO dataSourceDTO = verifyDataSource(baseMapper.selectDbId(viewThemeId));

        DataSourceVO vo = new DataSourceVO();
        if (!Objects.isNull(dataSourceDTO)){
            vo.setId(dataSourceDTO.id);
            vo.setName(dataSourceDTO.name);
        }
        return vo;
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

    private void updateRelAccountList(List<DataViewAccountDTO> list){
        for (DataViewAccountDTO dto : list){
            if (StringUtils.isEmpty(dto.getAccountName()) || StringUtils.isEmpty(dto.getAccountDesc())
                    || dto.getViewThemeId() == null){
                throw new FkException(ResultEnum.DS_VIEW_THEME_ACCOUNT_ERROR);
            }
            DataViewAccountPO po = new DataViewAccountPO();
            if (dto.getId() != null){
                po.setId(dto.getId());
            }
            po.setViewThemeId(dto.getViewThemeId());
            po.setAccountName(dto.getAccountName());
            po.setAccountDesc(dto.getAccountDesc());
            po.setJurisdiction(AccountJurisdictionEnum.READ_ONLY.getName());
            if (po.getId() == 0){
                log.info("新增");
                // 查询是否存在重复数据
                QueryWrapper<DataViewAccountPO> insertQw = new QueryWrapper<>();
                insertQw.eq("view_theme_id", dto.getViewThemeId()).eq("account_name", dto.getAccountName());
                DataViewAccountPO preModel = dataViewAccountMapper.selectOne(insertQw);
                if (Objects.isNull(preModel)){
                    log.info("不存在当前账号时则添加");
                    int save = dataViewAccountMapper.insert(po);
                    if (save <= 0){
                        throw new FkException(ResultEnum.DA_VIEWTHEME_UPDATE_ACCOUNT_ERROR);
                    }
                }
            }else{
                // 查询修改后的账号名称是否重复
                QueryWrapper<DataViewAccountPO> insertQw = new QueryWrapper<>();
                insertQw.ne("id", po.getId()).eq("view_theme_id", dto.getViewThemeId()).eq("account_name", dto.getAccountName());
                DataViewAccountPO preModel = dataViewAccountMapper.selectOne(insertQw);
                if (Objects.isNull(preModel)){
                    int update = dataViewAccountMapper.updateById(po);
                    if (update <= 0){
                        throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
                    }
                }
            }
        }
    }

    /**
     * 校验schema
     *
     * @param schemaName
     * @param targetDbId
     */
    public void createSchema(String schemaName, Integer targetDbId) {
        ResultEntity<DataSourceDTO> dataSourceConfig = null;
        try{
            dataSourceConfig = userClient.getFiDataDataSourceById(targetDbId);
            log.info("创建架构获取数据源， [{}]", JSON.toJSONString(dataSourceConfig));
            if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
                throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
            }
        }catch (Exception e){
            log.error("数据分析视图服务创建视图调用userClient失败,", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }

        AbstractCommonDbHelper helper = new AbstractCommonDbHelper();
        Connection connection = helper.connection(dataSourceConfig.data.conStr, dataSourceConfig.data.conAccount, dataSourceConfig.data.conPassword, dataSourceConfig.data.conType);
        log.info("已获取数据库连接");
        CreateSchemaSqlUtils.buildSchemaSql(connection, schemaName, dataSourceConfig.data.conType);
    }
}
