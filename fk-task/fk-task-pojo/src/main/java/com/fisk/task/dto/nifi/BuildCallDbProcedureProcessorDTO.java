package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BuildCallDbProcedureProcessorDTO extends BaseProcessorDTO {
 public String dbConnectionId;
 public String executsql;
 /*
  * 是否有下一个组件
  * */
 public Boolean haveNextOne;
}
