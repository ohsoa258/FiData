package com.fisk.dataservice.utils;

import com.fisk.dataservice.enums.GraphicTypeEnum;
import com.fisk.dataservice.utils.buildmdx.BaseBuildMdx;
import com.fisk.dataservice.utils.buildmdx.BuildLinePieMdx;
import com.fisk.dataservice.utils.buildmdx.BuildMatrixMdx;
import com.fisk.dataservice.utils.buildmdx.BuildTableMdx;

/**
 * Chart图形工厂
 * @author dick
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
