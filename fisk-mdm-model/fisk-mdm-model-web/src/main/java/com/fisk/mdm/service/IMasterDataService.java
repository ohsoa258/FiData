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
     * 得到所有
     *
     * @param entityId       实体id
     * @param modelVersionId 模型版本id
     * @return {@link ResultObjectVO}
     */
    ResultObjectVO getAll(Integer entityId,Integer modelVersionId);
}
