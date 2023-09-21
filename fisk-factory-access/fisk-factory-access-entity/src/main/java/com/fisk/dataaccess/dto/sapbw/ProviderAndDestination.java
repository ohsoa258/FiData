package com.fisk.dataaccess.dto.sapbw;

import com.fisk.common.core.utils.jcoutils.MyDestinationDataProvider;
import com.sap.conn.jco.JCoDestination;
import lombok.Data;

/**
 * @author lsj
 * @description sapbw的连接对象类
 * @date 2022/5/27 15:36
 */
@Data
public class ProviderAndDestination {

    public JCoDestination destination;

    public MyDestinationDataProvider myProvider;

}
