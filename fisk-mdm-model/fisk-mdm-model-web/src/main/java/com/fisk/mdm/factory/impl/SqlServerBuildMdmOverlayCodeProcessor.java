package com.fisk.mdm.factory.impl;

import com.fisk.mdm.factory.BuildMdmOverlayCodeProcessor;
import com.fisk.task.dto.accessmdm.AccessAttributeDTO;

import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2023-12-26
 * @Description:
 */
public class SqlServerBuildMdmOverlayCodeProcessor  implements BuildMdmOverlayCodeProcessor {
    @Override
    public String buildMeargeByPrimaryKey(List<AccessAttributeDTO> attributeList, String souceTableName, String targetTableName, Integer versionId) {
        return null;
    }

    @Override
    public String buildMeargeByAllData(List<AccessAttributeDTO> attributeList, String souceTableName, String targetTableName, Integer versionId) {
        return null;
    }
}
