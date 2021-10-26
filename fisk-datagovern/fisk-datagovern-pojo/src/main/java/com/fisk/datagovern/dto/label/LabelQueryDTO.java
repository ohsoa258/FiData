package com.fisk.datagovern.dto.label;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class LabelQueryDTO {
    public int categoryId;
    public Page<LabelDataDTO> dto;
}
