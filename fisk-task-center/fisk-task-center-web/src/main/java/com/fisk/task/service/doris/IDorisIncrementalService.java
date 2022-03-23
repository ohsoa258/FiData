package com.fisk.task.service.doris;

import com.fisk.common.entity.BusinessResult;
import com.fisk.task.dto.doris.UpdateLogAndImportDataDTO;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/3 11:16
 * Description:
 */
public interface IDorisIncrementalService {
     public BusinessResult updateNifiLogsAndImportOdsData(UpdateLogAndImportDataDTO dto);
}
