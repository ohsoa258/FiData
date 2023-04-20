package com.fisk.datamanagement.synchronization.pushmetadata.impl;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.server.metadata.ClassificationInfoDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataTableAttributeDTO;
import com.fisk.common.service.sqlparser.SqlParserUtils;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.consumeserveice.client.ConsumeServeiceClient;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datamanagement.enums.ClassificationTypeEnum;
import com.fisk.datamanagement.service.impl.ClassificationImpl;
import com.fisk.datamanagement.synchronization.pushmetadata.IBloodCompensation;
import com.fisk.datamodel.client.DataModelClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author JianWenYang
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
    MetaDataImpl metaData;
    @Resource
    ClassificationImpl classification;
//endregion
    /**
     * 血缘补偿
     * @param currUserName  执行账号
     * @param initialization   是否是初始化
     * @return ResultEnum
     */
    @Override
    public ResultEnum systemSynchronousBlood(String currUserName,boolean initialization) {
        if(initialization)
        {
            //清空系统血缘
            TruncateBlood();
        }
        log.info("******一.开始补偿数据接入相关元数据信息******");
        log.info("******1.开始同步数据接入系统名称到业务分类******");
        ResultEntity<List<AppBusinessInfoDTO>> appList = dataAccessClient.getAppList();
        synchronousClassification(appList,ClassificationTypeEnum.DATA_ACCESS);
        log.info("******2.开始同步数据接入来源表元数据******");
        //同步数据接入来源表元数据(解析接入表sql)
        synchronousAccessSourceMetaData(currUserName);
        log.info("******3.开始同步数据接入ods表以及stg表元数据******");
        //同步数据接入ods表以及stg表元数据
        synchronousAccessTableSourceMetaData(currUserName);

        log.info("*******二.开始同步数据建模相关元数据信息********");
        log.info("*******1.开始同步数据建模的业务分类********");
        ResultEntity<List<AppBusinessInfoDTO>> businessAreaList = dataModelClient.getBusinessAreaList();
        synchronousClassification(businessAreaList,ClassificationTypeEnum.ANALYZE_DATA);
        log.info("********2.开始同步建模业务分类元数据********");
        synchronousDataModelTableSourceMetaData(currUserName);

        log.info("*******三.开始同步API网关服务相关元数据信息********");
        log.info("********1.开始API网关服务的应用接入的业务分类******************");
        ResultEntity<List<AppBusinessInfoDTO>> apiTableViewAppList = serveiceClient.getApiTableViewService();
        synchronousClassification(apiTableViewAppList, ClassificationTypeEnum.API_GATEWAY_SERVICE);
        log.info("********2.开始API网关服务的元数据******************");
        synchronousAPIServiceMetaData(currUserName);
        return ResultEnum.SUCCESS;
    }
    //region 内置实现方法
    /**
     * 同步API网关服务的元数据信息
     * @param currUserName 当前执行账号
     */
    private void synchronousAPIServiceMetaData(String currUserName) {
        //待补充
    }
    /**
     * 清空系统血缘
     */
    private void TruncateBlood() {
        //待补充
        //1.清空元数据对象实体表：tb_metadata_entity
        //2.清空元数据对象技术属性表：tb_metadata_attribute
        //3.清空元数据实体血缘关系映射表：tb_lineage_map_relation
        //4.清空元数据对象与业务元数据属性映射表：tb_metadata_business_metadata_map
        //5.清空业务分类表：tb_business_classification
        //6.清空业务分类-分类属性表：tb_classification
        //7.清空元数据对象所属业务分类映射表：tb_metadata_classification_map
        //8.清空元数据实体分类属性表：tb_metadata_entity_classification_attribute
        //9.清空元数据标签映射表：tb_metadata_label_map
        //10.清空元数据术语与实体映射表：tb_metadata_glossary_map
        //11.元数据实体操作日志表：tb_metadata_entity_operation_log

    }
    /**
     *同步到业务分类的公共方法
     * @param appList 接入业务系统
     * @param classificationTypeEnum  建模类型
     */
    private void synchronousClassification(ResultEntity<List<AppBusinessInfoDTO>> appList,ClassificationTypeEnum classificationTypeEnum){
        if (appList.code != ResultEnum.SUCCESS.getCode()) {
            log.error("【获取"+classificationTypeEnum.getName()+"的业务分类数据失败】");
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        log.info("********开始同步"+classificationTypeEnum.getName()+"的业务分类********");
        if (CollectionUtils.isEmpty(appList.data)) {
            log.error("【未获取到"+classificationTypeEnum.getName()+"数据】");
        }
        for (AppBusinessInfoDTO item : appList.data) {
            ClassificationInfoDTO classificationInfoDto = new ClassificationInfoDTO();
            classificationInfoDto.setName(item.name);
            classificationInfoDto.setDescription(item.appDes);
            classificationInfoDto.setSourceType(classificationTypeEnum.getValue());
            classificationInfoDto.setDelete(false);
            try {
                classification.appSynchronousClassification(classificationInfoDto);
            } catch (Exception e) {
                log.error("【同步业务分类失败】,分类名称:{}", item.name);
                continue;
            }
        }
    }
    /**
     * 同步数据接入来源表元数据
     * @param currUserName 当前执行账号
     */
    private void synchronousAccessSourceMetaData(String currUserName) {
        //获取所有接入表
        ResultEntity<List<DataAccessSourceTableDTO>> dataAccessMetaData = dataAccessClient.getDataAccessMetaData();
        List<DataAccessSourceTableDTO> collect = dataAccessMetaData.data.stream()
                .filter(d->!("sftp").equals(d.driveType))
                .filter(d->!("ftp").equals(d.driveType))
                .collect(Collectors.toList());
        if (dataAccessMetaData.code != ResultEnum.SUCCESS.getCode()) {
            log.error("【获取接入所有表失败】");
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        //获取接入所有应用
        ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAppRegistration = dataAccessClient.synchronizationAppRegistration();
        if (synchronizationAppRegistration.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }

        if (CollectionUtils.isEmpty(synchronizationAppRegistration.data)
                || CollectionUtils.isEmpty(collect)) {
            return;
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
            if(("sftp").equals(accessTable.driveType)||("ftp").equals(accessTable.driveType)){
                continue;
            }else{
                log.debug("accessTable日志"+accessTable);
                log.debug("accessTable信息:表名称："+accessTable.tableName+",表ID"+accessTable.id+",表脚本"+accessTable.sqlScript);
                res = SqlParserUtils.sqlDriveConversionName(accessTable.appId,accessTable.driveType,accessTable.sqlScript);
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
                tableList.add(table);
            }
            first.get().dbList.get(0).tableList.addAll(tableList);
        }

        metaData.consumeMetaData(synchronizationAppRegistration.data,currUserName);

    }
    /**
     * 同步数据接入STG到ODS的元数据
     * @param currUserName  当前执行账号
     */
    private void synchronousAccessTableSourceMetaData(String currUserName) {
        ResultEntity<List<MetaDataInstanceAttributeDTO>> accessTable = dataAccessClient.synchronizationAccessTable();
        if (accessTable.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        metaData.consumeMetaData(accessTable.data,currUserName);
    }
    /**
     * 同步数仓建模的元数据
     * @param currUserName 当前执行账号
     */
    public void synchronousDataModelTableSourceMetaData(String currUserName) {
        ResultEntity<List<MetaDataInstanceAttributeDTO>> dataModelMetaData = dataModelClient.getDataModelMetaData();
        if (dataModelMetaData.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        metaData.consumeMetaData(dataModelMetaData.data,currUserName);
    }
    //endregion
}
