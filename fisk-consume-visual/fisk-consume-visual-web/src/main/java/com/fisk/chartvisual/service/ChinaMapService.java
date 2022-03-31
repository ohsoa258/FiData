package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.ChinaMapDTO;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/10/28 17:19
 */
public interface ChinaMapService {

    /**
     * 获取中国地图省信息
     * @return
     */
    List<ChinaMapDTO> getAll();
}
