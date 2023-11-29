package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.datamanagement.dto.standards.*;
import com.fisk.datamanagement.entity.StandardsPO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 
 *
 * @author wangjian
 * @email fisk@fisksoft.com
 * @date 2023-11-20 13:56:24
 */
public interface StandardsService extends IService<StandardsPO> {

    StandardsDTO getStandards(int id);
    /**
     * 添加数据标准数据
     * @param standardsDTO
     * @return
     */
    ResultEnum addStandards(StandardsDTO standardsDTO);

    /**
     * 更新数据标准数据
     * @param standardsDTO
     * @return
     */
    ResultEnum updateStandards(StandardsDTO standardsDTO);

    /**
     * 删除数据标准数据
     * @param id
     * @return
     */
    ResultEnum delStandards(int id);

    /**
     * 批量删除数据标准数据
     * @param ids
     * @return
     */
    ResultEnum delStandards(List<Integer> ids);

    List<DataSourceInfoDTO> getDataSourceTree();

    QueryResultDTO preview(QueryDTO query);

    List<TableColumnDTO> getColumn(ColumnQueryDTO dto);

    void exportStandards(List<Integer> ids, HttpServletResponse response);

    ResultEnum standardsSort(StandardsSortDTO dto);
}

