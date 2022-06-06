package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.datareview.DataReviewPageDTO;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import com.fisk.dataaccess.entity.TableFieldsPO;
import com.fisk.dataaccess.vo.datareview.DataReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Lock
 */
@Mapper
public interface TableFieldsMapper extends FKBaseMapper<TableFieldsPO> {

    /**
     * 根据tb_access_id获取表字段及id
     * @param id tb_access_id
     * @return 表字段及id
     */
    @Select("SELECT id,field_name,field_des,field_type,field_length FROM tb_table_fields WHERE table_access_id = #{id} AND del_flag = 1;")
    List<FieldNameDTO> listTableName(@Param("id") long id);

    /**
     * 筛选器
     * @param page 分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<DataReviewVO> filter(Page<DataReviewVO> page, @Param("query") DataReviewPageDTO query);
}
