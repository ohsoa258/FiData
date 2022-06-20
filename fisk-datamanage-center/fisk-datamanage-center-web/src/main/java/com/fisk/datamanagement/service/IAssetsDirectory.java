package com.fisk.datamanagement.service;

import com.fisk.datamanagement.dto.assetsdirectory.AssetsDirectoryDTO;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-06-20 14:32
 */
public interface IAssetsDirectory {

    /**
     * 资产目录数据
     *
     * @return
     */
    List<AssetsDirectoryDTO> assetsDirectoryData();

}
