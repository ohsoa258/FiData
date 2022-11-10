package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.enums.fidatadatasource.DataSourceConfigEnum;
import com.fisk.common.core.enums.fidatadatasource.TableBusinessTypeEnum;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.server.metadata.BusinessMetaDataInfoDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataDTO;
import com.fisk.datagovernance.dto.dataquality.datasource.DataTableFieldDTO;
import com.fisk.datagovernance.enums.dataquality.SourceTypeEnum;
import com.fisk.datagovernance.vo.dataquality.datasource.DataSourceConVO;
import com.fisk.datamanage.client.DataManageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 调用元数据接口实现类
 * @date 2022/9/22 11:31
 */
@Service
@Slf4j
public class ExternalInterfaceImpl {

    @Resource
    private DataManageClient dataManageClient;

    @Resource
    private DataSourceConManageImpl dataSourceConManageImpl;

    public void synchronousTableBusinessMetaData(int datasourceId, SourceTypeEnum sourceTypeEnum, int tableBusinessType, String tableUnique) {
        if (datasourceId == 0 || sourceTypeEnum == SourceTypeEnum.NONE || StringUtils.isEmpty(tableUnique)) {
            return;
        }
        try {
            String tableName = tableUnique;
            String dbName = "";
            List<DataSourceConVO> allDataSource = dataSourceConManageImpl.getAllDataSource();
            if (CollectionUtils.isEmpty(allDataSource)) {
                return;
            }
            DataSourceConVO dataSourceConVO = allDataSource.stream().filter(t -> t.getId() == datasourceId).findFirst().orElse(null);
            if (dataSourceConVO != null) {
                dbName = dataSourceConVO.getConDbname();
            }
            if (StringUtils.isEmpty(dbName)) {
                return;
            }
            if (sourceTypeEnum == SourceTypeEnum.FiData) {
                List<DataTableFieldDTO> dtoList = new ArrayList<>();
                DataTableFieldDTO dataTableFieldDTO = new DataTableFieldDTO();
                dataTableFieldDTO.setId(tableUnique);
                dataTableFieldDTO.setDataSourceConfigEnum(DataSourceConfigEnum.getEnum(dataSourceConVO.getDatasourceId()));
                dataTableFieldDTO.setTableBusinessTypeEnum(TableBusinessTypeEnum.getEnum(tableBusinessType));
                dtoList.add(dataTableFieldDTO);
                List<FiDataMetaDataDTO> fiDataMetaDatas = dataSourceConManageImpl.getTableFieldName(dtoList);
                if (CollectionUtils.isEmpty(fiDataMetaDatas)) {
                    return;
                }
                if (fiDataMetaDatas.get(0) == null || CollectionUtils.isEmpty(fiDataMetaDatas.get(0).getChildren())) {
                    return;
                }
                tableName = fiDataMetaDatas.get(0).getChildren().get(0).getLabel();
            }
            coll_SynchronousTableBusinessMetaData(dbName, tableName);
        } catch (Exception ex) {
            log.error("同步表级业务元数据异常：" + ex);
        }
    }

    public void coll_SynchronousTableBusinessMetaData(String dbName, String tableName) {
        BusinessMetaDataInfoDTO metaDataInfoDTO = new BusinessMetaDataInfoDTO();
        metaDataInfoDTO.setDbName(dbName);
        metaDataInfoDTO.setTableName(tableName);
        dataManageClient.synchronousTableBusinessMetaData(metaDataInfoDTO);
    }

}
