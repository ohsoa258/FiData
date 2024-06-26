package com.fisk.datamanagement.synchronization.pushmetadata.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.common.core.enums.task.nifi.DriverTypeEnum;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.common.service.dbMetaData.dto.TablePyhNameDTO;
import com.fisk.common.service.dbMetaData.dto.TableStructureDTO;
import com.fisk.common.service.dbMetaData.utils.MysqlConUtils;
import com.fisk.common.service.dbMetaData.utils.OracleUtils;
import com.fisk.common.service.dbMetaData.utils.PostgresConUtils;
import com.fisk.common.service.dbMetaData.utils.SqlServerPlusUtils;
import com.fisk.common.service.metadata.dto.metadata.*;
import com.fisk.common.service.sqlparser.SqlParserUtils;
import com.fisk.common.service.sqlparser.model.FieldMetaDataObject;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.consumeserveice.client.ConsumeServeiceClient;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.app.AppDataSourceDTO;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.dataaccess.enums.DataSourceTypeEnum;
import com.fisk.datamanagement.entity.BusinessClassificationPO;
import com.fisk.datamanagement.entity.MetaSyncTimePO;
import com.fisk.datamanagement.mapper.*;
import com.fisk.datamanagement.service.impl.ClassificationImpl;
import com.fisk.datamanagement.service.impl.EntityImpl;
import com.fisk.datamanagement.service.impl.MetaSyncTimePOServiceImpl;
import com.fisk.datamanagement.synchronization.pushmetadata.IBloodCompensation;
import com.fisk.datamodel.client.DataModelClient;
import com.fisk.mdm.client.MdmClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
 * 元数据同步血缘补充服务实现
 */
@Service
@Slf4j
public class BloodCompensationImpl
        implements IBloodCompensation {
    //region  装配Bean
    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    DataModelClient dataModelClient;
    @Resource
    ConsumeServeiceClient serveiceClient;
    @Resource
    MdmClient mdmClient;
    @Resource
    MetaDataImpl metaData;
    @Resource
    ClassificationImpl classification;
    @Resource
    BusinessClassificationMapper businessClassificationMapper;
    @Resource
    MetadataEntityMapper metadataEntityMapper;
    @Resource
    MetadataAttributeMapper metadataAttributeMapper;
    @Resource
    LineageMapRelationMapper lineageMapRelationMapper;
    @Resource
    MetadataBusinessMetadataMapper metadataBusinessMetadataMapper;
    @Resource
    ClassificationMapper classificationMapper;
    @Resource
    MetaDataClassificationMapMapper metaDataClassificationMapMapper;
    @Resource
    MetadataEntityClassificationAttributeMapper metadataEntityClassificationAttributeMapper;
    @Resource
    MetadataLabelMapper metadataLabelMapper;
    @Resource
    MetaDataGlossaryMapMapper metaDataGlossaryMapMapper;
    @Resource
    MetaDataEntityOperationLogMapper metaDataEntityOperationLogMapper;
    @Resource
    MetadataEntityAuditAttributeChangeMapper metadataEntityAuditAttributeChangeMapper;
    @Resource
    MetadataEntityAuditLogMapper metadataEntityAuditLogMapper;
    @Resource
    private MetaSyncTimePOMapper metaSyncTimePOMapper;
    @Resource
    private MetaSyncTimePOServiceImpl metaSyncTimePOService;
    @Resource
    private BloodCompensationImpl bloodCompensation;
    @Resource
    private EntityImpl entityImpl;


//endregion

    @Scheduled(cron = "0 0 20 * * ?")//每晚20点执行刷新元数据任务
    public void syncBlood() {
        List<Integer> moduleIds = new ArrayList<>();
        moduleIds.add(ClassificationTypeEnum.DATA_ACCESS.getValue());
        moduleIds.add(ClassificationTypeEnum.ANALYZE_DATA.getValue());
        moduleIds.add(ClassificationTypeEnum.API_GATEWAY_SERVICE.getValue());
        moduleIds.add(ClassificationTypeEnum.DATA_DISTRIBUTION.getValue());
        moduleIds.add(ClassificationTypeEnum.VIEW_ANALYZE_SERVICE.getValue());
        moduleIds.add(ClassificationTypeEnum.MASTER_DATA.getValue());
        //外部数据源暂时不跑
//        moduleIds.add(ClassificationTypeEnum.EXTERNAL_DATA.getValue());
        //不初始化 刷新所有模块
        this.systemSynchronousBloodV2(moduleIds);
    }

    /**
     * 血缘补偿  按tb_meta_sync_time表的同步时间增量 提高增量效率
     * 该方法仅供系统定时任务使用
     *
     * @return ResultEnum
     */
    @Override
    public ResultEnum systemSynchronousBloodV2(List<Integer> moduleIds) {
        String allBeginTime = DateTimeUtils.getNow();
        log.info("***************同步元数据开始时间：" + allBeginTime + "*********************");
        StopWatch externalDataStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.EXTERNAL_DATA.getValue())) {
            externalDataStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.EXTERNAL_DATA);

            log.info("******一.开始同步外部数据源元数据******");
            synchronousExternalData(null);

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            externalDataStopWatch.stop();
        }

        StopWatch dataAccessStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.DATA_ACCESS.getValue())) {
            dataAccessStopWatch.start();
            //查询上次同步时间
            LocalDateTime lastSyncTime = getLastSyncTime(ClassificationTypeEnum.DATA_ACCESS);
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.DATA_ACCESS);

            log.info("******一、开始补偿数据接入相关元数据信息******");
            log.info("******1.开始同步数据接入系统名称到业务分类******");
            ResultEntity<List<AppBusinessInfoDTO>> appList = dataAccessClient.getAppList();
            synchronousClassification(appList, ClassificationTypeEnum.DATA_ACCESS);
            //同步数据接入来源表元数据(解析接入表sql)
//            synchronousAccessSourceMetaData(currUserName);
            log.info("******3.开始同步数据接入ods表以及stg表元数据******");
            //同步数据接入ods表以及stg表元数据
            synchronousAccessTableSourceMetaData(null, syncTimeId, lastSyncTime);
            log.info("******4.数据接入ods表以及stg表元数据同步成功******");

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            dataAccessStopWatch.stop();

        }
        StopWatch analyzeDataStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.ANALYZE_DATA.getValue())) {
            analyzeDataStopWatch.start();
            //查询上次同步时间
            LocalDateTime lastSyncTime = getLastSyncTime(ClassificationTypeEnum.ANALYZE_DATA);
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.ANALYZE_DATA);

            log.info("*******二.开始同步数据建模相关元数据信息********");
            log.info("*******1.开始同步数据建模的业务分类********");
            ResultEntity<List<AppBusinessInfoDTO>> businessAreaList = dataModelClient.getBusinessAreaList();
            log.info("********2.开始同步建模业务分类元数据********");
            synchronousClassification(businessAreaList, ClassificationTypeEnum.ANALYZE_DATA);
            log.info("********3.开始同步建模ods表以及stg表元数据********");
            synchronousDataModelTableSourceMetaData(null, syncTimeId, lastSyncTime);
            log.info("******4.建模ods表以及stg表元数据同步成功******");
            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            analyzeDataStopWatch.stop();

        }
        StopWatch apiGatewayServiceStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.API_GATEWAY_SERVICE.getValue())) {
            apiGatewayServiceStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.API_GATEWAY_SERVICE);

            log.info("*******三.开始同步API网关服务相关元数据信息********");
            log.info("********1.开始API网关服务的业务分类******************");
            ResultEntity<List<AppBusinessInfoDTO>> apiAppList = serveiceClient.getApiService();
            synchronousClassification(apiAppList, ClassificationTypeEnum.API_GATEWAY_SERVICE);
            log.info("********2.开始API网关服务的元数据******************");
            synchronousAPIServiceMetaData(null);
            log.info("******3.API网关服务的元数据同步成功******");

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            apiGatewayServiceStopWatch.stop();

        }
        StopWatch viewAnalyzeServiceStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.VIEW_ANALYZE_SERVICE.getValue())) {

            viewAnalyzeServiceStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.VIEW_ANALYZE_SERVICE);

            log.info("*******四.开始同步视图服务相关元数据信息********");
            log.info("********1.开始视图服务的业务分类******************");
            ResultEntity<List<AppBusinessInfoDTO>> viewAppList = serveiceClient.getViewService();
            synchronousClassification(viewAppList, ClassificationTypeEnum.VIEW_ANALYZE_SERVICE);
            log.info("********2.开始视图服务的元数据******************");
            synchronousViewServiceMetaData(null);
            log.info("******3.视图服务的元数据同步成功******");

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            viewAnalyzeServiceStopWatch.stop();

        }
        StopWatch dataDistributionStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.DATA_DISTRIBUTION.getValue())) {

            dataDistributionStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.DATA_DISTRIBUTION);

            log.info("*******五.开始同步数据库同步服务相关元数据信息********");
            log.info("********1.开始数据库同步服务的业务分类******************");
            ResultEntity<List<AppBusinessInfoDTO>> tableAppList = serveiceClient.getTableService();
            synchronousClassification(tableAppList, ClassificationTypeEnum.DATA_DISTRIBUTION);
            log.info("********2.开始数据库同步服务的元数据******************");
            synchronousDataBaseSyncMetaData(null);
            log.info("******3.数据库同步服务的元数据同步成功******");

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            dataDistributionStopWatch.stop();

        }
        StopWatch masterDataStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.MASTER_DATA.getValue())) {

            masterDataStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.MASTER_DATA);

            log.info("*******六.开始主数据相关元数据信息********");
            log.info("********1.开始同步主数据业务分类******************");
            ResultEntity<List<AppBusinessInfoDTO>> masterDataModel = mdmClient.getMasterDataModel();
            synchronousClassification(masterDataModel, ClassificationTypeEnum.MASTER_DATA);
            log.info("********2.开始主数据的元数据******************");
            synchronousMasterDataMetaData(null, null, null);
            log.info("********3.主数据的元数据同步成功******************");

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            masterDataStopWatch.stop();

        }
        log.info("***************元数据同步结束。开始时间：" + allBeginTime + ". 结束时间: " + DateTimeUtils.getNow() + "*********************");
        log.info("******同步外部数据源元数据同步耗时: " + externalDataStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******数据接入元数据同步耗时: " + dataAccessStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******数据建模元数据同步耗时: " + analyzeDataStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******API网关服务元数据同步耗时: " + apiGatewayServiceStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******视图服务元数据同步耗时: " + viewAnalyzeServiceStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******数据库同步服务元数据同步耗时: " + dataDistributionStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******主数据元数据同步耗时: " + masterDataStopWatch.getTotalTimeSeconds() + "秒 ******");

        //开始刷新元数据树redis缓存
        log.info("******开始刷新元数据树redis缓存******");
        try {
            entityImpl.refreshEntityTreeList();
        }catch (Exception e){
            log.error("******刷新元数据树redis缓存失败******"+e);
        }

        return ResultEnum.SUCCESS;
    }

    /**
     * 获取特点服务的上次同步元数据的时间
     *
     * @param item
     * @return
     */
    private LocalDateTime getLastSyncTime(ClassificationTypeEnum item) {
        //获取最近一次成功同步的时间
        MetaSyncTimePO one = metaSyncTimePOService.getOne(new LambdaQueryWrapper<MetaSyncTimePO>()
                .eq(MetaSyncTimePO::getServiceType, item.getValue())
                .eq(MetaSyncTimePO::getStatus, 1)
                .orderByDesc(MetaSyncTimePO::getCreateTime)
                .last("limit 1")
        );
        return one.getCreateTime();
    }

    /**
     * 血缘补偿
     *
     * @param currUserName   执行账号
     * @param initialization 是否是初始化
     * @return ResultEnum
     */
    @Override
    public ResultEnum systemSynchronousBlood(String currUserName, boolean initialization, List<Integer> moduleIds) {
        if (initialization) {
            //清空系统血缘
            TruncateBlood();
        }
        //为空则同步所有模块
        if (moduleIds == null || (long) moduleIds.size() == 0) {
            moduleIds = Arrays.stream(ClassificationTypeEnum.values()).map(ClassificationTypeEnum::getValue).collect(Collectors.toList());
        }
        String allBeginTime = DateTimeUtils.getNow();
        log.info("***************同步元数据开始时间：" + allBeginTime + "*********************");
        StopWatch externalDataStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.EXTERNAL_DATA.getValue())) {
            externalDataStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.EXTERNAL_DATA);
            log.info("******1.开始同步外部数据源元数据******");
            synchronousExternalData(currUserName);
            log.info("******2.外部数据源元数据同步成功******");
            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            externalDataStopWatch.stop();

        }

        StopWatch dataAccessStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.DATA_ACCESS.getValue())) {
            dataAccessStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.DATA_ACCESS);

            log.info("******一、开始补偿数据接入相关元数据信息******");
            log.info("******1.开始同步数据接入系统名称到业务分类******");
            ResultEntity<List<AppBusinessInfoDTO>> appList = dataAccessClient.getAppList();
            synchronousClassification(appList, ClassificationTypeEnum.DATA_ACCESS);
            //同步数据接入来源表元数据(解析接入表sql)
//            synchronousAccessSourceMetaData(currUserName);
            log.info("******3.开始同步数据接入ods表以及stg表元数据******");
            //同步数据接入ods表以及stg表元数据
            synchronousAccessTableSourceMetaData(currUserName, null, null);
            log.info("******4.数据接入ods表以及stg表元数据同步成功******");

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            dataAccessStopWatch.stop();

        }
        StopWatch analyzeDataStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.ANALYZE_DATA.getValue())) {
            analyzeDataStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.ANALYZE_DATA);

            log.info("*******二.开始同步数据建模相关元数据信息********");
            log.info("*******1.开始同步数据建模的业务分类********");
            ResultEntity<List<AppBusinessInfoDTO>> businessAreaList = dataModelClient.getBusinessAreaList();
            log.info("********2.开始同步建模业务分类元数据********");
            synchronousClassification(businessAreaList, ClassificationTypeEnum.ANALYZE_DATA);
            log.info("********3.开始同步建模ods表以及stg表元数据********");
            synchronousDataModelTableSourceMetaData(currUserName, syncTimeId, null);
            log.info("******4.建模ods表以及stg表元数据同步成功******");

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            analyzeDataStopWatch.stop();
        }
        StopWatch apiGatewayServiceStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.API_GATEWAY_SERVICE.getValue())) {
            apiGatewayServiceStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.API_GATEWAY_SERVICE);

            log.info("*******三.开始同步API网关服务相关元数据信息********");
            log.info("********1.开始API网关服务的业务分类******************");
            ResultEntity<List<AppBusinessInfoDTO>> apiAppList = serveiceClient.getApiService();
            synchronousClassification(apiAppList, ClassificationTypeEnum.API_GATEWAY_SERVICE);
            log.info("********2.开始API网关服务的元数据******************");
            synchronousAPIServiceMetaData(currUserName);
            log.info("******3.API网关服务的元数据同步成功******");

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            apiGatewayServiceStopWatch.stop();

        }
        StopWatch viewAnalyzeServiceStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.VIEW_ANALYZE_SERVICE.getValue())) {

            viewAnalyzeServiceStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.VIEW_ANALYZE_SERVICE);

            log.info("*******四.开始同步视图服务相关元数据信息********");
            log.info("********1.开始视图服务的业务分类******************");
            ResultEntity<List<AppBusinessInfoDTO>> viewAppList = serveiceClient.getViewService();
            synchronousClassification(viewAppList, ClassificationTypeEnum.VIEW_ANALYZE_SERVICE);
            log.info("********2.开始视图服务的元数据******************");
            synchronousViewServiceMetaData(currUserName);
            log.info("******3.视图服务的元数据同步成功******");

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            viewAnalyzeServiceStopWatch.stop();

        }
        StopWatch dataDistributionStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.DATA_DISTRIBUTION.getValue())) {

            dataDistributionStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.DATA_DISTRIBUTION);

            log.info("*******五.开始同步数据库同步服务相关元数据信息********");
            log.info("********1.开始数据库同步服务的业务分类******************");
            ResultEntity<List<AppBusinessInfoDTO>> tableAppList = serveiceClient.getTableService();
            synchronousClassification(tableAppList, ClassificationTypeEnum.DATA_DISTRIBUTION);
            log.info("********2.开始数据库同步服务的元数据******************");
            synchronousDataBaseSyncMetaData(currUserName);
            log.info("******3.数据库同步服务的元数据同步成功******");

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            dataDistributionStopWatch.stop();

        }
        StopWatch masterDataStopWatch = new StopWatch();
        if (moduleIds.contains(ClassificationTypeEnum.MASTER_DATA.getValue())) {

            masterDataStopWatch.start();
            //插入同步时间 获取返回的主键id
            long syncTimeId = addLastSyncTime(ClassificationTypeEnum.MASTER_DATA);

            log.info("*******六.开始主数据相关元数据信息********");
            log.info("********1.开始同步主数据业务分类******************");
            ResultEntity<List<AppBusinessInfoDTO>> masterDataModel = mdmClient.getMasterDataModel();
            synchronousClassification(masterDataModel, ClassificationTypeEnum.MASTER_DATA);
            log.info("********2.开始主数据的元数据******************");
            synchronousMasterDataMetaData(currUserName, null, null);
            log.info("********3.主数据的元数据同步成功******************");

            //同步完成后更新状态
            //1成功 2失败 3同步中
            updateLastSyncTime(syncTimeId, 1);
            masterDataStopWatch.stop();

        }
        log.info("***************元数据同步结束。开始时间：" + allBeginTime + ". 结束时间: " + DateTimeUtils.getNow() + "*********************");
        log.info("******同步外部数据源元数据同步耗时: " + externalDataStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******数据接入元数据同步耗时: " + dataAccessStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******数据建模元数据同步耗时: " + analyzeDataStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******API网关服务元数据同步耗时: " + apiGatewayServiceStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******视图服务元数据同步耗时: " + viewAnalyzeServiceStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******数据库同步服务元数据同步耗时: " + dataDistributionStopWatch.getTotalTimeSeconds() + "秒 ******");
        log.info("******主数据元数据同步耗时: " + masterDataStopWatch.getTotalTimeSeconds() + "秒 ******");

        //开始刷新元数据树redis缓存
        log.info("******开始刷新元数据树redis缓存******");
        try {
            entityImpl.refreshEntityTreeList();
        }catch (Exception e){
            log.error("******刷新元数据树redis缓存失败******"+e);
        }

        return ResultEnum.SUCCESS;
    }
    //region 内置实现方法

    /**
     * 获取特点服务的上次同步元数据的时间
     *
     * @param item
     * @return
     */
    private Long addLastSyncTime(ClassificationTypeEnum item) {
        MetaSyncTimePO po = new MetaSyncTimePO();
        po.setServiceType(item.getValue());
        //3 同步中
        po.setStatus(3);
        metaSyncTimePOService.save(po);
        return po.getId();
    }

    /**
     * 同步完成后更新状态
     *
     * @param id
     * @return
     */
    public void updateLastSyncTime(long id, int status) {
        metaSyncTimePOService.update(new LambdaUpdateWrapper<MetaSyncTimePO>()
                .eq(MetaSyncTimePO::getId, id)
                .set(MetaSyncTimePO::getStatus, status)
                .set(MetaSyncTimePO::getUpdateTime, LocalDateTime.now())
        );
    }

    /**
     * 同步API网关服务的元数据信息
     *
     * @param currUserName 当前执行账号
     */
    private void synchronousAPIServiceMetaData(String currUserName) {
        //待补充
        ResultEntity<List<MetaDataEntityDTO>> apiMetaDataResult = serveiceClient.getApiMetaData();
        List<MetaDataEntityDTO> metaDataList = apiMetaDataResult.data;
        metaData.syncDataConsumptionMetaData(metaDataList, currUserName);

    }

    /**
     * 同步视图服务的元数据信息
     *
     * @param currUserName 当前执行账号
     */
    private void synchronousViewServiceMetaData(String currUserName) {
        //待补充
        ResultEntity<List<MetaDataEntityDTO>> apiMetaDataResult = serveiceClient.getViewServiceMetaData();
        List<MetaDataEntityDTO> metaDataList = apiMetaDataResult.data;
        metaData.syncDataConsumptionMetaData(metaDataList, currUserName);

    }


    /**
     * 同步数据库同步服务的元数据信息
     *
     * @param currUserName 当前执行账号
     */
    private void synchronousDataBaseSyncMetaData(String currUserName) {
        //待补充
        ResultEntity<List<MetaDataEntityDTO>> apiMetaDataResult = serveiceClient.getTableSyncMetaData();
        List<MetaDataEntityDTO> metaDataList = apiMetaDataResult.data;
        metaData.syncDataConsumptionMetaData(metaDataList, currUserName);

    }
    //region 初始化血缘方法

    /**
     * 清空系统血缘
     */
    private void TruncateBlood() {
        //1.清空元数据对象实体表：tb_metadata_entity
        metadataEntityMapper.truncateTable();
        //2.清空元数据对象技术属性表：tb_metadata_attribute
        metadataAttributeMapper.truncateTable();
        //3.清空元数据实体血缘关系映射表：tb_lineage_map_relation
        lineageMapRelationMapper.truncateTable();
        //4.清空元数据对象与业务元数据属性映射表：tb_metadata_business_metadata_map
        metadataBusinessMetadataMapper.truncateTable();
        //5.清空业务分类表：tb_business_classification
        businessClassificationMapper.truncateTable();
        // 6.清空审计日志表
        metadataEntityAuditAttributeChangeMapper.truncateTable();
        metadataEntityAuditLogMapper.truncateTable();
        //插入默认业务分类根节点：
        InsertRootBusinessClassification(ClassificationTypeEnum.DATA_ACCESS);
        InsertRootBusinessClassification(ClassificationTypeEnum.ANALYZE_DATA);
        InsertRootBusinessClassification(ClassificationTypeEnum.API_GATEWAY_SERVICE);
        InsertRootBusinessClassification(ClassificationTypeEnum.DATA_DISTRIBUTION);
        InsertRootBusinessClassification(ClassificationTypeEnum.VIEW_ANALYZE_SERVICE);
        InsertRootBusinessClassification(ClassificationTypeEnum.MASTER_DATA);

        //6.清空业务分类-分类属性表：tb_classification
        classificationMapper.truncateTable();
        //7.清空元数据对象所属业务分类映射表：tb_metadata_classification_map
        metaDataClassificationMapMapper.truncateTable();
        //8.清空元数据实体分类属性表：tb_metadata_entity_classification_attribute
        metadataEntityClassificationAttributeMapper.truncateTable();
        //9.清空元数据标签映射表：tb_metadata_label_map
        metadataLabelMapper.truncateTable();
        //10.清空元数据术语与实体映射表：tb_metadata_glossary_map
        metaDataGlossaryMapMapper.truncateTable();
        //11.元数据实体操作日志表：tb_metadata_entity_operation_log
        metaDataEntityOperationLogMapper.truncateTable();
        //12.清空元数据同步时间表
        metaSyncTimePOMapper.truncateTable();
//        //插入默认时间
//        initMetaSyncTime(ClassificationTypeEnum.DATA_ACCESS);
//        initMetaSyncTime(ClassificationTypeEnum.ANALYZE_DATA);
//        initMetaSyncTime(ClassificationTypeEnum.API_GATEWAY_SERVICE);
//        initMetaSyncTime(ClassificationTypeEnum.DATA_DISTRIBUTION);
//        initMetaSyncTime(ClassificationTypeEnum.VIEW_ANALYZE_SERVICE);
//        initMetaSyncTime(ClassificationTypeEnum.MASTER_DATA);
//        initMetaSyncTime(ClassificationTypeEnum.EXTERNAL_DATA);

    }

    /**
     * 初始化插入根节点
     *
     * @param item 枚举根节点
     */
    private void InsertRootBusinessClassification(ClassificationTypeEnum item) {
        BusinessClassificationPO POData = new BusinessClassificationPO();
        POData.name = item.getName();
        POData.id = item.getValue();
        POData.description = item.getDescription();
        businessClassificationMapper.insert(POData);
    }
    //endregion

//    /**
//     * 初始化各服务元数据同步时间表
//     *
//     * @param item 枚举根节点
//     */
//    private void initMetaSyncTime(ClassificationTypeEnum item) {
//        MetaSyncTimePO metaSyncTimePO = new MetaSyncTimePO();
//        metaSyncTimePO.setServiceType(item.getValue());
//        metaSyncTimePO.setStatus(3);
//        metaSyncTimePOMapper.insert(metaSyncTimePO);
//    }

    /**
     * 同步到业务分类的公共方法
     *
     * @param appList                接入业务系统
     * @param classificationTypeEnum 建模类型
     */
    private void synchronousClassification(ResultEntity<List<AppBusinessInfoDTO>> appList, ClassificationTypeEnum classificationTypeEnum) {
        if (appList.code != ResultEnum.SUCCESS.getCode()) {
            log.error("【获取" + classificationTypeEnum.getName() + "的业务分类数据失败】");
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        log.info("********开始同步" + classificationTypeEnum.getName() + "的业务分类********");
        if (CollectionUtils.isEmpty(appList.data)) {
            log.error("【未获取到" + classificationTypeEnum.getName() + "数据】");
        }
        for (AppBusinessInfoDTO item : appList.data) {
            ClassificationInfoDTO classificationInfoDto = new ClassificationInfoDTO();
            classificationInfoDto.setName(item.name);
            classificationInfoDto.setDescription(item.appDes);
            classificationInfoDto.setSourceType(classificationTypeEnum);
            classificationInfoDto.setAppType(item.getAppType());
            classificationInfoDto.setDelete(false);
            try {
                classification.appSynchronousClassification(classificationInfoDto);
            } catch (Exception e) {
                log.error("【同步业务分类失败】,分类名称:{}" + item.name, e.getMessage());
            }
        }
    }

    /**
     * 同步数据接入来源表元数据和数据血缘
     *
     * @param currUserName 当前执行账号
     */
    private void synchronousAccessSourceMetaData(String currUserName) {
        //获取所有接入表
        ResultEntity<List<DataAccessSourceTableDTO>> dataAccessMetaData = dataAccessClient.getDataAccessMetaData();
        List<DataAccessSourceTableDTO> collect = dataAccessMetaData.data.stream()
                .filter(d -> !("sftp").equals(d.driveType))
                .filter(d -> !("ftp").equals(d.driveType))
                .collect(Collectors.toList());
        if (dataAccessMetaData.code != ResultEnum.SUCCESS.getCode()) {
            log.error("【获取接入所有表失败】");
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        //获取所有接入的元数据对象。
        ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAppRegistration = dataAccessClient.synchronizationAppRegistration();
        if (synchronizationAppRegistration.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        if (CollectionUtils.isEmpty(synchronizationAppRegistration.data)
                || CollectionUtils.isEmpty(collect)) {
            log.error("【获取接入所有应用失败】");
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }

        for (DataAccessSourceTableDTO accessTable : collect) {
            Optional<MetaDataInstanceAttributeDTO> first = synchronizationAppRegistration
                    .data.stream().filter(e -> e.comment.equals(String.valueOf(accessTable.dataSourceId))).findFirst();
            if (!first.isPresent()) {
                continue;
            }
            if (CollectionUtils.isEmpty(first.get().dbList.get(0).tableList)) {
                first.get().dbList.get(0).tableList = new ArrayList<>();
            }
            //解析sql
            List<TableMetaDataObject> res;
            //解析SQL过滤掉SFTP，FTP，
            if (("sftp").equals(accessTable.driveType) || ("ftp").equals(accessTable.driveType)) {
                continue;
            } else {
                log.debug("accessTable日志" + accessTable);
                log.debug("accessTable信息:表名称：" + accessTable.tableName + ",表ID" + accessTable.id + ",表脚本" + accessTable.sqlScript);
//                res = SqlParserUtils.sqlDriveConversionName(accessTable.appId, accessTable.driveType, accessTable.sqlScript);
                res = SqlParserUtils.getAllTableFiledMeta(accessTable.sqlScript);
            }
            if (CollectionUtils.isEmpty(res)) {
                continue;
            }

            List<MetaDataTableAttributeDTO> tableList = new ArrayList<>();
            for (TableMetaDataObject item : res) {
                MetaDataTableAttributeDTO table = new MetaDataTableAttributeDTO();
                table.setQualifiedName(first.get().dbList.get(0).qualifiedName + "_" + item.name);
                table.setName(item.name);
                table.setComment(String.valueOf(accessTable.appId));
                table.setDisplayName(item.name);
                table.setComment("stg");
                table.setDescription("stg");
                List<MetaDataColumnAttributeDTO> fieldList = new ArrayList<>();
                if (item.getFields() != null) {
                    for (FieldMetaDataObject fieldItem : item.getFields()) {
                        MetaDataColumnAttributeDTO field = new MetaDataColumnAttributeDTO();
                        field.setName(fieldItem.name);
                        field.setQualifiedName(table.getQualifiedName() + "_" + fieldItem.name);
                        field.setDisplayName(fieldItem.name);
                        field.setDataType("");
                        field.setOwner("");
                        fieldList.add(field);
                    }
                }

                table.setColumnList(fieldList);
                tableList.add(table);
            }
            first.get().dbList.get(0).tableList.addAll(tableList);
        }

        metaData.consumeMetaData(synchronizationAppRegistration.data, currUserName, ClassificationTypeEnum.DATA_ACCESS, null);

    }

    /**
     * 同步数据接入STG到ODS的元数据
     *
     * @param currUserName 当前执行账号
     */
    private void synchronousAccessTableSourceMetaData(String currUserName, Long syncTimeId, LocalDateTime lastSyncTime) {
        ResultEntity<List<MetaDataInstanceAttributeDTO>> accessTable;
        if (lastSyncTime == null) {
            accessTable = dataAccessClient.synchronizationAccessTable();
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedLastSyncTime = lastSyncTime.format(formatter);
            accessTable = dataAccessClient.synchronizationAccessTableByLastSyncTime(formattedLastSyncTime);
        }

        if (accessTable.code != ResultEnum.SUCCESS.getCode()) {
            if (syncTimeId != null) {
                //1成功 2失败 3同步中
                bloodCompensation.updateLastSyncTime(syncTimeId, 2);
            }
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        metaData.consumeMetaData(accessTable.data, currUserName, ClassificationTypeEnum.DATA_ACCESS, syncTimeId);
    }

    /**
     * 同步数仓建模的元数据
     *
     * @param currUserName 当前执行账号
     */
    public void synchronousDataModelTableSourceMetaData(String currUserName, Long syncTimeId, LocalDateTime lastSyncTime) {
        ResultEntity<List<MetaDataInstanceAttributeDTO>> dataModelMetaData;
        if (lastSyncTime == null) {
            dataModelMetaData = dataModelClient.getDataModelMetaData();
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedLastSyncTime = lastSyncTime.format(formatter);
            dataModelMetaData = dataModelClient.getDataModelMetaDataByLastSyncTime(formattedLastSyncTime);
        }

        if (dataModelMetaData.code != ResultEnum.SUCCESS.getCode()) {
            if (syncTimeId != null) {
                //1成功 2失败 3同步中
                bloodCompensation.updateLastSyncTime(syncTimeId, 2);
            }
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        metaData.consumeMetaData(dataModelMetaData.data, currUserName, ClassificationTypeEnum.ANALYZE_DATA, syncTimeId);
    }

    /**
     * 同步主数据的元数据
     *
     * @param currUserName 当前执行账号
     */
    public void synchronousMasterDataMetaData(String currUserName, Long syncTimeId, LocalDateTime lastSyncTime) {
        ResultEntity<List<MetaDataInstanceAttributeDTO>> dataModelMetaData = mdmClient.getMasterDataMetaData(null);
        if (dataModelMetaData.code != ResultEnum.SUCCESS.getCode()) {
            if (syncTimeId != null) {
                //1成功 2失败 3同步中
                bloodCompensation.updateLastSyncTime(syncTimeId, 2);
            }
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        metaData.consumeMetaData(dataModelMetaData.data, currUserName, ClassificationTypeEnum.MASTER_DATA, syncTimeId);
    }

    /**
     * 同步外部数据源
     *
     * @param currUserName
     */
    public void synchronousExternalData(String currUserName) {
        //获取数据接入已使用的外部数据源所有的表元数据信息
        List<AppDataSourceDTO> allAppSources = dataAccessClient.getAppSources();
        List<AppDataSourceDTO> appSources = allAppSources.stream().filter(e -> e.driveType.equals(DataSourceTypeEnum.SQLSERVER.getName())
                        || e.driveType.equals(DataSourceTypeEnum.MYSQL.getName())
                        || e.driveType.equals(DataSourceTypeEnum.ORACLE.getName())
                        || e.driveType.equals(DataSourceTypeEnum.POSTGRESQL.getName()))
                .map(e -> {
                    //去重数据源,防止重复数据源
                    AppDataSourceDTO appDataSourceDTO = new AppDataSourceDTO();
                    appDataSourceDTO.dbName = e.dbName;
                    appDataSourceDTO.connectStr = e.connectStr;
                    appDataSourceDTO.connectAccount = e.connectAccount;
                    appDataSourceDTO.connectPwd = e.connectPwd;
                    appDataSourceDTO.driveType = e.driveType;
                    appDataSourceDTO.port = e.port;
                    appDataSourceDTO.host = e.host;
                    return appDataSourceDTO;
                }).distinct().collect(Collectors.toList());
        List<MetaDataInstanceAttributeDTO> instanceList = new ArrayList<>();
        for (AppDataSourceDTO appSource : appSources) {
            List<TablePyhNameDTO> tableNameAndColumns = null;
            String dbQualifiedName = null;

            List<MetaDataTableAttributeDTO> tableAttributeDTOList = new ArrayList<>();
            try {
                //组装实例信息
                MetaDataInstanceAttributeDTO instanceAttributeDTO = buildInstance(appSource);
                instanceList.add(instanceAttributeDTO);
                switch (appSource.driveType) {
                    case "oracle":
                        tableNameAndColumns = new OracleUtils().getTableNameAndColumns(appSource.connectStr, appSource.connectAccount, appSource.connectPwd, DriverTypeEnum.ORACLE);
                        break;
                    case "mysql":
                        tableNameAndColumns = new MysqlConUtils().getTableNameAndColumns(appSource.connectStr, appSource.connectAccount, appSource.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.MYSQL);
                        break;
                    case "sqlserver":
                        tableNameAndColumns = new SqlServerPlusUtils().getTableNameAndColumnsPlus(appSource.connectStr, appSource.connectAccount, appSource.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.SQLSERVER);
                        break;
                    case "postgresql":
                        tableNameAndColumns = new PostgresConUtils().getTableNameAndColumns(appSource.connectStr, appSource.connectAccount, appSource.connectPwd, com.fisk.common.core.enums.dataservice.DataSourceTypeEnum.POSTGRESQL);
                        break;
                    default:
                        break;
                }
                dbQualifiedName = instanceAttributeDTO.getDbList().get(0).getQualifiedName();

                instanceAttributeDTO.getDbList().get(0).tableList = tableAttributeDTOList;

            } catch (Exception e) {
                log.error("查询外部数据源元数据信息失败" + e);
                continue;
            }
            if (!CollectionUtils.isEmpty(tableNameAndColumns)) {
                for (TablePyhNameDTO tableNameAndColumn : tableNameAndColumns) {
                    MetaDataTableAttributeDTO tableAttributeDTO = new MetaDataTableAttributeDTO();
                    tableAttributeDTO.setName(tableNameAndColumn.tableName);
                    tableAttributeDTO.setQualifiedName(dbQualifiedName + "_" + tableNameAndColumn.tableName);
                    tableAttributeDTO.setDisplayName(tableNameAndColumn.tableFullName);
                    tableAttributeDTO.setIsExistStg(false);
                    tableAttributeDTO.setOwner("外部数据源");
                    tableAttributeDTO.setIsExistClassification(false);
                    List<MetaDataColumnAttributeDTO> columnAttributeDTOList = new ArrayList<>();
                    for (TableStructureDTO field : tableNameAndColumn.getFields()) {
                        MetaDataColumnAttributeDTO metaDataColumnAttributeDTO = new MetaDataColumnAttributeDTO();
                        metaDataColumnAttributeDTO.setName(field.fieldName);
                        metaDataColumnAttributeDTO.setQualifiedName(tableAttributeDTO.qualifiedName + "_" + field.fieldName);
                        metaDataColumnAttributeDTO.setDisplayName(field.fieldName);
                        metaDataColumnAttributeDTO.setDataType(field.fieldType);
                        metaDataColumnAttributeDTO.setLength(field.fieldLength + "");
                        metaDataColumnAttributeDTO.setDescription(field.fieldDes);
                        metaDataColumnAttributeDTO.setOwner("外部数据源");
                        columnAttributeDTOList.add(metaDataColumnAttributeDTO);
                    }
                    tableAttributeDTO.columnList = columnAttributeDTOList;
                    tableAttributeDTOList.add(tableAttributeDTO);
                }
            }
        }
        metaData.consumeMetaData(instanceList, currUserName, ClassificationTypeEnum.EXTERNAL_DATA, null);


    }

    public MetaDataInstanceAttributeDTO buildInstance(AppDataSourceDTO appSource) {
        // 实例
        MetaDataInstanceAttributeDTO instance = new MetaDataInstanceAttributeDTO();
        instance.setRdbms_type(appSource.driveType);
        instance.setHostname(appSource.host);
        instance.setPort(appSource.port);
        instance.setQualifiedName(appSource.host);
        instance.setName(appSource.host);
        instance.setDisplayName(appSource.host);

        // 库
        List<MetaDataDbAttributeDTO> dbList = new ArrayList<>();
        MetaDataDbAttributeDTO db = new MetaDataDbAttributeDTO();
        db.setQualifiedName(appSource.host + "_" + appSource.dbName);
        db.setName(appSource.dbName);
        db.setDisplayName(appSource.dbName);

        dbList.add(db);
        instance.setDbList(dbList);

        return instance;
    }

}
