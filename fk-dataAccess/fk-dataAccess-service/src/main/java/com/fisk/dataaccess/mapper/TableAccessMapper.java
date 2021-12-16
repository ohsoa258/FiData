package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.TableAccessPageDTO;
import com.fisk.dataaccess.dto.TableNameDTO;
import com.fisk.dataaccess.dto.TableNameTreeDTO;
import com.fisk.dataaccess.dto.datafactory.TableIdAndNameDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.vo.TableAccessVO;
import com.fisk.dataaccess.vo.TableNameVO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
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
     * 查询table_name及对应的appid
     *
     * @return 查询结果
     */
    @Select("select app_id,table_name from tb_table_access where del_flag=1")
    List<TableNameVO> getAppIdAndTableName();

    /**
     * 根据table_name查询appid
     *
     * @param tableName tableName
     * @return 查询结果
     */
    @Select("select app_id from tb_table_access where table_name=#{tableName} and del_flag=1")
    Long getAppIdByTableName(@Param("tableName") String tableName);


    /**
     * 筛选器分页功能
     *
     * @param page  分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<TableAccessVO> filter(Page<TableAccessVO> page, @Param("query") TableAccessPageDTO query);

    /**
     * 获取所有表
     *
     * @return 表名
     */
    @Select("SELECT id,table_name FROM tb_table_access WHERE del_flag = 1;")
    List<TableNameDTO> listTableName();

    /**
     * 根据应用id获取物理表
     *
     * @param id app_id
     * @return 物理表
     */
    @Select("SELECT id,table_name FROM tb_table_access WHERE app_id = #{app_id} and del_flag = 1;")
    List<TableNameDTO> listTableNameByAppId(@Param("app_id") long id);


    /**
     * 物理表tree
     *
     * @param appId appId
     * @return tree
     */
    @Select("SELECT id,table_name FROM tb_table_access WHERE app_id = #{app_id} and del_flag = 1;")
    List<TableNameTreeDTO> listTableNameTree(@Param("app_id") long appId);

    /**
     * insertTableAccessPO
     *
     * @param tableAccessPo po
     * @return 执行结果
     */
    int insertTableAccessPo(@Param("tableAccessPo") TableAccessPO tableAccessPo);

    /**
     * 获取所有物理表id
     *
     * @return list
     */
    @Select("SELECT id,table_name FROM tb_table_access where del_flag=1 ORDER BY id DESC;")
    List<ChannelDataDTO> listTableIdAndName();

    /**
     * 获取所有物理表id和表名
     *
     * @return list
     */
    @Select("SELECT id,table_name FROM tb_table_access where del_flag=1 ORDER BY id DESC;")
    List<TableIdAndNameDTO> listTableIdAndNames();
}
