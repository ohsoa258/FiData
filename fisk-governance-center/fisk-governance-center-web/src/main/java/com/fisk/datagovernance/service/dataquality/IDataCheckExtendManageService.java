package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datagovernance.entity.dataquality.DataCheckExtendPO;

/**
 * @author dick
 * @version 1.0
 * @description 数据校验规则扩展属性
 * @date 2022/4/2 11:21
 */
public interface IDataCheckExtendManageService extends IService<DataCheckExtendPO> {
    /**
     * 查询表字段规则信息
     *
     * @return 字段规则列表
     */
//    List<DataCheckVO> setTableFieldName(List<DataCheckVO> source, List<FiDataMetaDataDTO> tableFields);
}
