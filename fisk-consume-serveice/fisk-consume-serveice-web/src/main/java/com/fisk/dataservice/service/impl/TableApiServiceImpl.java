package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.MqConstants;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.common.core.user.UserInfo;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataservice.dto.tableapi.*;
import com.fisk.dataservice.dto.tableservice.TableServicePublishStatusDTO;
import com.fisk.dataservice.entity.KsfPlantPO;
import com.fisk.dataservice.entity.TableApiParameterPO;
import com.fisk.dataservice.entity.TableApiServicePO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.enums.SpecialTypeEnum;
import com.fisk.dataservice.map.TableApiParameterMap;
import com.fisk.dataservice.map.TableApiServiceMap;
import com.fisk.dataservice.mapper.TableApiServiceMapper;
import com.fisk.dataservice.service.*;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.kafka.KafkaReceiveDTO;
import com.fisk.task.dto.task.BuildDeleteTableApiServiceDTO;
import com.fisk.task.dto.task.BuildDeleteTableServiceDTO;
import com.fisk.task.dto.task.BuildTableApiServiceDTO;
import com.fisk.task.enums.OlapTableEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service("tableApiService")
public class TableApiServiceImpl extends ServiceImpl<TableApiServiceMapper, TableApiServicePO> implements ITableApiService {

    @Resource
    ITableAppManageService tableAppService;
    @Resource
    private ITableApiParameterService tableApiParameterService;
    @Resource
    private TableSyncModeImpl tableSyncMode;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    private UserHelper userHelper;

    @Resource
    private KsfPlantService ksfPlantService;
    @Resource
    private ITableService tableService;
    @Resource
    private ITableApiLogService tableApiLogService;

    @Value("${dataservice.scan.api_address}")
    private String dataserviceUrl;
    @Value("${dataservice.retrynum}")
    private Integer retryNum;
    @Value("${dataservice.retrytime}")
    private Integer retryTime;

    @Value("${fiData-data-ods-source}")
    private Integer dbId;

    @Override
    public Page<TableApiPageDataDTO> getTableApiListData(TableApiPageQueryDTO dto) {
        return baseMapper.getTableApiListData(dto.page, dto);
    }

    @Override
    public TableApiServiceSaveDTO getApiServiceById(long apiId) {
        TableApiServicePO tableApiServicePO = getById(apiId);
        if (tableApiServicePO == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        TableApiServiceSaveDTO tableApiServiceSaveDTO = new TableApiServiceSaveDTO();
        tableApiServiceSaveDTO.setTableApiServiceDTO(TableApiServiceMap.INSTANCES.poToDto(tableApiServicePO));
        LambdaQueryWrapper<TableApiParameterPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiParameterPO::getApiId, apiId);
        List<TableApiParameterPO> list = tableApiParameterService.list(queryWrapper);
        List<TableApiParameterDTO> tableApiParameterDTOS = TableApiParameterMap.INSTANCES.listPoToDto(list);
        tableApiServiceSaveDTO.setTableApiParameterDTO(tableApiParameterDTOS);
        tableApiServiceSaveDTO.tableSyncMode = tableSyncMode.getTableServiceSyncMode(apiId, AppServiceTypeEnum.TABLE_API.getValue());
        return tableApiServiceSaveDTO;
    }


    @Override
    public ResultEntity<Object> addTableApiService(TableApiServiceDTO dto) {
        TableApiServicePO po = this.query().eq("display_name", dto.displayName).one();
        if (po != null) {
            throw new FkException(ResultEnum.DATA_EXISTS);
        }
        if (dto.specialType == null){
            dto.specialType = 0;
        }
        TableApiServicePO data = TableApiServiceMap.INSTANCES.dtoToPo(dto);
        data.setPublish(0);
        if (data.specialType != SpecialTypeEnum.NONE.getValue()){
            data.setJsonType(2);
            data.setSyncTime("1970-01-01 00:00:00.000000");
            // todo: 待修改
            switch (SpecialTypeEnum.getEnum(data.specialType)){
                case KSF_NOTICE:
                    data.setSourceDbId(dbId);
                    data.setApiName("ksf_notice");
                    data.setSqlScript("select * from public.ods_sap_ksf_notice; " +
                            "select * from public.ods_sap_headers;" +
                            " select * from public.ods_sap_details;");
                    break;
                case KSF_ITEM_DATA:
                    data.setSourceDbId(dbId);
                    data.setApiName("ksf_item_data");
                    data.setSqlScript("select * from public.ods_sap_ksf_item_sys;" +
                            " select * from public.ods_sap_itemdata;");
                    break;
                case KSF_INVENTORY_STATUS_CHANGES:
                    data.setSourceDbId(dbId);
                    data.setApiName("ksf_inventory_status_changes");
                    data.setSqlScript("select * from public.ods_sap_ksf_inventory_sys;" +
                            "select * from public.ods_sap_ksf_inventory;");
                    break;
                case KSF_ACKNOWLEDGEMENT:
                    data.setSourceDbId(dbId);
                    data.setApiName("wms_acknowledgement");
                    data.setSqlScript("select * from wms.wms_acknowledgement_sys;" +
                            "select * from wms.wms_acknowledgement_headers;" +
                            "select * from wms.wms_acknowledgement_details;");
                    break;

            }
        }
        if (!this.save(data)) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEntityBuild.build(ResultEnum.SUCCESS, data.id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum TableApiServiceSave(TableApiServiceSaveDTO dto) {
        updateTableApiService(dto.tableApiServiceDTO);
        tableApiParameterService.savetTableApiParameter(dto.tableApiParameterDTO, dto.tableApiServiceDTO.id);
        //覆盖方式
        dto.tableSyncMode.typeTableId = (int) dto.tableApiServiceDTO.id;
        dto.tableSyncMode.type = AppServiceTypeEnum.TABLE_API.getValue();
        tableSyncMode.tableServiceTableSyncMode(dto.tableSyncMode);
        BuildTableApiServiceDTO buildTableApiServiceDTO = buildParameter(dto);
        UserInfo userInfo = userHelper.getLoginUserInfo();
        buildTableApiServiceDTO.userId = userInfo.id;
        //推送task
        publishTaskClient.publishBuildDataServiceApi(buildTableApiServiceDTO);




        return ResultEnum.SUCCESS;
    }

    /**
     * 构建参数
     *
     * @param dto
     */
    public BuildTableApiServiceDTO buildParameter(TableApiServiceSaveDTO dto) {

        BuildTableApiServiceDTO data = new BuildTableApiServiceDTO();
        //表信息
        data.id = dto.tableApiServiceDTO.id;
        LambdaQueryWrapper<TableApiServicePO> apiQueryWrapper = new LambdaQueryWrapper<>();
        apiQueryWrapper.eq(TableApiServicePO::getId, dto.tableApiServiceDTO.id);
        TableApiServicePO tableApiServicePO = getOne(apiQueryWrapper);
        LambdaQueryWrapper<TableAppPO> appQueryWrapper = new LambdaQueryWrapper<>();
        appQueryWrapper.eq(TableAppPO::getId, tableApiServicePO.getAppId());
        TableAppPO tableAppPO = tableAppService.getOne(appQueryWrapper);

        KsfPlantPO plantPO = ksfPlantService.getById(tableApiServicePO.getPlantId());
        data.apiDes = tableApiServicePO.getApiDes();
        data.apiName = tableApiServicePO.getApiName();
        data.appId = (int) tableAppPO.getId();
        data.appDesc = tableAppPO.getAppDesc();
        data.appName = tableAppPO.getAppName();
        data.enable = tableApiServicePO.getEnable();
        if (plantPO != null){
            data.lgpla = plantPO.getLgpla();
            data.name = plantPO.getName();
            data.sourcesys = plantPO.getSourcesys();
        }
        //同步配置
        data.syncModeDTO = dto.tableSyncMode;

        return data;
    }

    @Override
    public ResultEnum delTableApiById(long id) {
        LambdaQueryWrapper<TableApiParameterPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TableApiParameterPO::getApiId,id);
        tableApiParameterService.remove(queryWrapper);
        tableSyncMode.delTableServiceSyncMode(id, AppServiceTypeEnum.TABLE_API.getValue());
        TableApiServicePO apiService = this.getById(id);
        this.removeById(id);
        BuildDeleteTableApiServiceDTO buildDeleteTableApiService = new BuildDeleteTableApiServiceDTO();
        buildDeleteTableApiService.appId = String.valueOf(apiService.getAppId());
        buildDeleteTableApiService.ids = Arrays.asList(apiService.id);
        buildDeleteTableApiService.olapTableEnum = OlapTableEnum.DATA_SERVICE_API;
        buildDeleteTableApiService.userId = userHelper.getLoginUserInfo().id;
        buildDeleteTableApiService.delBusiness = true;
        if (apiService.getPublish() != 0){
            publishTaskClient.publishBuildDeleteDataServiceApi(buildDeleteTableApiService);
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public void updateTableServiceStatus(TableServicePublishStatusDTO dto) {
        TableApiServicePO po = this.getById(dto.id);
        if (po == null) {
            log.error("【表服务修改状态失败,原因:表不存在】");
            return;
        }
        po.setPublish(dto.status);
        if (this.updateById(po)) {
            log.error("表服务修改状态失败,原因表:修改异常");
        }
    }
    @Override
    public List<BuildTableApiServiceDTO> getTableApiListByPipelineId(Integer pipelineId) {
            List<Integer> tableListByPipelineId = tableSyncMode.getTableListByPipelineId(pipelineId,4);
            if (CollectionUtils.isEmpty(tableListByPipelineId)) {
                return new ArrayList<>();
            }
            List<BuildTableApiServiceDTO> list = new ArrayList<>();

            for (Integer id : tableListByPipelineId) {
                TableApiServiceSaveDTO tableService = getApiServiceById(id);
                list.add(buildParameter(tableService));
            }
        return list;
    }

    @Override
    public List<BuildTableApiServiceDTO> getTableApiListByInputId(Integer inputId) {
        List<Integer> tableListByPipelineId = tableSyncMode.getTableListByInputId(inputId,4);
        if (CollectionUtils.isEmpty(tableListByPipelineId)) {
            return new ArrayList<>();
        }
        List<BuildTableApiServiceDTO> list = new ArrayList<>();

        for (Integer id : tableListByPipelineId) {
            TableApiServiceSaveDTO tableService = getApiServiceById(id);
            list.add(buildParameter(tableService));
        }
        return list;
    }

    @Override
    public ResultEnum editTableApiServiceSync(TableApiServiceSyncDTO tableApiServiceSyncDTO) {
        TableApiServicePO tableApiServicePO = baseMapper.selectById(tableApiServiceSyncDTO.getApiId());
        //判断表状态是否已发布
        if (tableApiServicePO.getPublish() != 1) {
            log.info("手动同步失败，原因：表未发布");
            return ResultEnum.TABLE_NOT_PUBLISHED;
        }
        //获取远程调用接口中需要的参数KafkaReceiveDTO
        KafkaReceiveDTO kafkaReceiveDTO = getKafkaReceive(tableApiServiceSyncDTO);
        log.info(JSON.toJSONString(kafkaReceiveDTO));

        //参数配置完毕，远程调用接口，发送参数，执行同步
        ResultEntity<Object> resultEntity = publishTaskClient.universalPublish(kafkaReceiveDTO);
        if (resultEntity.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }
        return ResultEnum.SUCCESS;
    }

    /**
     * 用于远程调用方法的参数，↑
     *
     * @return
     */
    public static KafkaReceiveDTO getKafkaReceive(TableApiServiceSyncDTO dto) {

        //拼接所需的topic
        String topic = MqConstants.TopicPrefix.TOPIC_PREFIX + OlapTableEnum.DATA_SERVICE_API.getValue() + "." + dto.appId + "." + dto.apiId;
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

    @Override
    public ResultEnum enableOrDisable(Integer id) {
        TableApiServicePO tableApiServicePO = this.getById(id);
        TableApiServiceDTO tableServiceDTO = TableApiServiceMap.INSTANCES.poToDto(tableApiServicePO);
        ResultEntity<TableApiServiceDTO> result = publishTaskClient.apiEnableOrDisable(tableServiceDTO);
        if(result.getCode() != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.REMOTE_SERVICE_CALLFAILED);
        }else {
            TableApiServiceDTO data = result.getData();
            TableApiServicePO tableApiService = TableApiServiceMap.INSTANCES.dtoToPo(data);
            if (baseMapper.updateById(tableApiService) == 0) {
                throw new FkException(ResultEnum.SAVE_DATA_ERROR);
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum importantOrUnimportant(Integer id) {
        TableApiServicePO tableApiServicePO = this.getById(id);
        if (tableApiServicePO.getImportantInterface() == 0){
            tableApiServicePO.setImportantInterface(1);
        }else if (tableApiServicePO.getImportantInterface() == 1){
            tableApiServicePO.setImportantInterface(0);
        }
        if (baseMapper.updateById(tableApiServicePO) == 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }
        return ResultEnum.SUCCESS;
    }

    private ResultEnum updateTableApiService(TableApiServiceDTO dto) {
        dto.enable = 1;
        TableApiServicePO po = this.query().eq("id", dto.id).one();
        if (po == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        po = TableApiServiceMap.INSTANCES.dtoToPo(dto);
        po.id = dto.id;
        po.setPublish(3);
        this.updateById(po);
        return ResultEnum.SUCCESS;
    }
}
