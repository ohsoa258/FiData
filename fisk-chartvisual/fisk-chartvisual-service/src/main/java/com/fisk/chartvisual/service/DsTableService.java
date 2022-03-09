package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.DsTableDTO;
import com.fisk.chartvisual.dto.FieldInfoDTO;
import com.fisk.chartvisual.dto.ObtainTableDataDTO;
import com.fisk.chartvisual.dto.TableStructureDTO;
import com.fisk.common.response.ResultEntity;

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
}
