package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.dataaccess.entity.AppDriveTypePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Lock
 */
@Mapper
public interface AppDriveTypeMapper extends BaseMapper<AppDriveTypePO> {

    /**
     * 查询所有数据源驱动类型
     *
     * @return 驱动类型
     */
    @Select("SELECT id,`name`,connect_str,type FROM tb_app_drivetype;")
    List<AppDriveTypePO> listData();

}
