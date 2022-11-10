package com.fisk.dataaccess.utils.filterfield;

import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.dataaccess.dto.GetConfigDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JianWenYang
 */
@Component
public class FilterFieldUtils {

    @Resource
    GetMetadata getMetadata;
    @Resource
    GetConfigDTO getConfig;

    public List<FilterFieldDTO> getDataTargetColumn(String tableName, String filterSql) {
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = getConfig.url;
        dto.userName = getConfig.username;
        dto.password = getConfig.password;
        dto.driver = getConfig.driver;
        dto.tableName = tableName;
        dto.filterSql = filterSql;
        return getMetadata.getMetadataList(dto);
    }

}
