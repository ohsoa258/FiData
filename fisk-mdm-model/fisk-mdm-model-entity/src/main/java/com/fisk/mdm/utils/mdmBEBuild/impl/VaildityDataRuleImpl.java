package com.fisk.mdm.utils.mdmBEBuild.impl;

import com.fisk.mdm.enums.DataRuleEnum;
import com.fisk.mdm.utils.mdmBEBuild.IVaildityDataRule;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class VaildityDataRuleImpl implements IVaildityDataRule {

    @Override
    public String VailditySpiltDataRule(Object obj,Integer len) {
        String value = null;
            if (obj!= null) {
                Double str = Double.parseDouble((String) obj);
                final DecimalFormat formatter = new DecimalFormat();
                formatter.setMaximumFractionDigits(len);
                formatter.setGroupingSize(0);
                formatter.setRoundingMode(RoundingMode.FLOOR);
                value =  formatter.format(str);
            }
            return  value;

    }

}
