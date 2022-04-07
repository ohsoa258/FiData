package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.datacheck.SimilarityExtendDTO;
import com.fisk.datagovernance.dto.dataquality.notice.NoticeDTO;
import com.fisk.datagovernance.entity.dataquality.SimilarityExtendPO;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验模块下相似度组件扩展属性
 * @date 2022/4/2 11:21
 */
public interface ISimilarityExtendManageService extends IService<SimilarityExtendPO> {
    /**
     * 保存数据
     *
     * @param dto dto
     * @return 执行结果
     */
    ResultEnum saveBatchById(List<SimilarityExtendDTO> dto,long datacheckId,boolean isDel);
}
