package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 相似度模板扩展组件
 * @date 2022/4/2 10:58
 */
@Data
@TableName("tb_similarity_extend_module")
public class SimilarityExtendPO extends BasePO {
    /**
     * 数据校验Id
     */
    public int datacheckId;

    /**
     * 字段名称
     */
    public String fieldName;

    /**
     * 权重、比例
     */
    public int scale;
}
