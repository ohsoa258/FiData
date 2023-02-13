package com.fisk.common.service.accessAndTask;

import lombok.Data;

/**
 * @ClassName: 用于解决数据接入中调用task模块拼接sql数据传输
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/

@Data
public class DataTranDTO {

    public String tableName;

    public String querySql;

    public String driveType;

    public String deltaTimes;
}
