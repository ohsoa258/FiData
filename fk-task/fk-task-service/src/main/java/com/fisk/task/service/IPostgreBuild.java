package com.fisk.task.service;

import com.fisk.common.entity.BusinessResult;

public interface IPostgreBuild {
     BusinessResult postgreBuildTable(String executsql,String dbName);
}
