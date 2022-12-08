package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.datafactory.TableIdAndNameDTO;
import com.fisk.dataaccess.dto.datamanagement.DataAccessSourceTableDTO;
import com.fisk.dataaccess.dto.dataops.TableQueryDTO;
import com.fisk.dataaccess.dto.table.TableAccessPageDTO;
import com.fisk.dataaccess.dto.table.TableNameDTO;
import com.fisk.dataaccess.dto.table.TableNameTreeDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessDTO;
import com.fisk.dataaccess.dto.v3.TbTableAccessQueryDTO;
import com.fisk.dataaccess.entity.TableAccessPO;
import com.fisk.dataaccess.vo.TableAccessVO;
import com.fisk.dataaccess.vo.TableNameVO;
import com.fisk.datafactory.dto.components.ChannelDataDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * @author Lock
 */
@Mapper
public interface TableAccessMapper extends FKBaseMapper<TableAccessPO> {
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
    @Select("select app_id,api_id,table_name from tb_table_access where del_flag=1")
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

    /**
     * 获取发布的表信息
     *
     * @return
     */
    @Select("SELECT a.id,a.app_id,a.sql_script,b.b.app_abbreviation,c.drive_type,CONCAT('ods','_',b.app_abbreviation,'_',a.table_name) as tableName,a.table_des from tb_table_access a \n" +
            "join tb_app_registration b on a.app_id=b.id\n" +
            "join tb_app_datasource c on a.app_id=c.id\n" +
            "where a.del_flag=1 and a.publish=1")
    List<DataAccessSourceTableDTO> listTableMetaData();

    /**
     * 修改jobId
     *
     * @param id
     * @param jobId
     * @return
     */
    @Update("update tb_table_access set job_id = #{jobId} where id = #{id} ")
    Integer updateJobId(@Param("id") long id, @Param("jobId") String jobId);

    /**
     * 更新表发布状态
     *
     * @param id
     * @param publish
     * @param publishErrorMsg
     * @return
     */
    @Update("update tb_table_access set publish = #{publish},publish_error_msg = #{publishErrorMsg} where id = #{id} ")
    Integer updatePublishStatus(@Param("id") long id, @Param("publish") int publish, @Param("publishErrorMsg") String publishErrorMsg);

    /**
     * 设置stg数据保留天数s
     *
     * @param id
     * @param keepNumber
     * @return
     */
    @Update("update tb_table_access set keep_number = #{keepNumber} where id = #{id} ")
    Integer setKeepNumber(@Param("id") long id, @Param("keepNumber") String keepNumber);

    /**
     * 根据appId获取物理表列表
     *
     * @param page
     * @param query
     * @return
     */
    Page<TbTableAccessDTO> getTableAccessListData(Page<TbTableAccessDTO> page, @Param("query") TbTableAccessQueryDTO query);

    /**
     * 数据运维根据表名获取表信息
     *
     * @param tableName
     * @return
     */
    TableQueryDTO getTableInfo(@Param("tableName") String tableName);

}
