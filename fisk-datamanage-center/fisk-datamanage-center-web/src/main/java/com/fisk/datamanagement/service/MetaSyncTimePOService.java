package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.enums.datamanage.ClassificationTypeEnum;
import com.fisk.datamanagement.dto.metasynctime.ClassificationTypeDTO;
import com.fisk.datamanagement.dto.metasynctime.EntityTotalNumDTO;
import com.fisk.datamanagement.dto.metasynctime.MetaSyncDTO;
import com.fisk.datamanagement.entity.MetaSyncTimePO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 56263
* @description 针对表【tb_meta_sync_time】的数据库操作Service
* @createDate 2024-05-17 09:57:37
*/
public interface MetaSyncTimePOService extends IService<MetaSyncTimePO> {


    /**
     * 获取服务类型树
     *
     * @return
     */
    List<ClassificationTypeDTO> getServiceType();

    /**
     * 根据服务类型获取服务的元数据同步日志 分页
     *
     * @param type
     * @return
     */
    Page<MetaSyncDTO> getMetaSyncLogByType(ClassificationTypeEnum type, Integer current, Integer size);

    /**
     * 资产全景图 获取资产目录趋势分析（近七天）
     *
     * @return
     */
    List<EntityTotalNumDTO> getAssetCatalogTrendAnalysis();

}
