package com.fisk.datamodel.vo;

import com.fisk.datamodel.dto.DataDomain.AreaBusinessDTO;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/8/12 11:01
 * 数据域
 */
@Data
public class DataDomainVO {
    public List<AreaBusinessDTO> areaBusinessList;
}
