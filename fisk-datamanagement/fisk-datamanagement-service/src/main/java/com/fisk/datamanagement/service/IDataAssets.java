package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.dataassets.DataAssetsParameterDTO;
import com.fisk.datamanagement.dto.dataassets.DataAssetsResultDTO;

/**
 * @author JianWenYang
 */
public interface IDataAssets {

    /**
     * 获取数据资产表数据集合
     * @param dto
     * @return
     */
    DataAssetsResultDTO getDataAssetsTableList(DataAssetsParameterDTO dto);

}
