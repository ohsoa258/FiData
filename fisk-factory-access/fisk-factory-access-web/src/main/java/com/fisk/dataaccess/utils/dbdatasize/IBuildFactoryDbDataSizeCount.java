package com.fisk.dataaccess.utils.dbdatasize;

import com.fisk.system.dto.datasource.DataSourceDTO;

/**
 * @author lishiji
 */
public interface IBuildFactoryDbDataSizeCount {

    /**
     * 获取数据接入-ods库当前存储的数据大小 gb
     * @return
     */
    String DbDataStoredSize(DataSourceDTO data);

}
