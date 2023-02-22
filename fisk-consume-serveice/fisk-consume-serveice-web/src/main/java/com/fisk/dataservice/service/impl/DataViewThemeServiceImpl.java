package com.fisk.dataservice.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.enums.system.SourceBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.utils.CreateSchemaSqlUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.dbBEBuild.AbstractCommonDbHelper;
import com.fisk.datafactory.enums.DelFlagEnum;
import com.fisk.dataservice.dto.dataanalysisview.DataViewAccountDTO;
import com.fisk.dataservice.dto.dataanalysisview.DataViewThemeDTO;
import com.fisk.dataservice.entity.DataViewAccountPO;
import com.fisk.dataservice.entity.DataViewThemePO;
import com.fisk.dataservice.enums.AccountJurisdictionEnum;
import com.fisk.dataservice.map.DataViewMap;
import com.fisk.dataservice.mapper.DataViewAccountMapper;
import com.fisk.dataservice.mapper.DataViewThemeMapper;
import com.fisk.dataservice.service.IDataViewThemeService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private UserHelper userHelper;

    @Resource
    private DataViewThemeMapper baseMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private DataViewAccountMapper dataViewAccountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addViewTheme(DataViewThemeDTO dto) {
        Long userId = userHelper.getLoginUserInfo().getId();

        // 查询视图主题是否已经存在
        QueryWrapper<DataViewThemePO> qw = new QueryWrapper<>();
        qw.eq("theme_name", dto.getThemeName()).eq("del_flag", DelFlagEnum.NORMAL_FLAG.getValue());
        DataViewThemePO prePo = baseMapper.selectOne(qw);
        if (!Objects.isNull(prePo)){
            throw new FkException(ResultEnum.DS_VIEW_THEME_EXISTS);
        }

        // 校验应用简称
        if (!dto.getWhetherSchema()){
            List<String> abbrList = baseMapper.getAbbreviation(DelFlagEnum.NORMAL_FLAG.getValue());
            if (abbrList.contains(dto.getThemeAbbr())){
                throw new FkException(ResultEnum.DS_VIEW_THEME_ABBR_VALID);
            }
        }

        verifyDataSource(dto.getTargetDbId());

        // dto -> po
        DataViewThemePO model = DataViewMap.INSTANCES.dtoToPo(dto);
        model.setCreateUser(userId.toString());
        boolean save = this.save(model);
        if (!save){
            log.info("入库参数，[{}]", JSON.toJSONString(model));
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 是否添加schema
        if (dto.getWhetherSchema()){
            verifySchema(model.getThemeAbbr(), model.getTargetDbId());
        }

        // 处理关联账号
        if (!CollectionUtils.isEmpty(dto.getRelAccountList())){
            Integer viewThemeId = baseMapper.selectViewThemeId(dto.getThemeName(), DelFlagEnum.NORMAL_FLAG.getValue());
            saveRelationAccount(dto.getRelAccountList(), viewThemeId);
        }

        return ResultEnum.SUCCESS;
    }

    private void saveRelationAccount(List<DataViewAccountDTO> dtoList, Integer viewThemeId){
        log.info("视图主题id，[{}, 账号集合，{}]", viewThemeId, JSON.toJSONString(dtoList));
        for (DataViewAccountDTO dto : dtoList){
            if (StringUtils.isEmpty(dto.getAccountName()) || StringUtils.isEmpty(dto.getAccountDesc())){
                throw new FkException(ResultEnum.DS_VIEW_THEME_ACCOUNT_ERROR);
            }
            DataViewAccountPO po = new DataViewAccountPO();
            po.setViewThemeId(viewThemeId);
            po.setAccountName(dto.getAccountName());
            po.setAccountDesc(dto.getAccountDesc());
            po.setJurisdiction(AccountJurisdictionEnum.READ_ONLY.getName());
            int insert = dataViewAccountMapper.insert(po);
            if (insert <= 0){
                throw new FkException(ResultEnum.DS_VIEW_THEME_ACCOUNT_SAVE);
            }
        }
    }

    @Override
    public List<DataSourceDTO> getTargetDbList() {
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

        return targetDbList;
    }

    @Override
    public ResultEnum removeViewTheme(Integer viewThemeId) {
        // 查询数据
        if (viewThemeId <= 0){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        DataViewThemePO model = baseMapper.selectById(viewThemeId);
        if (Objects.isNull(model)){
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        // 删除视图主题
        boolean removeFlag = this.removeById(viewThemeId);
        if (!removeFlag){
            throw new FkException(ResultEnum.DELETE_ERROR);
        }

        //删除视图主题关联账号
        List<Integer> accountIds = dataViewAccountMapper.selectIdListByViewThemeId(viewThemeId);
        if (!CollectionUtils.isEmpty(accountIds)){
            int flag = dataViewAccountMapper.deleteBatchIds(accountIds);
            if (flag <= 0){
                throw new FkException(ResultEnum.DELETE_ERROR);
            }
        }
        return ResultEnum.SUCCESS;
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
        qw.ne("id", dto.getId()).eq("theme_name", dto.getThemeName())
                .eq("del_flag", DelFlagEnum.NORMAL_FLAG.getValue());
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
        qw.eq("del_flag", DelFlagEnum.NORMAL_FLAG.getValue()).orderByDesc("create_time");
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
            aqw.in("view_theme_id", themeIds);
            List<DataViewAccountPO> poList = dataViewAccountMapper.selectList(aqw);
            for (DataViewThemeDTO parent : dtoRecords){
                List<DataViewAccountPO> accList = poList.stream().filter(item -> item.getViewThemeId().equals(parent.getId())).collect(Collectors.toList());
                parent.setRelAccountList(DataViewMap.INSTANCES.accountListPoToDto(accList));
            }
            pageDTO.setItems(dtoRecords);
        }
        return pageDTO;
    }

    private void verifyDataSource(Integer targetDbId){
        try{
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
        }catch (Exception e){
            log.error("数据分析视图调用userClient失败", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
    }

    private void updateRelAccountList(List<DataViewAccountDTO> list){
        for (DataViewAccountDTO dto : list){
            if (StringUtils.isEmpty(dto.getAccountName()) || StringUtils.isEmpty(dto.getAccountDesc())
                    || dto.getViewThemeId() == null || dto.getId() == null){
                throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
            }
            // 查询账号名称是否重复
            QueryWrapper<DataViewAccountPO> qw = new QueryWrapper<>();
            qw.ne("id", dto.getId()).eq("view_theme_id", dto.getViewThemeId())
                    .eq("account_name", dto.getAccountName());
            Integer count = dataViewAccountMapper.selectCount(qw);
            if (count > 0){
                throw new FkException(ResultEnum.DS_VIEW_THEME_ACCOUNT_EXIST);
            }
            DataViewAccountPO po = new DataViewAccountPO();
            po.setId(dto.getId());
            po.setViewThemeId(dto.getViewThemeId());
            po.setAccountName(dto.getAccountName());
            po.setAccountDesc(dto.getAccountDesc());
            po.setJurisdiction(dto.getJurisdiction());
            int flag = dataViewAccountMapper.updateById(po);
            if (flag <= 0){
                throw new FkException(ResultEnum.UPDATE_DATA_ERROR);
            }
        }
    }

    /**
     * 校验schema
     *
     * @param schemaName
     * @param targetDbId
     */
    public void verifySchema(String schemaName, Integer targetDbId) {
        ResultEntity<DataSourceDTO> dataSourceConfig = userClient.getFiDataDataSourceById(targetDbId);
        if (dataSourceConfig.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }
        AbstractCommonDbHelper helper = new AbstractCommonDbHelper();
        Connection connection = helper.connection(dataSourceConfig.data.conStr, dataSourceConfig.data.conAccount, dataSourceConfig.data.conPassword, dataSourceConfig.data.conType);
        CreateSchemaSqlUtils.buildSchemaSql(connection, schemaName, dataSourceConfig.data.conType);
    }
}
