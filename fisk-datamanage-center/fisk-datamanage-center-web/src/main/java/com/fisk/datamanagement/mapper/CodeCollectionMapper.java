package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.datamanagement.dto.DataSet.CodeCollectionQueryDTO;
import com.fisk.datamanagement.entity.CodeCollectionPO;
import com.fisk.datamanagement.vo.CodeCollectionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2024-08-01 14:37:05
 */
@Mapper
public interface CodeCollectionMapper extends BaseMapper<CodeCollectionPO> {

    Integer getAllCodeCollectionCount();

    List<CodeCollectionVO> getCodeCollection(@Param("keyword") String keyword,
                                             @Param("startIndex") Integer startIndex,
                                             @Param("pageSize") Integer pageSize);

    List<CodeCollectionVO> getCollectionList();
}
