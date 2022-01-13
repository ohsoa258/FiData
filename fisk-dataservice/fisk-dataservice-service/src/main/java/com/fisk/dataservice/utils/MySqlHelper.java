package com.fisk.dataservice.utils;

import com.fisk.common.enums.dataservice.DataSourceTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * mysql
 *
 * @author dick
 */
@Slf4j
public class MySqlHelper extends AbstractDbHelper {
    public MySqlHelper() {
        super(DataSourceTypeEnum.MYSQL);
    }

}
