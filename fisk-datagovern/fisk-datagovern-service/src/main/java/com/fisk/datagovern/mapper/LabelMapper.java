package com.fisk.datagovern.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovern.dto.label.LabelDataDTO;
import com.fisk.datagovern.entity.LabelPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface LabelMapper extends FKBaseMapper<LabelPO> {

    /**
     * 根据类目id集合获取标签列表
     * @param page
     * @param categoryIds
     * @return
     */
    Page<LabelDataDTO> queryPageList(Page<LabelDataDTO> page,@Param("categoryIds") String categoryIds);

}
