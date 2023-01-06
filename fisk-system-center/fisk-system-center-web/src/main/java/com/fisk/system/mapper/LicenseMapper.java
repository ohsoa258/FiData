package com.fisk.system.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.system.dto.roleinfo.RolePageDTO;
import com.fisk.system.entity.LicencePO;
import com.fisk.system.vo.license.LicenceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description LicenseMapper
 * @date 2022/11/10 15:48
 */
@Mapper
public interface LicenseMapper extends FKBaseMapper<LicencePO> {

    /**
     * 查询所有客户许可证信息
     *
     * @return 查询结果
     */
    List<LicenceVO> getAll(@Param("keyWord") String keyWord);

    /**
     * 获取最大的ID
     *
     * @return 查询结果
     */
    @Select("SELECT\n" +
            "\tifnull( MAX( id ), 0 ) AS maxId \n" +
            "FROM\n" +
            "\ttb_system_licence;")
    int getMaxId();
}
