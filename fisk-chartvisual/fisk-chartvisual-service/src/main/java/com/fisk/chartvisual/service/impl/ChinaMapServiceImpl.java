package com.fisk.chartvisual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.chartvisual.dto.ChinaMapDTO;
import com.fisk.chartvisual.entity.ProvincialAmountPO;
import com.fisk.chartvisual.entity.ProvincialPO;
import com.fisk.chartvisual.mapper.ProvincialAmountMapper;
import com.fisk.chartvisual.mapper.ProvincialMapper;
import com.fisk.chartvisual.service.ChinaMapService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author WangYan
 * @date 2021/10/28 17:20
 */
@Service
public class ChinaMapServiceImpl implements ChinaMapService {

    @Resource
    ProvincialMapper provincialMapper;
    @Resource
    ProvincialAmountMapper amountMapper;

    @Override
    public List<ChinaMapDTO> getAll() {
        List<ProvincialPO> provincialList = provincialMapper.selectList(null);

        return provincialList.stream().map(e -> {
            ChinaMapDTO dto = new ChinaMapDTO();
            dto.setId((int)e.getId());
            dto.setName(e.getProvincialName());

            QueryWrapper<ProvincialAmountPO> query = new QueryWrapper<>();
            query.lambda()
                    .eq(ProvincialAmountPO::getProvincialId,(int)e.getId())
                    .select(ProvincialAmountPO::getSalesAmount);
            ProvincialAmountPO amount = amountMapper.selectOne(query);

            if (amount == null) {
                dto.setValue("0");
            }else {
                dto.setValue(amount.getSalesAmount());
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
