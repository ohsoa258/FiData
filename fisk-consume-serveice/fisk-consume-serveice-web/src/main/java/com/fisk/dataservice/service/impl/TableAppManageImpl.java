package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataservice.dto.GetConfigDTO;
import com.fisk.dataservice.dto.tableservice.TableAppDTO;
import com.fisk.dataservice.dto.tableservice.TableAppDatasourceDTO;
import com.fisk.dataservice.dto.tableservice.TableAppPageDTO;
import com.fisk.dataservice.dto.tableservice.TableAppQueryDTO;
import com.fisk.dataservice.entity.AppServiceConfigPO;
import com.fisk.dataservice.entity.TableAppDatasourcePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.entity.TableServicePO;
import com.fisk.dataservice.enums.ApiStateTypeEnum;
import com.fisk.dataservice.map.TableAppDatasourceMap;
import com.fisk.dataservice.map.TableAppMap;
import com.fisk.dataservice.mapper.AppServiceConfigMapper;
import com.fisk.dataservice.mapper.TableAppDatasourceMapper;
import com.fisk.dataservice.mapper.TableAppMapper;
import com.fisk.dataservice.mapper.TableServiceMapper;
import com.fisk.dataservice.service.ITableAppManageService;
import com.fisk.dataservice.vo.tableservice.TableAppDatasourceVO;
import com.fisk.dataservice.vo.tableservice.TableAppVO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.task.BuildDeleteTableServiceDTO;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TableAppManageImpl
        extends ServiceImpl<TableAppMapper, TableAppPO>
        implements ITableAppManageService {

    @Resource
    private UserHelper userHelper;

    @Resource
    private GetConfigDTO getConfig;

    @Resource
    private GetMetadata getMetadata;

    @Resource
    private GenerateCondition generateCondition;

    @Resource
    private TableAppDatasourceMapper tableAppDatasourceMapper;

    @Resource
    private TableAppDatasourceManageImpl tableAppDatasourceManage;

    @Resource
    private AppServiceConfigMapper appServiceConfigMapper;

    @Resource
    private TableServiceMapper tableServiceMapper;

    @Resource
    private PublishTaskClient publishTaskClient;

    @Override
    public List<FilterFieldDTO> getFilterColumn() {
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.driver = getConfig.driver;
        dto.tableName = "tb_table_app";
        dto.filterSql = FilterSqlConstants.TABLE_APP_REGISTRATION_SQL;
        return getMetadata.getMetadataList(dto);
    }

    @Override
    public Page<TableAppVO> pageFilter(TableAppQueryDTO query) {
        StringBuilder querySql = new StringBuilder();
        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(query.dto));
        TableAppPageDTO data = new TableAppPageDTO();
        data.page = query.page;
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();

        Page<TableAppVO> filter = baseMapper.filter(query.page, data);
        if (filter != null && CollectionUtils.isNotEmpty(filter.getRecords())) {
            List<Integer> tableAppId = filter.getRecords().stream().map(t -> t.getId()).collect(Collectors.toList());
            QueryWrapper<TableAppDatasourcePO> tableAppDatasourcePOQueryWrapper = new QueryWrapper<>();
            tableAppDatasourcePOQueryWrapper.lambda().
                    eq(TableAppDatasourcePO::getDelFlag, 1)
                    .in(TableAppDatasourcePO::getTableAppId, tableAppId);
            List<TableAppDatasourcePO> tableAppDatasourcePOS = tableAppDatasourceMapper.selectList(tableAppDatasourcePOQueryWrapper);
            filter.getRecords().forEach(t -> {
                if (CollectionUtils.isNotEmpty(tableAppDatasourcePOS)) {
                    List<TableAppDatasourcePO> tableAppDatasourcePOList = tableAppDatasourcePOS.stream().filter(k -> k.getTableAppId() == t.getId()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(tableAppDatasourcePOList)) {
                        List<TableAppDatasourceVO> tableAppDatasourceVOS = TableAppDatasourceMap.INSTANCES.listPoToVo(tableAppDatasourcePOList);
                        t.setTableAppDatasourceVOS(tableAppDatasourceVOS);
                    }
                }
            });
        }
        return filter;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(TableAppDTO dto) {
        try {
            if (dto == null || org.apache.commons.lang.StringUtils.isEmpty(dto.getAppName())) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            QueryWrapper<TableAppPO> tableAppPOQueryWrapper = new QueryWrapper<>();
            tableAppPOQueryWrapper.lambda()
                    .eq(TableAppPO::getDelFlag, 1)
                    .eq(TableAppPO::getAppName, dto.getAppName());
            TableAppPO tableAppPO = baseMapper.selectOne(tableAppPOQueryWrapper);
            if (tableAppPO != null) {
                return ResultEnum.DS_APP_NAME_EXISTS;
            }
            List<TableAppDatasourcePO> tableAppDatasourcePOList = null;
            if (CollectionUtils.isNotEmpty(dto.getTableAppDatasourceDTOS())) {
                tableAppDatasourcePOList = TableAppDatasourceMap.INSTANCES.listDtoToPo(dto.getTableAppDatasourceDTOS());
            }
            tableAppPO = TableAppMap.INSTANCES.dtoToPo(dto);
            tableAppPO.setCreateTime(LocalDateTime.now());
            tableAppPO.setCreateUser(String.valueOf(userHelper.getLoginUserInfo().getId()));
            int i = baseMapper.insertOne(tableAppPO);
            if (i <= 0) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            int tableAppId = Math.toIntExact(tableAppPO.getId());
            if (CollectionUtils.isNotEmpty(tableAppDatasourcePOList)) {
                tableAppDatasourcePOList.forEach(t -> {
                    t.setTableAppId(tableAppId);
                });
                tableAppDatasourceManage.saveBatch(tableAppDatasourcePOList);
            }
        } catch (Exception ex) {
            log.error("【addRule】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(TableAppDTO dto) {
        try {
            if (dto == null || dto.getId() == 0
                    || org.apache.commons.lang.StringUtils.isEmpty(dto.getAppName())
                    || CollectionUtils.isEmpty(dto.getTableAppDatasourceDTOS())) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            TableAppPO tableAppPO = baseMapper.selectById(dto.getId());
            if (tableAppPO == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }

            List<TableAppDatasourcePO> tableAppDatasourcePOList = null;
            if (CollectionUtils.isNotEmpty(dto.getTableAppDatasourceDTOS())) {
                tableAppDatasourcePOList = TableAppDatasourceMap.INSTANCES.listDtoToPo(dto.getTableAppDatasourceDTOS());
            }
            int tableAppId = Math.toIntExact(dto.getId());
            tableAppPO = TableAppMap.INSTANCES.dtoToPo(dto);
            int i = baseMapper.updateById(tableAppPO);
            if (i <= 0) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            if (CollectionUtils.isNotEmpty(tableAppDatasourcePOList)) {
                tableAppDatasourcePOList.forEach(t -> {
                    t.setTableAppId(tableAppId);
                });
                tableAppDatasourceMapper.updateByTableAppId(tableAppId);
                tableAppDatasourceManage.saveBatch(tableAppDatasourcePOList);
            }
        } catch (Exception ex) {
            log.error("【editRule】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteData(int id) {
        try {
            if (id == 0) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            TableAppPO tableAppPO = baseMapper.selectById(id);
            if (tableAppPO == null) {
                return ResultEnum.DATA_NOTEXISTS;
            }
            int i = baseMapper.deleteByIdWithFill(tableAppPO);
            if (i <= 0) {
                return ResultEnum.DELETE_ERROR;
            }
            // 查询应用下的表ID
            QueryWrapper<AppServiceConfigPO> appServiceConfigPoQueryWrapper = new QueryWrapper<>();
            appServiceConfigPoQueryWrapper.lambda().eq(AppServiceConfigPO::getDelFlag, 1)
                    .eq(AppServiceConfigPO::getApiState, ApiStateTypeEnum.Enable.getValue())
                    .eq(AppServiceConfigPO::getAppId, id)
                    .eq(AppServiceConfigPO::getType, 2);
            List<AppServiceConfigPO> appServiceConfigPos = appServiceConfigMapper.selectList(appServiceConfigPoQueryWrapper);
            List<Long> tableServiceIdList = null;
            if (CollectionUtils.isNotEmpty(appServiceConfigPos)) {
                tableServiceIdList = appServiceConfigPos.stream().map(t -> Long.parseLong(String.valueOf(t.getServiceId()))).collect(Collectors.toList());
                // 根据表ID查询表配置详情
                QueryWrapper<TableServicePO> tableServicePoQueryWrapper = new QueryWrapper<>();
                tableServicePoQueryWrapper.lambda().eq(TableServicePO::getDelFlag, 1)
                        .in(TableServicePO::getId, tableServiceIdList);
                List<TableServicePO> tableServicePos = tableServiceMapper.selectList(tableServicePoQueryWrapper);
                if (CollectionUtils.isNotEmpty(tableServiceIdList)) {
                    tableServiceIdList = tableServicePos.stream().map(t -> t.getId()).collect(Collectors.toList());
                    BuildDeleteTableServiceDTO buildDeleteTableService = new BuildDeleteTableServiceDTO();
                    buildDeleteTableService.ids = tableServiceIdList;
                    buildDeleteTableService.olapTableEnum = OlapTableEnum.DATASERVICES;
                    buildDeleteTableService.userId = userHelper.getLoginUserInfo().id;
                    publishTaskClient.publishBuildDeleteDataServices(buildDeleteTableService);
                }
            }

            // 删除调度任务
        } catch (Exception ex) {
            log.error("【deleteRule】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum checkDataSourceIsNoUse(TableAppDatasourceDTO dto) {
        try {
            if (dto == null) {
                return ResultEnum.PARAMTER_NOTNULL;
            }
            TableAppPO tableAppPO = baseMapper.selectById(dto.getTableAppId());
            if (tableAppPO == null) {
                return ResultEnum.DS_DATA_STATUS_HAS_CHANGED;
            }
            // 查询应用下的表ID
            QueryWrapper<AppServiceConfigPO> appServiceConfigPOQueryWrapper = new QueryWrapper<>();
            appServiceConfigPOQueryWrapper.lambda().eq(AppServiceConfigPO::getDelFlag, 1)
                    .eq(AppServiceConfigPO::getApiState, ApiStateTypeEnum.Enable.getValue())
                    .eq(AppServiceConfigPO::getAppId, dto.getTableAppId())
                    .eq(AppServiceConfigPO::getType, 2);
            List<AppServiceConfigPO> appServiceConfigPOS = appServiceConfigMapper.selectList(appServiceConfigPOQueryWrapper);
            if (CollectionUtils.isEmpty(appServiceConfigPOS)) {
                return ResultEnum.SUCCESS;
            }
            List<Integer> tableServiceIdList = appServiceConfigPOS.stream().map(t -> t.getServiceId()).collect(Collectors.toList());
            // 根据表ID查询表配置详情
            QueryWrapper<TableServicePO> tableServicePOQueryWrapper = new QueryWrapper<>();
            if (dto.getDatasourceType() == 1) {
                tableServicePOQueryWrapper.lambda().eq(TableServicePO::getDelFlag, 1)
                        .eq(TableServicePO::getSourceDbId, dto.getDatasourceId())
                        .in(TableServicePO::getId, tableServiceIdList);
            } else {
                tableServicePOQueryWrapper.lambda().eq(TableServicePO::getDelFlag, 1)
                        .eq(TableServicePO::getTargetDbId, dto.getDatasourceId())
                        .in(TableServicePO::getId, tableServiceIdList);
            }
            List<TableServicePO> tableServicePOS = tableServiceMapper.selectList(tableServicePOQueryWrapper);
            if (CollectionUtils.isNotEmpty(tableServicePOS)) {
                return ResultEnum.DS_DATA_SOURCE_APPLIED;
            }
        } catch (Exception ex) {
            log.error("【checkDataSourceIsUse】ex：" + ex);
            throw new FkException(ResultEnum.ERROR, ex.getMessage());
        }
        return ResultEnum.SUCCESS;
    }
}
