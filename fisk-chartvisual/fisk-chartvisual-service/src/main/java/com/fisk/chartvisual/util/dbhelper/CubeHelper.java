package com.fisk.chartvisual.util.dbhelper;

import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * @author JinXingWang
 */
@Service
public class CubeHelper extends AMOHelper  {
    public CubeHelper() {
        super(DataSourceTypeEnum.CUBE);
    }
}
