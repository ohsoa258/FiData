package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.SimilarityExtendPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验模块下相似度组件扩展属性Mapper
 * @date 2022/4/2 11:16
 */
@Mapper
public interface SimilarityExtendMapper extends FKBaseMapper<SimilarityExtendPO> {
    /**
     * 修改有效性
     *
     * @return 执行结果
     */
    @Update("UPDATE tb_similarity_extend_module SET del_flag = 0 WHERE datacheck_id= #{datacheckId};")
    int updateByDatacheckId(int datacheckId);
}
