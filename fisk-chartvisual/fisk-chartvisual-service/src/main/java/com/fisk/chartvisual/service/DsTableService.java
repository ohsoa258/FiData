package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.*;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;

import java.util.List;
import java.util.Map;

/**
 * @author WangYan
 * @date 2022/3/4 11:22
 */
public interface DsTableService {

    /**
     * 根据数据源连接获取表名
     * @param id
     * @return
     */
    List<DsTableDTO> getTableInfo(Integer id);

    /**
     * 获取预览表数据
     * @param dto
     * @return
     */
    List<Map<String, Object>> getData(ObtainTableDataDTO dto);

    /**
     * 获取字段结构库里否存在
     * @param dto
     * @return
     */
    List<TableInfoDTO> getTableStructure(TableStructureDTO dto);

    /**
     * 保存表信息
     * @param dsTableDto
     * @return
     */
    ResultEntity<ResultEnum> saveTableInfo(SaveDsTableDTO dsTableDto);

    /**
     * 根据数据源id查询表字段
     * @param dataSourceId
     * @return
     */
    List<SaveDsTableDTO> selectByDataSourceId(Integer dataSourceId);

    /**
     * 根据数据源连接获取表名状态
     * @param id
     * @return
     */
    List<ShowDsTableDTO> getTableInfoStatus(Integer id);

    /**
     * 修改表信息
     * @param dto
     * @return
     */
    ResultEntity<ResultEnum> updateTableInfo(List<UpdateDsTableDTO> dto);
}
