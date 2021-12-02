package com.fisk.datamodel.dto.modelanalysispublish;

import lombok.Data;

import javax.swing.plaf.PanelUI;
import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class AnalysisPublishIndicatorFactDTO {
    public long factId;
    public String factTable;
    public List<AnalysisPublishIndicatorDTO> list;
}
