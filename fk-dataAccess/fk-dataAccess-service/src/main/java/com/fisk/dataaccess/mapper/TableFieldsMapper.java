package com.fisk.dataaccess.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.FieldNameDTO;
import com.fisk.dataaccess.entity.TableFieldsPO;
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
    @Select("SELECT id,field_name FROM tb_table_fields WHERE table_access_id = #{id} AND del_flag = 1;")
    List<FieldNameDTO> listTableName(@Param("id") long id);

}
