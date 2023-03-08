package com.fisk.datamanagement.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.label.LabelDataDTO;
import com.fisk.datamanagement.dto.label.LabelInfoDTO;
import com.fisk.datamanagement.entity.LabelPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author JianWenYang
 */
public interface LabelMapper extends FKBaseMapper<LabelPO> {

    /**
     * 根据类目id集合获取标签列表
     *
     * @param page
     * @param categoryIds
     * @return
     */
    Page<LabelDataDTO> queryPageList(Page<LabelDataDTO> page, @Param("categoryIds") String categoryIds);

    /**
     * 获取所有标签集合
     *
     * @return
     */
    @Select("select id,label_cn_name from tb_label where del_flag = 1  order by create_time desc ")
    List<LabelInfoDTO> getLabelList();

}
