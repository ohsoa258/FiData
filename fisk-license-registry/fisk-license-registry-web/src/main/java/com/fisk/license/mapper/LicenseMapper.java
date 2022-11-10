package com.fisk.license.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.license.entity.LicencePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description LicenseMapper
 * @date 2022/11/10 15:48
 */
@Mapper
public interface LicenseMapper extends FKBaseMapper<LicencePO> {
}
