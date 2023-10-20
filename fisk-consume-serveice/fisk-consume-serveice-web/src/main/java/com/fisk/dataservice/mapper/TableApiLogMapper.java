package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.dataservice.entity.TableApiLogPO;
import com.fisk.dataservice.vo.tableapi.TopFrequencyVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 
 *
 * @author wangjian
 * @date 2023-10-07 10:54:30
 */
@Mapper
public interface TableApiLogMapper extends BaseMapper<TableApiLogPO> {

    @Select("SELECT sum(number) FROM `tb_table_api_log` WHERE create_time >= CURDATE() and del_flag = 1")
    int getTotalNumber();
    @Select("SELECT count(1) FROM `tb_table_api_log` WHERE create_time >= CURDATE() and del_flag = 1")
    int getFrequency();
    @Select("select count(1) FROM (SELECT api_id FROM `tb_table_api_log` WHERE create_time >= CURDATE() and del_flag = 1 GROUP BY api_id) as api_log")
    int getApiNumber();
    @Select("SELECT count(important_interface) FROM `tb_table_api_log` WHERE create_time >= CURDATE() and important_interface = 1 and status = 1 and del_flag = 1")
    int focusApiTotalNumber();
    @Select("SELECT count(1) FROM `tb_table_api_log` WHERE create_time >= CURDATE() and status = 1 and del_flag = 1")
    int successNumber();
    @Select("SELECT count(1) FROM `tb_table_api_log` WHERE create_time >= CURDATE() and status = 0 and del_flag = 1")
    int faildNumber();

    @Select("SELECT t1.frequency,t2.api_name as apiName from (SELECT api_id,count(1) as frequency FROM `tb_table_api_log` WHERE create_time >= CURDATE() and status = 1 and del_flag = 1 GROUP BY api_id ORDER BY frequency DESC limit 5) as t1\n" +
            "LEFT JOIN tb_table_api_service t2 on t1.api_id = t2.id")
    List<TopFrequencyVO> getTopFrequency();
}
