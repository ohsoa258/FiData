package com.fisk.dataservice.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.dataservice.dto.app.AppApiSubQueryDTO;
import com.fisk.dataservice.entity.AppApiPO;
import com.fisk.dataservice.entity.AppConfigPO;
import com.fisk.dataservice.vo.app.AppApiSubVO;
import com.fisk.dataservice.vo.app.AppRegisterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 应用api mapper
 *
 * @author dick
 */
@Mapper
public interface AppApiMapper extends FKBaseMapper<AppApiPO> {
    Page<AppApiSubVO> getSubscribeAll(Page<AppApiSubVO> page, @Param("query") AppApiSubQueryDTO query);

    /**
     * 根据应用id和apiId查询单条订阅记录
     *
     * @return 查询结果
     */
    @Select("SELECT id,api_id,app_id,api_state FROM tb_app_api WHERE app_id=#{appId} AND api_id=#{apiId} AND del_flag=1;")
    AppApiPO getSubscribeBy(@Param("appId") int appId, @Param("apiId") int apiId);

    /**
     * 根据应用id查询此应用下所有的api
     *
     * @return 查询结果
     */
    @Select("SELECT id,api_id,app_id,api_state FROM tb_app_api WHERE app_id=#{appId} AND del_flag=1;")
    List<AppApiPO> getSubscribeListByAppId(@Param("appId") int appId);
}
