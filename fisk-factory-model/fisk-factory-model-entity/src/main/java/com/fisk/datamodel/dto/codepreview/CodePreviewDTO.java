package com.fisk.datamodel.dto.codepreview;

import com.fisk.datamodel.dto.businessarea.OverlayCodePreviewDTO;
import com.fisk.task.dto.daconfig.OverLoadCodeDTO;
import lombok.Data;

/**
 * @author lishiji
 * 并非用于前端传参，后端自行使用，用于整合参数
 */
@Data
public class CodePreviewDTO {

    /**
     *
     */
    public OverLoadCodeDTO overLoadCodeDTO;

    /**
     *
     */
    public OverlayCodePreviewDTO overlayCodePreviewDTO;

}
