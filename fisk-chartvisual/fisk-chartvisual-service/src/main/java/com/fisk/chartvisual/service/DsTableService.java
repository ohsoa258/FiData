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
    ResultEntity<DsTableDTO> getTableInfo(Integer id);

    /**
     * 获取预览表数据
     * @param dto
     * @return
     */
    ResultEntity<List<Map<String, Object>>> getData(ObtainTableDataDTO dto);

    /**
     * 获取表字段结构
     * @param dto
     * @return
     */
    ResultEntity<List<FieldInfoDTO>> getTableStructure(TableStructureDTO dto);

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
    ResultEntity<List<SaveDsTableDTO>> selectByDataSourceId(Integer dataSourceId);
}
