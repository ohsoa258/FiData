package com.fisk.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 * @version 1.3
 * @description 推送数据路径表
 * @date 2022/2/18 15:34
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_authenticate_pushdatalist")
public class AuthenticatePushDataListPO extends BasePO {
    public String path;
    public String details;
}
