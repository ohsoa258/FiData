package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.datacheck.SimilarityExtendDTO;
import com.fisk.datagovernance.entity.dataquality.SimilarityExtendPO;
import com.fisk.datagovernance.entity.dataquality.TemplatePO;
import com.fisk.datagovernance.enums.dataquality.TemplateTypeEnum;
import com.fisk.datagovernance.map.dataquality.SimilarityExtendMap;
import com.fisk.datagovernance.mapper.dataquality.SimilarityExtendMapper;
import com.fisk.datagovernance.service.dataquality.ISimilarityExtendManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验模块下相似度组件扩展属性
 * @date 2022/4/2 11:23
 */
@Service
public class SimilarityExtendManageImpl extends ServiceImpl<SimilarityExtendMapper, SimilarityExtendPO> implements ISimilarityExtendManageService {
    @Resource
    private SimilarityExtendManageImpl similarityExtendManageImpl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveBatchById(List<SimilarityExtendDTO> dto, long datacheckId, boolean isDel) {
     int datacheckIdVal= Math.toIntExact(datacheckId);
        if (CollectionUtils.isNotEmpty(dto)) {
            if (isDel){
                baseMapper.updateByDatacheckId(datacheckIdVal);
            }
            dto.forEach(m -> {
                m.datacheckId = datacheckIdVal;
            });
            List<SimilarityExtendPO> similarityExtendPOS = SimilarityExtendMap.INSTANCES.dtoToPo(dto);
            if (CollectionUtils.isNotEmpty(similarityExtendPOS)) {
                similarityExtendManageImpl.saveBatch(similarityExtendPOS);
            }
        }
        return ResultEnum.SUCCESS;
    }
}
