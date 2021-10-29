package com.fisk.chartvisual.service.impl;

import com.fisk.chartvisual.dto.ChinaMapDTO;
import com.fisk.chartvisual.entity.ProvincialPO;
import com.fisk.chartvisual.map.ChinaMap;
import com.fisk.chartvisual.mapper.ProvincialMapper;
import com.fisk.chartvisual.service.ChinaMapService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/10/28 17:20
 */
@Service
public class ChinaMapServiceImpl implements ChinaMapService {

    @Resource
    ProvincialMapper provincialMapper;

    @Override
    public List<ChinaMapDTO> getAll() {
        List<ProvincialPO> provincialList = provincialMapper.selectList(null);

        List<ChinaMapDTO> dtoList = new ArrayList<>();
        for (ProvincialPO provincialPO : provincialList) {
            dtoList.add(ChinaMap.INSTANCES.poToDto(provincialPO));
        }

        return dtoList;
    }
}
