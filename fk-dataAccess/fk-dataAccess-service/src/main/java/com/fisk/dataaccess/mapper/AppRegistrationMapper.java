package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.AppRegistrationPageDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Lock
 */
@Mapper
public interface AppRegistrationMapper extends FKBaseMapper<AppRegistrationPO> {

    /**
     * 查询表信息
     *
     * @return 返回值
     */
    @Select("select * from tb_app_registration")
    List<AppRegistrationVO> getData();

    /**
     * 查询appName
     * @return 返回值
     */
    @Select("select app_name from tb_app_registration where del_flag=1")
    List<String> getAppName();

    /**
     * 倒序查询
     * @return 返回值
     */
    @Select("select app_name,app_des,create_time from tb_app_registration order by create_time desc limit 0,10")
    List<AppRegistrationPO> getDescDate();

    /**
     * 筛选器
     *
     * @param page page
     * @param query query
     * @return 查询结果
     */
    Page<AppRegistrationDTO> filter(Page<AppRegistrationDTO> page, @Param("query") AppRegistrationPageDTO query);
}
