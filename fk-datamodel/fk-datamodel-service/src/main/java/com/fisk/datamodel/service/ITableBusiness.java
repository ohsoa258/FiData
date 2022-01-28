package com.fisk.datamodel.service;

import com.fisk.datamodel.dto.syncmode.GetTableBusinessDTO;

/**
 * @author JianWenYang
 */
public interface ITableBusiness {

    /**
     * 根据维度/事实id和类型,获取增量配置
     * @param tableId
     * @param tableType
     * @return
     */
    GetTableBusinessDTO getTableBusiness(int tableId,int tableType);

}
