package com.fisk.dataservice.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.sql.Connection;
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
    private UserHelper userHelper;

    @Resource
    private DataViewThemeMapper baseMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private DataViewAccountMapper dataViewAccountMapper;

    /**
     * 新增视图接口
     * @param dto
     * @return
     */
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
            DataSourceDTO dataSourceDTO = targetDbList.stream().filter(item -> item.sourceBusinessTypeValue == dto.getTargetDbId()).findFirst().orElse(null);
            if (Objects.isNull(dataSourceDTO)){
                throw new FkException(ResultEnum.DATASOURCE_INFORMATION_ISNULL);
            }
        }catch (Exception e){
            log.error("数据分析视图调用userClient失败", e);
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }

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
