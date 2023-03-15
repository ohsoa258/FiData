package com.fisk.datamodel.service;

import com.fisk.datamodel.dto.businessarea.OverlayCodePreviewDTO;
import com.fisk.system.dto.datasource.DataSourceDTO;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
public interface IBuildOverlaySqlPreview {

    Object buildStgToOdsSql(DataSourceDTO data, OverlayCodePreviewDTO dto, OverLoadCodeDTO dataModel);
}
