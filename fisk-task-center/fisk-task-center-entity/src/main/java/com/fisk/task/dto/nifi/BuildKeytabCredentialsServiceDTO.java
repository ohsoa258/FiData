package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildKeytabCredentialsServiceDTO extends BaseProcessorDTO {
    public String kerberosKeytab;
    public String kerberosprincipal;
}
