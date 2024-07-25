package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableColumnDTO;
import com.fisk.common.service.dbMetaData.dto.ColumnQueryDTO;
import com.fisk.common.service.dbMetaData.dto.FiDataMetaDataTreeDTO;
import com.fisk.datamanagement.dto.category.CategoryQueryDTO;
import com.fisk.datamanagement.dto.classification.BusinessTargetinfoMenuDTO;
import com.fisk.datamanagement.dto.standards.*;
import com.fisk.datamanagement.entity.StandardsPO;
import org.springframework.web.multipart.MultipartFile;

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

    Page<StandardsMenuDTO> standardsQuery(StandardsQueryDTO dto);

    List<StandardsDTO> getStandardsBySource(Integer fieldMetadataId);

    ResultEnum importExcelStandards(long menuId,MultipartFile file);

    List<FiDataMetaDataTreeDTO> getAllStandardsTree(String id);

    /**
     * 数仓建模-关联字段和数据源标准
     * @param dtos
     * @return
     */
    Object setStandardsByModelField(List<StandardsBeCitedDTO> dtos);

    /**
     * 数仓建模-获取所有数据元标准 只获取数据元id 和中文名、menuid
     * @return
     */
    List<StandardsDTO> modelGetStandards();

    /**
     * 数仓建模-获取所有数仓字段和数据元标准的关联关系
     * @return
     */
    List<StandardsBeCitedDTO> modelGetStandardsMap();

    List<SearchColumnDTO> searchStandardBeCitedField(String key);

    /**
     * 获取数据元标准数量
     * @return
     */
    Integer getStandardTotal();
}

