package com.fisk.chartvisual.util.dbhelper;

import com.fisk.chartvisual.enums.GraphicTypeEnum;
import com.fisk.chartvisual.util.dbhelper.buildmdx.BaseBuildMdx;
import com.fisk.chartvisual.util.dbhelper.buildmdx.BuildLinePieMdx;

/**
 * Chart图形工厂
 * @author JinXingWang
 */
public class GraphicsFactory {
     public static BaseBuildMdx getMdxHelper (GraphicTypeEnum graphicTypeEnum)
     {
        switch (graphicTypeEnum){
            case TABLE:
            case MATRIX:
            case Pie:
            case Line:
            default:
                return new BuildLinePieMdx();
        }
     }
}
