package com.fisk.dataservice.utils;

import com.fisk.common.enums.dataservice.DataSourceTypeEnum;
import org.springframework.stereotype.Service;

/**
 * @author dick
 */
@Service
public class TabularHelper extends AmoHelper {
    public TabularHelper() {
        super(DataSourceTypeEnum.TABULAR);
    }
}
