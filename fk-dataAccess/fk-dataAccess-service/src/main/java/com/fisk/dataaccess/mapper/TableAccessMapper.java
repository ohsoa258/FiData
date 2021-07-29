package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.TableAccessPageDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.vo.TableAccessVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Mapper
public interface TableAccessMapper extends FKBaseMapper<TableAccessPO> {


/*    @Select("SELECT \n" +
            "    a.id,\n" +
            "    a.table_name,\n" +
            "    a.table_des,\n" +
            "    a.update_time,\n" +
            "\t\tf.field_name,\n" +
            "\t\ts.sync_mode\n" +
            "FROM (\n" +
            "`tb_table_access` a\n" +
            "LEFT JOIN `tb_table_fields` AS f\n" +
            "ON a.id = f.id\n" +
            ")LEFT JOIN `tb_table_syncmode` s\n" +
            "ON s.id=a.appid\n" +
            "WHERE table_name like concat('%',#{key},'%')\n" +
            "AND a.del_flag = 1 " +
            "ORDER BY a.update_time DESC")*/

    /**
     * 分页查询
     *
     * @param page 当前页
     * @param key  每页显示条数
     * @return 返回值
     */
    @Select("select a.id,\n" +
            "a.`table_name`,\n" +
            "a.table_des,\n" +
            "a.update_time,\n" +
            "b.`sync_field`\n" +
            "from tb_table_access a\n" +
            "left join `tb_table_syncmode` as b\n" +
            "on a.id = b.id\n" +
            "WHERE a.table_name like concat('%',#{key},'%')\n" +
            "AND a.del_flag = 1 " +
            "ORDER BY a.update_time DESC")
    List<Map<String, Object>> queryByPage(Page<Map<String, Object>> page, @Param("key") String key);


    /**
     * 查询表中所有的table_name(未删除的)
     *
     * @return 返回值
     */
    @Select("select table_name from tb_table_access where del_flag=1")
    List<String> getTableName();



    /**
     * 筛选器分页功能
     *
     * @param page  分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<TableAccessVO> filter(Page<TableAccessVO> page, @Param("query") TableAccessPageDTO query);
}
