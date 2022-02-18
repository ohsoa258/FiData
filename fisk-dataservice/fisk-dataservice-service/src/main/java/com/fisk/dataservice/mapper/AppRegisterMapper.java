package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.app.*;
import com.fisk.dataservice.entity.AppConfigPO;
import com.fisk.dataservice.vo.app.*;
import com.fisk.dataservice.vo.datasource.DataSourceConVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 应用注册mapper
 *
 * @author dick
 */
@Mapper
public interface AppRegisterMapper extends FKBaseMapper<AppConfigPO> {

    /**
     * 筛选器分页功能
     *
     * @param page  分页对象
     * @param query query对象
     * @return 查询结果
     */
    Page<AppRegisterVO> filter(Page<AppRegisterVO> page, @Param("query") AppRegisterPageDTO query);

    /**
     * 应用列表分页功能
     *
     * @param page 分页对象
     * @return 查询结果
     */
    Page<AppRegisterVO> getAll(Page<AppRegisterVO> page);

    /**
     * 根据应用账号查询应用信息
     *
     * @return 查询结果
     */
    @Select("SELECT id,app_name,app_name,app_desc,app_password,app_principal FROM tb_app_config WHERE app_account=#{appAccount} AND del_flag=1;")
    AppConfigPO getByAppAccount(@Param("appAccount") String appAccount);

    /**
     * 根据应用账号密码查询应用信息
     *
     * @return 查询结果
     */
    @Select("SELECT id,app_name,app_name,app_desc,app_password,app_principal FROM tb_app_config WHERE app_account=#{appAccount} AND app_password=#{appPassword} AND del_flag=1;")
    AppConfigPO getByAppInfo(@Param("appAccount") String appAccount, @Param("appPassword") String appPassword);
}
