package com.fisk.dataaccess.utils.sql;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.jcoutils.MyDestinationDataProvider;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.pgsqlmetadata.OdsResultDTO;
import com.fisk.dataaccess.dto.sapbw.*;
import com.fisk.dataaccess.dto.table.FieldNameDTO;
import com.fisk.dataaccess.dto.table.TablePyhNameDTO;
import com.fisk.dataaccess.dto.tablestructure.TableStructureDTO;
import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.Environment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lsj
 * @date 20230731
 */
@Component
@Slf4j
public class SapBwUtils {

    /**
     * 获取所有cube名称
     *
     * @param destination
     * @param myProvider
     * @return
     */
    public static CubesAndCats getAllCubes(JCoDestination destination, MyDestinationDataProvider myProvider) {
        List<String> cubeNames = new ArrayList<>();
        List<String> cateNames = new ArrayList<>();
        CubesAndCats cubesAndCats = new CubesAndCats();
        try {
            // 启动连接上下文
            JCoContext.begin(destination);
            // 创建刷新函数
            JCoFunction refreshFunction = destination.getRepository().getFunction("BAPI_MDPROVIDER_REFRESH");
            // 执行刷新函数
            refreshFunction.execute(destination);
            // 创建RFC函数
            JCoFunction function = destination.getRepository().getFunction("BAPI_MDPROVIDER_GET_CUBES");

            // 设置函数参数   不设置具体cube名称，就是获取所有cube
//            JCoParameterList inputParams = function.getImportParameterList();
//            inputParams.setValue("CUBE_NAM", "ZFI_A001/ZFI_A001_QA21002_SY_2020_A2");

            // 执行RFC函数
            function.execute(destination);
            JCoParameterList tableParameterList = function.getTableParameterList();
            // 获取结果表
            JCoTable outputParams = tableParameterList.getTable("CUBES");

            for (int i = 0; i < outputParams.getNumRows(); i++) {
                outputParams.setRow(i);
                String cubeName = outputParams.getString("CUBE_NAM");
                String catName = outputParams.getString("CAT_NAM");

                cubeNames.add(cubeName);
                cateNames.add(catName);
            }
            // 结束连接上下文
            JCoContext.end(destination);
        } catch (Exception e) {
            log.error("sapbw获取所有cube名称报错..",e);
            throw new FkException(ResultEnum.SAPBW_GETCUBES_ERROR, e);
        } finally {
            Environment.unregisterDestinationDataProvider(myProvider);
        }
        cubesAndCats.setCubeNames(cubeNames);
        cubesAndCats.setCatNames(cateNames);
        return cubesAndCats;
    }

    /**
     * 获取所有cube名称  返回组装对应的对象
     *
     * @param destination
     * @param myProvider
     * @return
     */
    public static List<TablePyhNameDTO> getAllCubesV2(JCoDestination destination, MyDestinationDataProvider myProvider) {
        List<TablePyhNameDTO> list = null;
        try {
            list = new ArrayList<>();
            // 启动连接上下文
            JCoContext.begin(destination);
            // 创建刷新函数
            JCoFunction refreshFunction = destination.getRepository().getFunction("BAPI_MDPROVIDER_REFRESH");
            // 执行刷新函数
            refreshFunction.execute(destination);
            // 创建RFC函数
            JCoFunction function = destination.getRepository().getFunction("BAPI_MDPROVIDER_GET_CUBES");
            // 执行RFC函数
            function.execute(destination);
            JCoParameterList tableParameterList = function.getTableParameterList();
            // 获取结果表
            JCoTable outputParams = tableParameterList.getTable("CUBES");

            for (int i = 0; i < outputParams.getNumRows(); i++) {
                TablePyhNameDTO tablePyhNameDTO = new TablePyhNameDTO();
                outputParams.setRow(i);

                tablePyhNameDTO.setCubeName(outputParams.getString("CUBE_NAM"));
                tablePyhNameDTO.setCatName(outputParams.getString("CAT_NAM"));

                list.add(tablePyhNameDTO);
            }
            // 结束连接上下文
            JCoContext.end(destination);
        } catch (Exception e) {
            log.error("sapbw获取所有cube名称报错..v2:",e);
            throw new FkException(ResultEnum.SAPBW_GETCUBES_ERROR, e);
        } finally {
            Environment.unregisterDestinationDataProvider(myProvider);
        }
        return list;
    }

    /**
     * 根据cube名称获取cube的参数列表
     *
     * @param destination
     * @param myProvider
     * @return
     */
    public static List<TableStructureDTO> getVariablesByCubeName(JCoDestination destination, MyDestinationDataProvider myProvider, String cubeName) {
        List<TableStructureDTO> colNameList = new ArrayList<>();
        try {
            // 启动连接上下文
            JCoContext.begin(destination);
            // 测试连接
            destination.ping();
            // 创建刷新函数
            JCoFunction refreshFunction = destination.getRepository().getFunction("BAPI_MDPROVIDER_REFRESH");
            // 执行刷新函数
            refreshFunction.execute(destination);
            // 该函数用于获取cube的参数
            JCoFunction function = destination.getRepository().getFunction("BAPI_MDPROVIDER_GET_VARIABLES");
            // 设置参数
            JCoParameterList importParameterList = function.getImportParameterList();
            // 设置CUBE_NAM
            importParameterList.setValue("CUBE_NAM", cubeName);
            // 可以不设置CAT_NAM
//            importParameterList.setValue("CAT_NAM", "ZFI_A001");

            // 执行
            function.execute(destination);

            // 获取返回的数据--cube的参数列表详情
            JCoParameterList tableParameterList = function.getTableParameterList();
            // cube的参数列表详情
            JCoTable variablesTable = tableParameterList.getTable("VARIABLES");

            for (int i = 0; i < variablesTable.getNumRows(); i++) {
                variablesTable.setRow(i);
                TableStructureDTO dto = new TableStructureDTO();
                CubeVariable cubeVariable = new CubeVariable();
                //如果需要别的值，继续在这里增加即可
                // VAR_NAM 意思是 参数名称  其他值的意思可以参照 CubeVariable 对象里面的注释
                cubeVariable.setVarName(variablesTable.getString("VAR_NAM"));
                cubeVariable.setVarCap(variablesTable.getString("VAR_CAP"));
                cubeVariable.setVarUid(variablesTable.getString("VAR_UID"));
                cubeVariable.setVarOrder(variablesTable.getString("VAR_ORDINAL"));
                cubeVariable.setVarDataType(variablesTable.getString("VAR_TYPE"));
                cubeVariable.setVarRefDim(variablesTable.getString("REF_DIM"));
                cubeVariable.setVarDfltLow(variablesTable.getString("DFLT_LOW"));
                cubeVariable.setVarDfltHigh(variablesTable.getString("DFLT_HIGH"));
                cubeVariable.setVarDfltLowCap(variablesTable.getString("DFLT_LOW_CAP"));
                cubeVariable.setVarDfltHighCap(variablesTable.getString("DFLT_HIGH_CAP"));
                cubeVariable.setVarDescribe(variablesTable.getString("DSCRPTN"));
                dto.cubeVariable = cubeVariable;
                colNameList.add(dto);
            }
            // 结束连接上下文
            JCoContext.end(destination);

            // 只给返回的集合里面的第一个对象装载cube的所有维度和指标信息
            TableStructureDTO tableStructureDTO = colNameList.get(0);
            // 调用方法，获取指定cube下的所有维度和指标
            CubeDimsAndMeas dimsAndMeas = getDimsAndMeasByCubeName(destination, myProvider, cubeName);
            tableStructureDTO.setCubeDimsAndMeas(dimsAndMeas);

        } catch (Exception e) {
            log.error("sapbw获取cube参数报错:",e);
            throw new FkException(ResultEnum.SAPBW_GETVARS_ERROR, e);
        } finally {
            Environment.unregisterDestinationDataProvider(myProvider);
        }
        return colNameList;
    }

    /**
     * 根据cube名称获取指定cube下的所有维度和指标
     *
     * @param destination
     * @param myProvider
     * @return
     */
    public static CubeDimsAndMeas getDimsAndMeasByCubeName(JCoDestination destination, MyDestinationDataProvider myProvider, String cubeName) {
        CubeDimsAndMeas dimsAndMeas = new CubeDimsAndMeas();
        List<CubeDim> cubeDimList = new ArrayList<>();
        List<CubeMes> cubeMesList = new ArrayList<>();
        try {
            // 启动连接上下文
            JCoContext.begin(destination);
            // 测试连接
            destination.ping();
            // 获取dimension信息  dimension：维度
            JCoFunction dimensionFunction = destination.getRepository().getFunction("BAPI_MDPROVIDER_GET_DIMENSIONS");
            JCoParameterList dimImport = dimensionFunction.getImportParameterList();
            dimImport.setValue("CUBE_NAM", cubeName);
            dimensionFunction.execute(destination);
            // 获取维度 dims
            JCoParameterList dimsParams = dimensionFunction.getTableParameterList();
            JCoTable dimensionList = dimsParams.getTable("DIMENSIONS");

            for (int i = 0; i < dimensionList.getNumRows(); i++) {
                CubeDim cubeDim = new CubeDim();
                dimensionList.setRow(i);
                // 如果需要别的值，继续在这里增加即可
                cubeDim.setDimName(dimensionList.getString("DIM_NAM"));
                cubeDim.setDimUname(dimensionList.getString("DIM_UNAM"));
                cubeDim.setDimCap(dimensionList.getString("DIM_CAP"));
                cubeDim.setDimType(dimensionList.getString("DIM_TYPE"));
                cubeDim.setDimDescribe(dimensionList.getString("DSCRPTN"));
                cubeDimList.add(cubeDim);
            }

            // 获取measure信息  measure：指标
            JCoFunction measureFunction = destination.getRepository().getFunction("BAPI_MDPROVIDER_GET_MEASURES");
            JCoParameterList meaImport = measureFunction.getImportParameterList();
            meaImport.setValue("CUBE_NAM", cubeName);
            measureFunction.execute(destination);
            // 获取指标 meas
            JCoParameterList meaParams = measureFunction.getTableParameterList();
            JCoTable measureList = meaParams.getTable("MEASURES");

            for (int i = 0; i < measureList.getNumRows(); i++) {
                CubeMes cubeMes = new CubeMes();
                measureList.setRow(i);
                //如果需要别的值，继续在这里增加即可
                cubeMes.setMesName(measureList.getString("MES_NAM"));
                cubeMes.setMesUname(measureList.getString("MES_UNAM"));
                cubeMes.setMesCap(measureList.getString("MES_CAP"));
                cubeMes.setMesDataType(measureList.getString("DATA_TYPE"));
                cubeMes.setMesDescribe(measureList.getString("DSCRPTN"));
                cubeMesList.add(cubeMes);
            }

            // 结束连接上下文
            JCoContext.end(destination);
        } catch (Exception e) {
            log.error("sapbw获取指定cube下的所有维度和指标报错",e);
            throw new FkException(ResultEnum.SAPBW_GETDIMS_MES_ERROR, e);
        } finally {
            Environment.unregisterDestinationDataProvider(myProvider);
        }
        dimsAndMeas.setCubeMeaList(cubeMesList);
        dimsAndMeas.setCubeDimList(cubeDimList);
        return dimsAndMeas;
    }

    /**
     * 执行mdx语句，获取数据并返回
     *
     * @param destination
     * @param myProvider
     * @return
     */
    public static OdsResultDTO excuteMdx(JCoDestination destination, MyDestinationDataProvider myProvider, String mdx) {
        OdsResultDTO odsResultDTO = new OdsResultDTO();
        List<FieldNameDTO> fieldNameDTOS = new ArrayList<>();
        List<List<String>> allData = new ArrayList<>();

        //将前端传递的mdx语句截取为每段50长度的字符串
        int segmentLength = 50; // 每段字符串的长度
        List<String> segments = new ArrayList<>();
        for (int i = 0; i < mdx.length(); i += segmentLength) {
            int endIndex = Math.min(i + segmentLength, mdx.length());
            String segment = mdx.substring(i, endIndex);
            segments.add(segment);
        }

        try {
            // 为了执行多个RFC函数，我们需要开启上下文，这行代码至关重要！！！
            JCoContext.begin(destination);

            // 获取创建数据集的函数
            JCoFunction function_create = destination.getRepository().getFunction("BAPI_MDDATASET_CREATE_OBJECT");

            //获取入参列表
            JCoParameterList tableParams = function_create.getTableParameterList();
            try {
                JCoTable table = tableParams.getTable("COMMAND_TEXT");
                // 将每段要执行的mdx语句拼接起来
                for (String mdxByPart : segments) {
                    table.appendRow();
                    table.setValue("LINE", mdxByPart);
                }
                // 设置要执行的已处理的mdx语句作为参数COMMAND_TEXT
                tableParams.setValue("COMMAND_TEXT", table);
                function_create.getTableParameterList().setValue("COMMAND_TEXT", table);
                // 执行创建数据集的函数
                function_create.execute(destination);
                // 获取刚刚创建的数据集的id
                String datasetid = String.valueOf(function_create.getExportParameterList().getValue("DATASETID"));

                // 定义第二个函数：查询刚刚创建的数据集的数据
                JCoFunction function_select = destination.getRepository().getFunction("BAPI_MDDATASET_SELECT_DATA");
                // 将刚创建的数据集的id作为我们查询的参数
                function_select.getImportParameterList().setValue("DATASETID", datasetid);
                // 执行查询函数
                function_select.execute(destination);

                // 定义第三个函数：查询刚刚创建的数据集的列明细（字段详情）
                JCoFunction function_getAxisInfo = destination.getRepository().getFunction("BAPI_MDDATASET_GET_AXIS_INFO");
                // 获取参数列表
                JCoParameterList axisInfoParam = function_getAxisInfo.getImportParameterList();
                // 设置参数
                axisInfoParam.setValue("DATASETID", datasetid);
                // 执行函数
                function_getAxisInfo.execute(destination);

                JCoParameterList tableParameterList = function_getAxisInfo.getTableParameterList();
                // 获取字段详情
                JCoTable axisInfo = tableParameterList.getTable("AXIS_INFO");
                // 获取字段id
                List<String> axisList = new ArrayList<>();
                for (int i = 0; i < axisInfo.getNumRows(); i++) {
                    axisInfo.setRow(i);
                    String axis = axisInfo.getString("AXIS");
                    // 排除 000 和255 这两个无效列（非数据列）  000应该是字段
                    if (!"000".equals(axis) && !"255".equals(axis)) {
                        axisList.add(axis);
                    }
                }

                // 定义第四个函数：通过数据集id和要查询的列id,查询刚刚创建的数据集的数据
                JCoFunction function_getData = destination.getRepository().getFunction("BAPI_MDDATASET_GET_AXIS_DATA");
                JCoParameterList param = function_getData.getImportParameterList();
                param.setValue("DATASETID", datasetid);
                // 设置查询数据的行数
                // 预览只预览前九行数据！！！
                param.setValue("START_TUPLE", "0");
                param.setValue("END_TUPLE", "9");

                // 请注意：unFormattedData集合装载所有未处理数据
                List<String> unFormattedData = new ArrayList<>();
                // BAPI_MDDATASET_GET_AXIS_DATA在设置AXIS参数时，无法设置多个，因此采用这种方法去查询mdx语句里面的多列数据
                for (String s : axisList) {
                    // 设置axis参数，axis相当于每个字段
                    param.setValue("AXIS", s);
                    // 执行函数
                    function_getData.execute(destination);

                    // 获取查询到的数据
                    JCoParameterList parameterList = function_getData.getTableParameterList();
                    JCoTable mndtryPrptys = parameterList.getTable("MNDTRY_PRPTYS");

                    String firstFieldName = null;
                    if (!mndtryPrptys.isEmpty()) {
                        for (int i = 0; i < mndtryPrptys.getNumRows(); i++) {
                            mndtryPrptys.setRow(i);
                            String value = mndtryPrptys.getString("MEM_CAP");
                            unFormattedData.add(value);
                            // 这一步是为了获取字段名称
                            if (i == 0) {
                                firstFieldName = mndtryPrptys.getString("LVL_UNAM");
                                FieldNameDTO fieldNameDTO = new FieldNameDTO();
                                fieldNameDTO.setSourceFieldName(firstFieldName);
                                fieldNameDTO.setSourceFieldType("nvarchar");
                                fieldNameDTOS.add(fieldNameDTO);
                            }
                            if (!firstFieldName.equals(mndtryPrptys.getString("LVL_UNAM"))){
                                FieldNameDTO fieldNameDTO = new FieldNameDTO();
                                fieldNameDTO.setSourceFieldName(mndtryPrptys.getString("LVL_UNAM"));
                                fieldNameDTO.setSourceFieldType("nvarchar");
                                fieldNameDTOS.add(fieldNameDTO);
                            }
                        }
                    }
                }
                // 执行查询后不管mdx语句查询多少列数据，返回的都在一列里面，因此我们需要根据列的个数对数据做处理
                int size = fieldNameDTOS.size();
                // 这一步是将unFormattedData集合里面装载的未处理的数据，转换为allData集合装载的处理过的数据
                allData = convertToRowData(unFormattedData,size);

                // 将转换后的数据设置给返回对象
                odsResultDTO.setSapdataList(allData);
                odsResultDTO.setFieldNameDTOList(fieldNameDTOS);

                // 删除前面创建的数据集
                JCoFunction deleteObjectFunction = destination.getRepository().getFunction("BAPI_MDDATASET_DELETE_OBJECT");
                deleteObjectFunction.getImportParameterList().setValue("DATASETID", datasetid);
                deleteObjectFunction.execute(destination);

                // 结束连接上下文
                JCoContext.end(destination);
            } catch (Exception e) {
                log.error("sapbw执行mdx获取结果报错..",e);
                throw new FkException(ResultEnum.SAPBW_EXECUATE_MDX_ERROR, e);
            }
        } catch (Exception e) {
            log.error("sapbw执行mdx获取结果报错..",e);
            throw new FkException(ResultEnum.SAPBW_EXECUATE_MDX_ERROR, e);
        } finally {
            Environment.unregisterDestinationDataProvider(myProvider);
        }
        return odsResultDTO;
    }

    /**
     * 将集合里面装载的每列数据转换为装载每行数据的集合
     *
     * @param colList
     * @return
     */
    private static List<List<String>> convertToRowData(List<String> colList,int size) {
        List<List<String>> formedData = new ArrayList<>();
        List<String> sublist = new ArrayList<>();

        for (int i = 0; i < colList.size(); i++) {
            sublist.add(colList.get(i));

            if ((i + 1) % size == 0) {
                formedData.add(sublist);
                sublist = new ArrayList<>();
            }
        }

        // 处理剩余的元素，如果有的话
        if (!sublist.isEmpty()) {
            formedData.add(sublist);
        }
        return formedData;
    }


}
