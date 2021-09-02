package com.fisk.chartvisual.util.dbhelper;

import com.fisk.chartvisual.enums.GraphicTypeEnum;
import com.fisk.chartvisual.util.dbhelper.buildmdx.BaseBuildMdx;
import com.fisk.chartvisual.util.dbhelper.buildmdx.BuildLinePieMdx;
import com.fisk.chartvisual.util.dbhelper.buildmdx.BuildMatrixMdx;
import com.fisk.chartvisual.util.dbhelper.buildmdx.BuildTableMdx;

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
            case PIE:
            case LINE:
            case BAR:
            default:
                return new BuildLinePieMdx();
        }
     }
}
