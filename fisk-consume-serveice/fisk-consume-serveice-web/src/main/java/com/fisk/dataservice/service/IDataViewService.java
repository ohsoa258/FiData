package com.fisk.dataservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.baseObject.dto.PageDTO;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.fisk.dataservice.dto.dataanalysisview.*;
import com.fisk.dataservice.entity.DataViewPO;
import com.fisk.dataservice.entity.ViewFieldsPO;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
public interface IDataViewService extends IService<DataViewPO> {

    /**
     * 分页获取数据
     * @param viewThemeId
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageDTO<DataViewDTO> getViewList(Integer viewThemeId, Integer pageNum, Integer pageSize);

    /**
     *
     * @param viewThemeId
     * @return
     */
    DataSourceViewDTO getDataSourceMeta(Integer viewThemeId);

    /**
     * 查询数据库表中的字段结构信息
     * @param viewThemeId
     * @param tableName
     * @param queryType
     * @return
     */
    List<TableStructureDTO> getSourceColumnMeta(Integer viewThemeId, String tableName, Integer queryType);

    /**
     * 查询sql执行后的结果数据
     * @param dto
     * @return
     */
    OdsResultDTO getDataAccessQueryList(SelSqlResultDTO dto);

    /**
     * 新增数据视图
     * @param dto
     * @return
     */
    ResultEnum addDataView(SaveDataViewDTO dto);

    /**
     * 删除数据视图
     *
     * @param viewId
     * @return
     */
    ResultEnum removeDataView(Integer viewId);

    /**
     * 修改数据视图
     * @param dto
     * @return
     */
    ResultEnum editDataView(EditDataViewDTO dto);

    /**
     * 获取数据视图字段信息
     * @param viewId
     * @return
     */
    List<DataViewFieldsDTO> getViewTableFields(Integer viewId);

    /**
     * 修改数据视图及sql执行内容
     * @param dto
     * @return
     */
    ResultEnum updateDataView(UpdateDataViewDTO dto);
}
