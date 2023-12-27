package com.fisk.mdm.factory;

import com.fisk.task.dto.accessmdm.AccessAttributeDTO;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-12-26
 * @Description:
 */

public interface BuildMdmOverlayCodeProcessor {
    //根据主键同步
    String buildMeargeByPrimaryKey(List<AccessAttributeDTO> attributeList, String souceTableName, String targetTableName, Integer versionId);
    //全量同步
    String buildMeargeByAllData(List<AccessAttributeDTO> attributeList,String souceTableName,String targetTableName,Integer versionId);
}
