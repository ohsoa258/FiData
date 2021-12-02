package com.fisk.datamodel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.QueryDTO;
import com.fisk.datamodel.dto.dimension.DimensionSqlDTO;
import com.fisk.datamodel.dto.fact.FactDTO;
import com.fisk.datamodel.dto.fact.FactDropDTO;
import com.fisk.datamodel.dto.fact.FactListDTO;
import com.fisk.datamodel.dto.fact.FactScreenDropDTO;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IFact {

    /**
     * 添加事实表
     * @param dto
     * @return
     */
    ResultEnum addFact(FactDTO dto);

    /**
     * 删除事实表
     * @param id
     * @return
     */
    ResultEnum deleteFact(int id);

    /**
     * 根据id获取事实表详情
     * @param id
     * @return
     */
    FactDTO getFact(int id);

    /**
     * 编辑事实表
     * @param dto
     * @return
     */
    ResultEnum updateFact(FactDTO dto);

    /**
     * 获取事实表列表
     * @param dto
     * @return
     */
    IPage<FactListDTO> getFactList(QueryDTO dto);

    /**
     * 获取事实表下拉列表
     * @return
     */
    List<FactDropDTO> getFactDropList();

    /**
     * 获取事实筛选列表
     * @return
     */
    List<FactScreenDropDTO> getFactScreenDropList();

    /**
     * 更新事实脚本数据
     * @param dto
     * @return
     */
    ResultEnum updateFactSql(DimensionSqlDTO dto);

    /**
     * 根据事实id,更改发布状态
     * @param dto
     */
    void updateFactPublishStatus(ModelPublishStatusDTO dto);

}
