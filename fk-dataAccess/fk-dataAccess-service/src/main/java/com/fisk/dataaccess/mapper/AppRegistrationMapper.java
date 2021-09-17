package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.AppNameDTO;
import com.fisk.dataaccess.dto.AppRegistrationPageDTO;
import com.fisk.dataaccess.dto.DataAccessTreeDTO;
import com.fisk.dataaccess.dto.RepeatNameDTO;
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
     * 根据appName查询id
     * @param appName appName
     * @return 返回值
     */
    @Select("select id from tb_app_registration where app_name=#{app_name} and del_flag=1")
    Long getIdByAppName(@Param("app_name")String appName);

    /**
     * 根据appName查询id
     * @param appName appName
     * @return 返回值
     */
    @Select("select * from tb_app_registration where app_name=#{app_name} and del_flag=1")
    List<AppRegistrationPO> getByAppName(@Param("app_name")String appName);
    /**
     * 倒序查询
     * @return 返回值
     */
    @Select("select id,app_name,app_des,create_time from tb_app_registration order by create_time desc limit 0,10")
    List<AppRegistrationPO> getDescDate();

    /**
     * 筛选器分页功能
     *
     * @param page  分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<AppRegistrationVO> filter(Page<AppRegistrationVO> page, @Param("query") AppRegistrationPageDTO query);

    /**
     * 获取应用注册名称和id
     *
     * @return 应用名称
     */
    @Select("SELECT id,app_name,app_type FROM tb_app_registration WHERE del_flag = 1 ORDER BY create_time DESC;")
    List<AppNameDTO> getDataList();

    /**
     * 获取应用注册name及id
     * @return tree
     */
    @Select("SELECT id,app_name,sync_mode,expression,msg FROM tb_app_registration WHERE del_flag = 1;")
    List<DataAccessTreeDTO> listAppTree();

    /**
     * 查询应用名称及应用简称集合
     * @return list
     */
    @Select("SELECT app_name,app_abbreviation FROM tb_app_registration WHERE del_flag=1;")
    List<RepeatNameDTO> getRepeatNameList();

    /**
     * 查询appAbbreviation
     * @return appAbbreviation
     */
    @Select("select app_abbreviation from tb_app_registration where del_flag=1")
    List<String> getAppAbbreviation();

    /**
     * appRegistrationDTO
     *
     * @return
     */
    int insertAppRegistrationPO(@Param("appRegistrationPO")AppRegistrationPO appRegistrationPO);

}
