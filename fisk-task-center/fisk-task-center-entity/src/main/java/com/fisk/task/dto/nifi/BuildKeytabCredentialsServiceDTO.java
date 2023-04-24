package com.fisk.task.dto.nifi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildKeytabCredentialsServiceDTO extends BaseProcessorDTO {
    @ApiModelProperty(value = "kerberos密钥选项卡")
    public String kerberosKeytab;

    @ApiModelProperty(value = "kerberos主体")
    public String kerberosprincipal;
}
