package com.fisk.mdm.service;

import com.fisk.mdm.vo.resultObject.ResultObjectVO;

import java.util.List;
import java.util.Map;

/**
 * 主数据服务
 *
 * @author ChenYa
 * @date 2022/04/25
 */
public interface IMasterDataService {
    /**
     * 根据code查询主数据
     *
     * @param entityId 实体id
     * @param code     代码
     * @return {@link Map}<{@link String}, {@link Object}>
     */
//    Map<String,Object> getByCode(Integer entityId,Integer code);

    ResultObjectVO getAll(Integer entityId);
}
