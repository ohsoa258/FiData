package com.fisk.chartvisual.util.dbhelper;

import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import org.springframework.stereotype.Service;

/**
 * @author JinXingWang
 */
//@Service
public class TabularHelper extends AmoHelper {
    public TabularHelper() {
        super(DataSourceTypeEnum.TABULAR);
    }
}
