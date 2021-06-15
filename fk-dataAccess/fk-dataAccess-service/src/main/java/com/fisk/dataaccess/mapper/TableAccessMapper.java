package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.dataaccess.dto.TablePhyHomeDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author: Lock
 */
@Mapper
public interface TableAccessMapper extends BaseMapper<TableAccessPO> {

    @Select("SELECT \n" +
            "    a.id,\n" +
            "    a.table_name,\n" +
            "    a.table_des,\n" +
            "    f.update_time,\n" +
            "\t\tf.field_name,\n" +
            "\t\ts.sync_mode\n" +
            "FROM (\n" +
            "`tb_table_access` a\n" +
            "LEFT JOIN `tb_table_fields` AS f\n" +
            "ON a.id = f.id\n" +
            ")LEFT JOIN `tb_table_syncmode` s\n" +
            "ON s.id=a.appid\n" +
            "WHERE a.del_flag=1\n" +
            "AND table_name LIKE '%input%'")
//    List<TablePhyHomeDTO> queryByPage(@Param("key") String key);
    List<Map<String, Object>> queryByPage(Page<Map<String,Object>> page, String key);

}
