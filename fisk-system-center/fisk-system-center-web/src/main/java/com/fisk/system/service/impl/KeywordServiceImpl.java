package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.system.dto.KeywordTypeDTO;
import com.fisk.system.entity.KeywordPO;
import com.fisk.system.mapper.KeywordMapper;
import com.fisk.system.service.IKeywordService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lock
 */
@Service
public class KeywordServiceImpl extends ServiceImpl<KeywordMapper, KeywordPO> implements IKeywordService {

    @Override
    public List<String> getList(KeywordTypeDTO dto) {

        QueryWrapper<KeywordPO> queryWrapper = new QueryWrapper<>();

        dto.keywordType.forEach(item -> queryWrapper.lambda().eq(KeywordPO::getKeywordType,item).or().select(KeywordPO::getKeyword));

        List<KeywordPO> poList = baseMapper.selectList(queryWrapper);
        List<String> list = new ArrayList<>();
        poList.forEach(e -> list.add(e.keyword));

        return list;
    }
}