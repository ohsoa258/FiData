package com.fisk.dataaccess.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataTableMetaDataReqDTO;
import com.fisk.dataaccess.dto.access.ExportCdcConfigDTO;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.dataaccess.dto.table.TableAccessDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.entity.TableAccessPO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Lock
 * @version 2.0
 * @description
 * @date 2022/1/6 15:16
 */
public interface IDataAccess extends IService<TableAccessPO> {

    /**
     * 获取数据接入已发布的元数据对象
     *
     * @return 元数据对象
     */
    ResultEntity<List<DataAccessSourceTableDTO>> getDataAccessMetaData();

    /**
     * 通过名称查询数据接入已发布的元数据对象
     * @return
     */
    ResultEntity<DataAccessSourceTableDTO> getDataAccessMetaDataByTableName(String tableName);

    /**
     * 构建元数据查询对象(表及下面的字段)
     *
     * @param dto dto
     * @return 元数据对象
     */
    List<FiDataTableMetaDataDTO> buildFiDataTableMetaData(FiDataTableMetaDataReqDTO dto);

    /**
     * 数据湖管理-导出配置数据
     *
     * @param dto
     * @return
     */
    Object exportCdcConfig(ExportCdcConfigDTO dto);

    /**
     * 根据应用id获取应用下的表名称和表id
     *
     * @param appId
     * @return
     */
    List<TableAccessDTO> getTblsByAppId(Integer appId);

}
