package com.fisk.chartvisual.util.dbhelper;

import com.fisk.common.core.enums.chartvisual.DataSourceTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * mysql
 *
 * @author gy
 */
@Slf4j
public class MySqlHelper extends AbstractDbHelper {
    public MySqlHelper() {
        super(DataSourceTypeEnum.MYSQL);
    }

}
