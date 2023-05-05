package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.dto.datasource.DataSourceConfigInfoDTO;
import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.dto.tableservice.*;
import com.fisk.dataservice.entity.AppServiceConfigPO;
import com.fisk.dataservice.entity.TableServicePO;
import com.fisk.dataservice.enums.ApiStateTypeEnum;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.map.DataSourceConMap;
import com.fisk.dataservice.map.TableServiceMap;
import com.fisk.dataservice.mapper.AppServiceConfigMapper;
import com.fisk.dataservice.mapper.TableServiceMapper;
import com.fisk.dataservice.service.ITableService;
import com.fisk.system.client.UserClient;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.BuildDeleteTableServiceDTO;
import com.fisk.task.dto.task.BuildTableServiceDTO;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author JianWenYang
 */
@Service
@Slf4j
public class TableServiceImpl
        extends ServiceImpl<TableServiceMapper, TableServicePO>
        implements ITableService {

    @Resource
    private PublishTaskClient publishTaskClient;

    @Resource
    TableFieldImpl tableField;

    @Resource
    TableSyncModeImpl tableSyncMode;

    @Resource
    UserClient client;

    @Resource
    private UserHelper userHelper;

    @Resource
    TableServiceMapper mapper;

    @Resource
    private AppServiceConfigMapper appServiceConfigMapper;

    @Override
    public Page<TableServicePageDataDTO> getTableServiceListData(TableServicePageQueryDTO dto) {
        return mapper.getTableServiceListData(dto.page, dto);
    }

    @Override
    public ResultEntity<Object> addTableServiceData(TableServiceDTO dto) {
        TableServicePO po = this.query().eq("table_name", dto.tableName).one();
        if (po != null) {
            throw new FkException(ResultEnum.DATA_EXISTS);
        }
        TableServicePO data = TableServiceMap.INSTANCES.dtoToPo(dto);
        if (!this.save(data)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        AppServiceConfigPO appServiceConfigPO = new AppServiceConfigPO();
        appServiceConfigPO.setAppId(dto.getTableAppId());
        appServiceConfigPO.setServiceId(Math.toIntExact(data.getId()));
        appServiceConfigPO.setApiState(ApiStateTypeEnum.Enable.getValue());
        appServiceConfigPO.setType(AppServiceTypeEnum.TABLE.getValue());
        int insert = appServiceConfigMapper.insert(appServiceConfigPO);
        if (insert <= 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, data.id);
    }

    @Override
    public List<DataSourceConfigInfoDTO> getDataSourceConfig() {

        ResultEntity<List<DataSourceDTO>> allExternalDataSource = client.getAllExternalDataSource();
        if (allExternalDataSource.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        return DataSourceConMap.INSTANCES.voListToDtoInfo(allExternalDataSource.data);
    }

    @Override
    public List<DataSourceConfigInfoDTO> getAllDataSourceConfig() {

        ResultEntity<List<DataSourceDTO>> all = client.getAll();
        if (all.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.DATA_SOURCE_ERROR);
        }

        return DataSourceConMap.INSTANCES.voListToDtoInfo(all.data);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum TableServiceSave(TableServiceSaveDTO dto) {

        //修改表服务数据
        updateTableService(dto.tableService);

        //表字段
        tableField.tableServiceSaveConfig((int) dto.tableService.id, 0, dto.tableFieldList);

        //覆盖方式
        dto.tableSyncMode.typeTableId = (int) dto.tableService.id;
        dto.tableSyncMode.type = AppServiceTypeEnum.TABLE.getValue();
        tableSyncMode.tableServiceTableSyncMode(dto.tableSyncMode);

        BuildTableServiceDTO buildTableServiceDTO = buildParameter(dto);

        UserInfo userInfo = userHelper.getLoginUserInfo();
        buildTableServiceDTO.userId = userInfo.id;

        //推送task
        publishTaskClient.publishBuildDataServices(buildTableServiceDTO);

        return ResultEnum.SUCCESS;
    }

    @Override
    public TableServiceSaveDTO getTableServiceById(long id) {
        TableServicePO po = mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        TableServiceSaveDTO data = new TableServiceSaveDTO();
        data.tableService = TableServiceMap.INSTANCES.poToDto(po);
        data.tableFieldList = tableField.getTableServiceField(id, 0);
        data.tableSyncMode = tableSyncMode.getTableServiceSyncMode(id);
        return data;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum delTableServiceById(long id) {
        TableServicePO po = mapper.selectById(id);
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        if (mapper.deleteByIdWithFill(po) == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        tableField.delTableServiceField((int) id, 0);

        tableSyncMode.delTableServiceSyncMode(id, AppServiceTypeEnum.TABLE.getValue());

        QueryWrapper<AppServiceConfigPO> appServiceConfigPOQueryWrapper = new QueryWrapper<>();
        appServiceConfigPOQueryWrapper.lambda()
                .eq(AppServiceConfigPO::getServiceId, id)
                .eq(AppServiceConfigPO::getType, AppServiceTypeEnum.TABLE.getValue());
        List<AppServiceConfigPO> appServiceConfigPOS = appServiceConfigMapper.selectList(appServiceConfigPOQueryWrapper);
        if (CollectionUtils.isNotEmpty(appServiceConfigPOS)) {
            if (appServiceConfigMapper.deleteByIdWithFill(appServiceConfigPOS.get(0)) <= 0) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        BuildDeleteTableServiceDTO buildDeleteTableService = new BuildDeleteTableServiceDTO();
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        buildDeleteTableService.userId = userHelper.getLoginUserInfo().id;
        buildDeleteTableService.ids = ids;
        buildDeleteTableService.olapTableEnum = OlapTableEnum.DATASERVICES;
        publishTaskClient.publishBuildDeleteDataServices(buildDeleteTableService);
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<BuildTableServiceDTO> getTableListByPipelineId(Integer pipelineId) {
        List<Integer> tableListByPipelineId = tableSyncMode.getTableListByPipelineId(pipelineId);
        if (CollectionUtils.isEmpty(tableListByPipelineId)) {
            return new ArrayList<>();
        }

        List<BuildTableServiceDTO> list = new ArrayList<>();

        for (Integer id : tableListByPipelineId) {
            TableServiceSaveDTO tableService = getTableServiceById(id);
            list.add(buildParameter(tableService));
        }
        return list;
    }

    @Override
    public void updateTableServiceStatus(TableServicePublishStatusDTO dto) {
        TableServicePO po = mapper.selectById(dto.id);
        if (po == null) {
            log.error("【表服务修改状态失败,原因:表不存在】");
            return;
        }
        po.publish = dto.status;
        if (mapper.updateById(po) > 0) {
            log.error("表服务修改状态失败,原因表:修改异常");
        }

    }

    @Override
    public BuildTableServiceDTO getBuildTableServiceById(long id) {

        TableServiceSaveDTO data = getTableServiceById(id);

        return buildParameter(data);
    }

    @Override
    public ResultEnum addTableServiceField(TableFieldDTO dto) {
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        List<TableFieldDTO> dtoList = new ArrayList<>();
        dtoList.add(dto);
        return tableField.addTableServiceField(0, dtoList);
    }

    @Override
    public ResultEnum editTableServiceField(TableFieldDTO dto) {
        if (dto == null) {
            return ResultEnum.PARAMTER_NOTNULL;
        }
        List<TableFieldDTO> dtoList = new ArrayList<>();
        dtoList.add(dto);
        return tableField.tableServiceSaveConfig(0, dto.getId(), dtoList);
    }

    @Override
    public ResultEnum deleteTableServiceField(long tableFieldId) {
        return tableField.delTableServiceField(0, tableFieldId);
    }

    @Override
    public ResultEnum editTableServiceSync(long tableId) {
        TableServicePO tableServicePO = mapper.selectById(tableId);
        //判断表状态是否已发布
        if (tableServicePO.getPublish() != 1) {
            log.info("手动同步失败，原因：表未发布");
            return ResultEnum.TABLE_NOT_PUBLISHED;
        }
        //获取远程调用接口中需要的参数KafkaReceiveDTO
        KafkaReceiveDTO kafkaReceiveDTO = getKafkaReceive(tableId);
        log.info(JSON.toJSONString(kafkaReceiveDTO));

        //参数配置完毕，远程调用接口，发送参数，执行同步
        ResultEntity<Object> resultEntity = publishTaskClient.universalPublish(kafkaReceiveDTO);
        if (resultEntity.getCode() != ResultEnum.SUCCESS.getCode()){
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 用于远程调用方法的参数，↑
     *
     * @return
     */
    public static KafkaReceiveDTO getKafkaReceive(Long tableId) {
        //拼接所需的topic
        String topic = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.DATASERVICES.getValue() + ".0." + tableId;
        //获取当前时间并格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTime = formatter.format(LocalDateTime.now());
        //剔除uuid生成字符串里面的"-"符号
        String pipeTaskTraceId = UUID.randomUUID().toString().replace("-", "");
        String fidata_batch_code = UUID.randomUUID().toString().replace("-", "");
        String pipelStageTraceId = UUID.randomUUID().toString().replace("-", "");
        return KafkaReceiveDTO.builder()
                .topic(topic)
                .start_time(dateTime)
                .pipelTaskTraceId(pipeTaskTraceId)
                .fidata_batch_code(fidata_batch_code)
                .pipelStageTraceId(pipelStageTraceId)
                .ifTaskStart(true)
                .topicType(1)
                .build();
    }

    /**
     * 更新表服务数据
     *
     * @param dto
     * @return
     */
    public ResultEnum updateTableService(TableServiceDTO dto) {
        TableServicePO po = this.query().eq("id", dto.id).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        po = TableServiceMap.INSTANCES.dtoToPo(dto);
        po.id = dto.id;
        if (mapper.updateById(po) == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;

    }

    /**
     * 构建参数
     *
     * @param dto
     */
    public BuildTableServiceDTO buildParameter(TableServiceSaveDTO dto) {

        BuildTableServiceDTO data = new BuildTableServiceDTO();
        //表信息
        data.id = dto.tableService.id;
        data.addType = dto.tableService.addType;
        data.dataSourceId = dto.tableService.sourceDbId;
        data.targetDbId = dto.tableService.targetDbId;
        data.tableName = dto.tableService.tableName;
        data.sqlScript = dto.tableService.sqlScript;
        data.targetTable = dto.tableService.targetTable;

        if (data.targetTable.indexOf(".") > 1) {
            String[] str = data.targetTable.split("\\.");
            data.schemaName = str[0];
            data.targetTable = str[1];
        }

        //表字段
        data.fieldDtoList = dto.tableFieldList;
        //同步配置
        data.syncModeDTO = dto.tableSyncMode;

        if (StringUtils.isBlank(data.syncModeDTO.customScriptAfter)) {
            data.syncModeDTO.customScriptAfter = "select 'fisk' as fisk";
        }

        if (StringUtils.isBlank(data.syncModeDTO.customScriptBefore)) {
            data.syncModeDTO.customScriptBefore = "select 'fisk' as fisk";
        }

        return data;
    }

}
