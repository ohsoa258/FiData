package com.fisk.chartvisual.util.dbhelper;

import com.fisk.chartvisual.enums.GraphicTypeEnum;
import com.fisk.chartvisual.util.dbhelper.buildmdx.BaseBuildMdx;
import com.fisk.chartvisual.util.dbhelper.buildmdx.BuildLinePieMdx;
import com.fisk.chartvisual.util.dbhelper.buildmdx.BuildMatrixMdx;
import com.fisk.chartvisual.util.dbhelper.buildmdx.BuildTableMdx;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;

/**
 * Chart图形工厂
 * @author JinXingWang
 */
public class GraphicsFactory {
     public static BaseBuildMdx getMdxHelper (GraphicTypeEnum graphicTypeEnum)
     {
        switch (graphicTypeEnum){
            case TABLE:
                return new BuildTableMdx();
            case MATRIX:
                return new BuildMatrixMdx();
            case DEFAULT:
                return new BuildLinePieMdx();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
     }
}
