package com.fisk.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.system.dto.KeywordTypeDTO;
import com.fisk.system.entity.KeywordPO;

import java.util.List;

/**
 * @author lock
 */
public interface IKeywordService extends IService<KeywordPO> {

    /**
     * 根据数据源类型查询SQL关键字集合
     *
     * @param dto 请求参数
     * @return 返回值
     */
    List<String> getList(KeywordTypeDTO dto);

    boolean judgeKeyWord(KeywordTypeDTO dto);
}

