package com.fisk.datamodel.service;

import com.fisk.common.response.ResultEnum;
import com.fisk.datamodel.dto.modelpublish.ModelPublishStatusDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableConfigDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableFieldConfigDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableListDTO;
import com.fisk.datamodel.dto.widetableconfig.WideTableQueryPageDTO;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface IWideTable {

    /**
     * 根据业务域id，获取宽表列表
     * @param businessId
     * @return
     */
    List<WideTableListDTO> getWideTableList(int businessId);

    /**
     * 执行宽表SQL
     * @param dto
     * @return
     */
    WideTableQueryPageDTO executeWideTableSql(WideTableFieldConfigDTO dto);

    /**
     * 添加宽表
     * @param dto
     * @return
     */
    ResultEnum addWideTable(WideTableConfigDTO dto);

    /**
     * 获取宽表详情
     * @param id
     * @return
     */
    WideTableConfigDTO getWideTable(int id);

    /**
     * 修改宽表
     * @param dto
     * @return
     */
    ResultEnum updateWideTable(WideTableConfigDTO dto);

    /**
     * 删除宽表
     * @param id
     * @return
     */
    ResultEnum deleteWideTable(int id);

    /**
     * 更改宽表doris发布状态
     * @param dto
     */
    void updateWideTablePublishStatus(ModelPublishStatusDTO dto);


}
