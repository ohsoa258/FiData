package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangjian
 * @date 2024-03-20 14:01:01
 */
@TableName("tb_api_encrypt_config")
@Data
public class ApiEncryptConfigPO extends BasePO {

    @ApiModelProperty(value = "api_id")
    private Integer apiId;

    @ApiModelProperty(value = "密钥值")
    private String encryptKey;

}
