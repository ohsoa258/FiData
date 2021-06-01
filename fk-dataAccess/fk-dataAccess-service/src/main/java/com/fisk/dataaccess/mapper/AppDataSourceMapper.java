package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

/**
 * @author: Lock
 * @data: 2021/5/26 16:09
 */
@Mapper
public interface AppDataSourceMapper extends BaseMapper<AppDataSourcePO> {

    //
    @Insert("insert into tb_app_datasource values(#{id},#{appid},#{driveType}," +
            "#{connectStr},#{connectAccount},#{connectPwd},#{realtimeAccount}," +
            "#{realtimePwd},#{createTime},#{createUser},#{updateTime},#{updateUser},#{delFlag})")
    @Options(
            keyProperty = "id",
            keyColumn = "id"
    )
    int insert(AppDataSourcePO appDataSourcePO);

    int update(AppDataSourcePO appDataSourcePO);
}
