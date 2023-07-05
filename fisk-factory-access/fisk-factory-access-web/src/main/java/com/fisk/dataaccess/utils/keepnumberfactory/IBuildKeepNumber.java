package com.fisk.dataaccess.utils.keepnumberfactory;

import com.fisk.dataaccess.dto.table.TableKeepNumberDTO;
import com.fisk.dataaccess.entity.AppRegistrationPO;

import java.util.List;

public interface IBuildKeepNumber {

    String setKeepNumberSql(TableKeepNumberDTO dto, AppRegistrationPO appRegistrationPO, List<String> stgAndTableName);

}
