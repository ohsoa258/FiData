package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.fact.*;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.entity.fact.FactPO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IFact extends IService<FactPO> {

    /**
     * 添加事实表
     *
     * @param dto
     * @return
     */
    ResultEnum addFact(FactDTO dto);

    /**
     * 删除事实表
     *
     * @param id
     * @return
     */
    ResultEnum deleteFact(int id);

    /**
     * 根据id获取事实表详情
     *
     * @param id
     * @return
     */
    FactDTO getFact(int id);

    /**
     * 编辑事实表
     *
     * @param dto
     * @return
     */
    ResultEnum updateFact(FactDTO dto);

    /**
     * 获取事实表列表
     *
     * @param dto
     * @return
     */
    IPage<FactListDTO> getFactList(QueryDTO dto);

    /**
     * 获取事实表下拉列表
     *
     * @return
     */
    List<FactDropDTO> getFactDropList();

    /**
     * 获取事实筛选列表
     *
     * @return
     */
    List<FactScreenDropDTO> getFactScreenDropList();

    /**
     * 更新事实脚本数据
     *
     * @param dto
     * @return
     */
    ResultEnum updateFactSql(DimensionSqlDTO dto);

    /**
     * 根据事实id,更改发布状态
     *
     * @param dto
     */
    void updateFactPublishStatus(ModelPublishStatusDTO dto);

    /**
     * 获取事实表发布成功数据
     *
     * @param businessId
     * @return
     */
    List<TableNameDTO> getPublishSuccessFactTable(Integer businessId);

    /**
     * 获取业务域下的事实表计数
     *
     * @return
     */
    Integer getFactCountByBid(Integer id);

    /**
     * 获取所有事实表计数
     *
     * @return
     */
    Integer getFactTotalCount();

    /**
     * 获取事实tree
     *
     * @return
     */
    List<FactTreeDTO> getFactTree();
}
