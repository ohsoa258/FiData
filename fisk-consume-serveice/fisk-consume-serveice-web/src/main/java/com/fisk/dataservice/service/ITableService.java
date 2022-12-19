package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.datasource.DataSourceConfigInfoDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageDataDTO;
import com.fisk.dataservice.dto.tableservice.TableServicePageQueryDTO;
import com.fisk.dataservice.dto.tableservice.TableServiceSaveDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface ITableService {

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    Page<TableServicePageDataDTO> getTableServiceListData(TableServicePageQueryDTO dto);

    /**
     * 新增表
     *
     * @param dto
     * @return
     */
    ResultEntity<Object> addTableServiceData(TableServiceDTO dto);

    /**
     * 获取数据源配置
     *
     * @return
     */
    List<DataSourceConfigInfoDTO> getDataSourceConfig();

    /**
     * 表服务保存
     *
     * @param dto
     * @return
     */
    ResultEnum TableServiceSave(TableServiceSaveDTO dto);

}
