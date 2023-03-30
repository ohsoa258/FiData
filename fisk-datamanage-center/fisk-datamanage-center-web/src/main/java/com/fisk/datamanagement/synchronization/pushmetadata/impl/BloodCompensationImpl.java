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
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
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

    @Resource
    DataAccessClient dataAccessClient;
    @Resource
    DataModelClient dataModelClient;

    @Resource
    MetaDataImpl metaData;
    @Resource
    ClassificationImpl classification;


    @Override
    public ResultEnum systemSynchronousBlood(String currUserName) {

        log.info("********开始同步数据接入********");

        //获取接入业务分类
        ResultEntity<List<AppBusinessInfoDTO>> appList = dataAccessClient.getAppList();
        if (appList.code != ResultEnum.SUCCESS.getCode()) {
            log.error("获取接入应用列表失败");
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }

        //同步数据接入业务分类
        if (CollectionUtils.isEmpty(appList.data)) {
            return ResultEnum.SUCCESS;
        }
        log.info("******开始同步接入业务分类******");
        synchronousClassification(appList.data, 1);

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

        log.info("******开始同步数据接入来源表元数据******");
        //同步数据接入来源表元数据(解析接入表sql)
        synchronousAccessSourceMetaData(collect,currUserName);

        log.info("******开始同步数据接入ods表以及stg表元数据******");
        //同步数据接入ods表以及stg表元数据
        synchronousAccessTableSourceMetaData(currUserName);

        log.info("********开始同步数据建模********");

        ResultEntity<List<AppBusinessInfoDTO>> businessAreaList = dataModelClient.getBusinessAreaList();
        log.info("********开始同步数据建模********:{}",businessAreaList);
        if (businessAreaList.code != ResultEnum.SUCCESS.getCode()) {
            log.error("【获取建模业务域数据失败】");
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }

        log.info("********开始同步建模业务分类********");
        if (CollectionUtils.isEmpty(businessAreaList.data)) {
            return ResultEnum.SUCCESS;
        }
        synchronousClassification(businessAreaList.data, 2);

        log.info("********开始同步建模业务分类********");
        synchronousDataModelTableSourceMetaData(currUserName);

        return ResultEnum.SUCCESS;
    }

    /**
     * 同步业务分类
     *
     * @param dtoList
     */
    public void synchronousClassification(List<AppBusinessInfoDTO> dtoList, Integer sourceType) {
        for (AppBusinessInfoDTO item : dtoList) {
            ClassificationInfoDTO classificationInfoDto = new ClassificationInfoDTO();
            classificationInfoDto.setName(item.name);
            classificationInfoDto.setDescription(item.appDes);
            classificationInfoDto.setSourceType(sourceType);
            classificationInfoDto.setDelete(false);
            try {
                classification.appSynchronousClassification(classificationInfoDto);
            } catch (Exception e) {
                log.error("【血缘补偿,同步业务分类失败】,分类名称:{}", item.name);
                continue;
            }
        }
    }

    /**
     * 同步接入来源表元数据
     *
     * @return
     */
    public void synchronousAccessSourceMetaData(List<DataAccessSourceTableDTO> dataAccessMetaData,String currUserName) {
        //获取接入所有应用
        ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAppRegistration = dataAccessClient.synchronizationAppRegistration();
        if (synchronizationAppRegistration.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }

        if (CollectionUtils.isEmpty(synchronizationAppRegistration.data)
                || CollectionUtils.isEmpty(dataAccessMetaData)) {
            return;
        }

        for (DataAccessSourceTableDTO accessTable : dataAccessMetaData) {


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

    public void synchronousAccessTableSourceMetaData(String currUserName) {
        ResultEntity<List<MetaDataInstanceAttributeDTO>> accessTable = dataAccessClient.synchronizationAccessTable();
        if (accessTable.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        metaData.consumeMetaData(accessTable.data,currUserName);
    }

    public void synchronousDataModelTableSourceMetaData(String currUserName) {
        ResultEntity<List<MetaDataInstanceAttributeDTO>> dataModelMetaData = dataModelClient.getDataModelMetaData();
        if (dataModelMetaData.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }
        metaData.consumeMetaData(dataModelMetaData.data,currUserName);
    }

}
