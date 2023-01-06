package com.fisk.datamanagement.synchronization.pushmetadata.impl;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataTableAttributeDTO;
import com.fisk.common.service.sqlparser.SqlParserUtils;
import com.fisk.common.service.sqlparser.model.TableMetaDataObject;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.datamanagement.synchronization.pushmetadata.IBloodCompensation;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author JianWenYang
 */
@Service
public class BloodCompensationImpl
        implements IBloodCompensation {

    @Resource
    DataAccessClient dataAccessClient;

    @Resource
    MetaDataImpl metaData;

    @Override
    public ResultEnum systemSynchronousBlood() {

        //获取所有接入表
        ResultEntity<List<DataAccessSourceTableDTO>> dataAccessMetaData = dataAccessClient.getDataAccessMetaData();
        if (dataAccessMetaData.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }

        //同步接入来源表元数据
        synchronousAccessSourceMetaData(dataAccessMetaData.data);


        return ResultEnum.SUCCESS;
    }

    /**
     * 同步接入来源表元数据
     *
     * @return
     */
    public ResultEnum synchronousAccessSourceMetaData(List<DataAccessSourceTableDTO> dataAccessMetaData) {
        //获取接入所有应用
        ResultEntity<List<MetaDataInstanceAttributeDTO>> synchronizationAppRegistration = dataAccessClient.synchronizationAppRegistration();
        if (synchronizationAppRegistration.code != ResultEnum.SUCCESS.getCode()) {
            throw new FkException(ResultEnum.VISUAL_QUERY_ERROR);
        }

        if (CollectionUtils.isEmpty(synchronizationAppRegistration.data)
                || CollectionUtils.isEmpty(dataAccessMetaData)) {
            return ResultEnum.SUCCESS;
        }

        for (DataAccessSourceTableDTO accessTable : dataAccessMetaData) {

            Optional<MetaDataInstanceAttributeDTO> first = synchronizationAppRegistration
                    .data.stream().filter(e -> e.comment.equals(String.valueOf(accessTable.appId))).findFirst();
            if (!first.isPresent()) {
                continue;
            }

            //解析sql
            List<TableMetaDataObject> res = SqlParserUtils.sqlDriveConversionName(accessTable.driveType, accessTable.sqlScript);
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

            first.get().dbList.get(0).tableList = tableList;
        }

        return metaData.consumeMetaData(synchronizationAppRegistration.data);

    }

}
