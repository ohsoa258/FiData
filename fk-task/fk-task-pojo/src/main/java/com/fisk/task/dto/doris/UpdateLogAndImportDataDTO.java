package com.fisk.task.dto.doris;

import com.fisk.common.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/2 12:32
 * Description:
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UpdateLogAndImportDataDTO extends BaseDTO {
    public int id;
    public String tablename;
    public DateTime startdate;
    public DateTime enddate;
    public int datarows;
    public int status;
    public String code;
    public String errordesc;
}
