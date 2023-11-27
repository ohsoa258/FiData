package com.fisk.common.service.factorymdmdomainscript.impl;

import com.fisk.common.core.utils.StringBuildUtils;
import com.fisk.common.service.dbBEBuild.datamodel.dto.TableSourceRelationsDTO;
import com.fisk.common.service.factorymdmdomainscript.IBuildMdmScript;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author wangjian
 */
public class FactoryMdmDomainScriptSqlServerImpl implements IBuildMdmScript {

    /**
     * 拼接主数据构建基于域脚本
     *
     * @return
     */
    @Override
    public String buildMdmScript(List<TableSourceRelationsDTO> dto) {

        StringBuilder str = new StringBuilder();

        String tName = "[" + dto.get(0).sourceTable + "].";

        for (TableSourceRelationsDTO item : dto) {
            str.append("update ")
                    .append("[");
            str.append(item.sourceTable)
                    .append("]");
            str.append(" set ")
                    .append("[");
            str.append(item.sourceTable)
                    .append("]")
                    .append(".")
                    .append("[")
                    .append(item.sourceColumn)
                    .append("]");
            str.append(" = ");
            str.append("[")
                    .append(item.targetTable)
                    .append("]")
                    .append(".")
                    .append("[")
                    .append("fidata_id")
                    .append("]");
            str.append(" from ")
                    .append("[");
            str.append(item.sourceTable)
                    .append("]");
            if (!StringUtils.isEmpty(item.joinType)) {
                str.append(" ").append(item.joinType);
                str.append(" ")
                        .append("[")
                        .append(item.targetTable)
                        .append("]");
            }
            str.append(" on ")
                    .append("[");

            str.append(item.sourceTable)
                    .append("]")
                    .append(".")
                    .append("[")
                    .append(item.sourceColumn)
                    .append("]");
            str.append(" = ");
            str.append("[")
                    .append(item.targetTable)
                    .append("]")
                    .append(".")
                    .append("[")
                    .append(item.targetColumn)
                    .append("]")
                    .append(" WHERE ")
                    .append(tName)
                    .append("fidata_batch_code=")
                    .append("'${fidata_batch_code}' AND ")
                    .append(tName)
                    .append("fidata_flow_batch_code=")
                    .append("'${fragment.index}' AND ")
                    .append("fidata_version_id=")
                    .append("'${fidata_version_id}'");
            str.append(";");
        }

        return str.toString();
    }

}
