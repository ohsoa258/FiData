package com.fisk.datagovernance.mapper.dataquality;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗mapper
 * @date 2022/3/23 12:42
 */
@Mapper
public interface BusinessFilterMapper extends FKBaseMapper<BusinessFilterPO> {
    /**
     * 查询数据校验分页列表
     *
     * @param page    分页信息
     * @param keyword where条件
     * @param tableUnique 表名称/Id
     * @return 查询结果
     */
    Page<BusinessFilterVO> getAll(Page<BusinessFilterVO> page,
                                  @Param("datasourceId") int datasourceId,
                                  @Param("tableUnique") String tableUnique,
                                  @Param("keyword") String keyword);
}
