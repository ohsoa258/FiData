package com.fisk.dataservice.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataservice.entity.AppWhiteListConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author dick
 * @version 1.0
 * @description 应用白名单配置
 * @date 2023/6/7 9:41
 */
@Mapper
public interface AppWhiteListMapper extends FKBaseMapper<AppWhiteListConfigPO> {
}
