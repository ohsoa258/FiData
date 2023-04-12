package com.fisk.license.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.license.entity.LicencePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author dick
 * @version 1.0
 * @description LicenseMapper
 * @date 2022/11/10 15:48
 */
@Mapper
public interface LicenseMapper extends FKBaseMapper<LicencePO> {

    /**
     * 修改字段有效性标记
     *
     * @return 操作结果
     */
    @Update("UPDATE tb_system_licence SET del_flag=0,update_time=NOW();")
    int updateDelFlag();
}
