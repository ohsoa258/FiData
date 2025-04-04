<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="Content-Style-Type" content="text/css"/>
    <title></title>
    <style type="text/css">
        * {
            margin: 0;
            padding: 0;
        }

        body {
            font-family: pingfang sc light;
        }

        ul {
            font-size: 16px;
        }

        ul li {
            width: 300px;
            height: 25px;
            margin: 15px 0;
        }

        li {
            list-style-type: none;
        }
    </style>
</head>

<body>

<!---首页 start-->
<div class="page" style="text-align: center;">
    <br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
    <div>
        <p><b style="font-size: 25px">${title}</b></p>
    </div>
    <div>
        <p><b style="font-size: 15px">${docVersion}</b></p>
    </div>
    <br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
    <div>
        <p><b style="font-size: 15px">${isuCompany}</b></p>
    </div>
    <div>
        <p><b style="font-size: 15px">${isuDate}</b></p>
    </div>
</div>
<span style="page-break-after:always;"></span>
<!---首页 end-->


<!---版本页 start-->
<div style="text-align: center;">
    <p><b>版本历史</b></p>
    <div>
        <table style="border-collapse: collapse; margin: auto; border: 1px solid #c6c6c6; width: 100%">
            <tr>
                <th style="border-collapse: collapse;background-color: #ddeeff;padding: 5px 9px;font-size: 14px;font-weight: normal;
            text-align: center;  border: 1px solid #c6c6c6;">版本号
                </th>
                <th style="border-collapse: collapse;background-color: #ddeeff;padding: 5px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;
  border: 1px solid #c6c6c6;">开始日期
                </th>
                <th style="border-collapse: collapse;
            background-color: #ddeeff;
            padding: 5px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;
  border: 1px solid #c6c6c6;">结束日期
                </th>
                <th style="border-collapse: collapse;
            background-color: #ddeeff;
            padding: 5px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;
  border: 1px solid #c6c6c6;">修改者
                </th>
                <th style="border-collapse: collapse;
            background-color: #ddeeff;
            padding: 5px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;
  border: 1px solid #c6c6c6;">说明
                </th>
                <th style="border-collapse: collapse;
            background-color: #ddeeff;
            padding: 5px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;
  border: 1px solid #c6c6c6;">状态
                </th>
            </tr>
            <#list apiVersionDTOS as apiVersionItem>
                <tr>
                    <td style="border-collapse: collapse;
                  border: 1px solid #c6c6c6;
            padding: 5px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiVersionItem.version!}
                    </td>
                    <td style="border-collapse: collapse;
                  border: 1px solid #c6c6c6;
            padding: 5px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiVersionItem.startDate!}
                    </td>
                    <td style="border-collapse: collapse;
                  border: 1px solid #c6c6c6;
            padding: 5px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiVersionItem.endDate!}
                    </td>
                    <td style="border-collapse: collapse;
                  border: 1px solid #c6c6c6;
            padding: 5px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiVersionItem.modifier!}
                    </td>
                    <td style="border-collapse: collapse;
                  border: 1px solid #c6c6c6;
            padding: 5px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiVersionItem.explain!}
                    </td>
                    <td style="border-collapse: collapse;
                  border: 1px solid #c6c6c6;
            padding: 5px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiVersionItem.state!}
                    </td>
                </tr>
            </#list>
        </table>
    </div>
</div>
<span style="page-break-after:always;"></span>
<!---版本页 end-->


<!---目录页 start-->
<div style="padding: 0px">
    <div style="text-align: center;">
        <p><b>目录</b></p>
    </div>
    <ul>
        <#list apiCatalogueDTOS as apiCatalogueItem>
            <#if apiCatalogueItem.grade == 1>
                <li>${apiCatalogueItem.catalogueName}</li>
            <#elseif apiCatalogueItem.grade  == 2>
                <li>
                    <b style="font-size: 12px">${apiCatalogueItem.catalogueIndex}</b>&nbsp;&nbsp;${apiCatalogueItem.catalogueName}
                </li>
            <#elseif apiCatalogueItem.grade  == 3>
                <li>&nbsp;&nbsp;<b
                            style="font-size: 12px;">${apiCatalogueItem.catalogueIndex}</b>&nbsp;&nbsp;${apiCatalogueItem.catalogueName}
                </li>
            </#if>
        </#list>
    </ul>
</div>
<span style="page-break-after:always;"></span>
<!---目录页 end-->


<!---文档介绍 start-->
<div>
    <div>
        <div>
            <p style="font-weight: bold;">1.&nbsp;&nbsp;文档概述</p>
        </div>
    </div>
    <div>
        <div>
            <p style="font-weight: bold;">1.1.&nbsp;&nbsp;文档目的</p>
        </div>
        <div>
            <p style="text-indent: 2em;font-size: 12px">${docPurpose}</p>
        </div>
    </div>
    <div>
        <div>
            <p style="font-weight: bold;">1.2.&nbsp;&nbsp;读者对象</p>
        </div>
        <div>
            <p style="text-indent: 2em;font-size: 12px">${readers}</p>
        </div>
    </div>
</div>
<span style="page-break-after:always;"></span>
<!---文档介绍 end-->


<!---Restful API start-->
<div>
    <div>
        <div>
            <p style="font-weight: bold;">2.&nbsp;&nbsp;Restful API接口</p>
        </div>
    </div>
    <div>
        <div>
            <p style="font-weight: bold;">2.1.&nbsp;&nbsp;接口对接规范</p>
        </div>
        <div>
            <p style="text-indent: 2em;font-size: 12px">${standard}</p>
        </div>
    </div>
    <div>
        <div>
            <p style="font-weight: bold;">2.2.&nbsp;&nbsp;登录授权</p>
        </div>
        <div>
            <p style="text-indent: 2em;font-size: 12px">${authStandard}</p>
        </div>
    </div>
    <div>
        <div>
            <p style="font-weight: bold;">2.3.&nbsp;&nbsp;接口地址</p>
        </div>
        <div>
            <p style="text-indent: 2em;font-size: 12px">${prdAddress}</p>
        </div>
        <#--        <div>-->
        <#--            <p style="text-indent: 2em;font-size: 12px">${prdAddress}</p>-->
        <#--        </div>-->
    </div>
    <#list apiBasicInfoDTOS as apiBasicItem>
        <div>
            <div>
                <p style="font-weight: bold;">${apiBasicItem.apiNameCatalogue}&nbsp;&nbsp;${apiBasicItem.apiName}</p>
            </div>
        </div>
        <div>
            <div>
                <p style="font-weight: bold;">${apiBasicItem.apiAddressCatalogue}&nbsp;&nbsp;apiaddress</p>
            </div>
            <div>
                <p style="text-indent: 3em;font-size: 12px">${apiBasicItem.apiAddress}</p>
            </div>
        </div>
        <div>
            <div>
                <p style="font-weight: bold;">${apiBasicItem.apiDescCatalogue}&nbsp;&nbsp;简要描述</p>
            </div>
            <div>
                <p style="text-indent: 3em;font-size: 12px">${apiBasicItem.apiDesc!}</p>
            </div>
        </div>
        <div>
            <div>
                <p style="font-weight: bold;">${apiBasicItem.apiRequestTypeCatalogue}&nbsp;&nbsp;请求方式</p>
            </div>
            <div>
                <p style="text-indent: 3em;font-size: 12px">${apiBasicItem.apiRequestType}</p>
            </div>
        </div>
        <div>
            <div>
                <p style="font-weight: bold;">${apiBasicItem.apiContentTypeCatalogue}&nbsp;&nbsp;Content-Type</p>
            </div>
            <div>
                <p style="text-indent: 3em;font-size: 12px">${apiBasicItem.apiContentType}</p>
            </div>
        </div>
        <div>
            <div>
                <p style="font-weight: bold;">${apiBasicItem.apiHeaderCatalogue}&nbsp;&nbsp;参数（header）</p>
            </div>
            <div>
                <p style="text-indent: 3em;font-size: 12px">${apiBasicItem.apiHeader}</p>
            </div>
        </div>
        <div>
            <div>
                <p style="font-weight: bold;">${apiBasicItem.apiRequestExamplesCatalogue}&nbsp;&nbsp;请求示例</p>
            </div>
            <div style="width: 100%;padding: 20px;">
                <pre id="out_pre_req"
                     style="word-wrap: break-word; white-space: pre-wrap;">${apiBasicItem.apiRequestExamples}</pre>
            </div>
        </div>
        <div>
            <div>
                <p style="font-weight: bold;">${apiBasicItem.apiRequestCatalogue}&nbsp;&nbsp;参数（body）</p>
            </div>
            <div>
                <table style="border-collapse: collapse; margin: auto; border: 1px solid #c6c6c6;width: 100%">
                    <thead>
                    <tr>
                        <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">参数名
                        </th>
                        <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">必填
                        </th>
                        <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">类型
                        </th>
                        <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">说明
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <#list apiBasicItem.apiRequestDTOS_Fixed as apiRequestItem_Fixed>
                        <tr style="${apiRequestItem_Fixed.trStyle!}">
                            <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiRequestItem_Fixed.parmName!}
                            </td>
                            <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiRequestItem_Fixed.isRequired!}
                            </td>
                            <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiRequestItem_Fixed.parmType!}
                            </td>
                            <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiRequestItem_Fixed.parmDesc!}
                            </td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>

            <#if apiBasicItem.apiRequestDTOS?? && (apiBasicItem.apiRequestDTOS?size>0)>
                <div>
                    <p>parmList参数说明</p>
                </div>
                <div>
                    <table style="border-collapse: collapse; margin: auto; border: 1px solid #c6c6c6;width: 100%">
                        <thead>
                        <tr>
                            <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">参数名
                            </th>
                            <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">必填
                            </th>
                            <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">类型
                            </th>
                            <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">说明
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list apiBasicItem.apiRequestDTOS as apiRequestItem>
                            <tr style="${apiRequestItem.trStyle!}">
                                <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiRequestItem.parmName!}
                                </td>
                                <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiRequestItem.isRequired!}
                                </td>
                                <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiRequestItem.parmType!}
                                </td>
                                <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiRequestItem.parmDesc!}
                                </td>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
                </div>
            </#if>

        </div>
        <div>
            <div>
                <p style="font-weight: bold;">${apiBasicItem.apiResponseExamplesCatalogue}&nbsp;&nbsp;返回示例</p>
            </div>
            <div style="width: 100%;padding: 20px;">
                <pre id="out_pre_rep"
                     style="word-wrap: break-word; white-space: pre-wrap;">${apiBasicItem.apiResponseExamples}</pre>
            </div>
        </div>
        <div>
            <div>
                <p style="font-weight: bold;">${apiBasicItem.apiResponseCatalogue}&nbsp;&nbsp;${apiBasicItem.apiResponseHeaderDesc}</p>
            </div>
            <div>
                <table style="border-collapse: collapse; margin: auto; border: 1px solid #c6c6c6;width: 100%">
                    <thead>
                    <tr>
                        <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">参数名
                        </th>
                        <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">类型
                        </th>
                        <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">说明
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <#list apiBasicItem.apiResponseDTOS as apiResponseItem>
                        <tr style="${apiResponseItem.trStyle!}">
                            <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiResponseItem.parmName!}
                            </td>
                            <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiResponseItem.parmType!}
                            </td>
                            <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiResponseItem.parmDesc!}
                            </td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
            <#if apiBasicItem.apiResponseDataArray?? && (apiBasicItem.apiResponseDataArray?size>0)>
                <div>
                    <p>data参数说明</p>
                </div>
                <div>
                    <table style="border-collapse: collapse; margin: auto; border: 1px solid #c6c6c6;width: 100%">
                        <thead>
                        <tr>
                            <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">参数名
                            </th>
                            <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">类型
                            </th>
                            <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">说明
                            </th>
                            <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">是否为校验结果字段
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list apiBasicItem.apiResponseDataArray as apiResponseItem>
                            <tr style="${apiResponseItem.trStyle!}">
                                <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiResponseItem.parmName!}
                                </td>
                                <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiResponseItem.parmType!}
                                </td>
                                <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiResponseItem.parmDesc!}
                                </td>
                                <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiResponseItem.parmReturnFlag!}
                                </td>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
                </div>
            </#if>
        </div>
    </#list>
</div>
<span style="page-break-after:always;"></span>
<!---Restful API end-->

<!---Restful API C# 调用示例 start-->
<div>
    <div>
        <p style="font-weight: bold;">3.&nbsp;&nbsp;C#代码调用示例</p>
    </div>
    <div style="width: 100%;padding-left: 20px;">
        <pre id="out_pre_apiCodeExamples_net" style="word-wrap: break-word; white-space: pre-wrap;">
          ${apiCodeExamples_net?html}
        </pre>
    </div>
</div>
<span style="page-break-after:always;"></span>
<!---Restful API C# 调用示例 end-->

<!---Restful API java 调用示例 start-->
<div>
    <div>
        <p style="font-weight: bold;">4.&nbsp;&nbsp;java代码调用示例</p>
    </div>
    <div style="width: 100%;padding-left: 20px;">
        <pre id="out_pre_apiCodeExamples_java" style="word-wrap: break-word; white-space: pre-wrap;">
          ${apiCodeExamples_java?html}
        </pre>
    </div>
</div>
<span style="page-break-after:always;"></span>
<!---Restful API java 调用示例 end-->

<!---错误代码示例 start-->
<div>
    <div>
        <p style="font-weight: bold;">5.&nbsp;&nbsp;API返回代码示例</p>
    </div>
    <div>
        <table style="border-collapse: collapse; margin: auto; border: 1px solid #c6c6c6;width: 100%">
            <thead>
            <tr>
                <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">code
                </th>
                <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">类型
                </th>
                <th style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            background-color: #ddeeff;
            padding: 8px 9px;
            font-size: 14px;
            font-weight: normal;
            text-align: center;">说明
                </th>
            </tr>
            </thead>
            <tbody>
            <#list apiResponseCodeDTOS as apiResponseCodeItem>
                <tr style="${apiResponseCodeItem.trStyle}">
                    <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiResponseCodeItem.code}
                    </td>
                    <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiResponseCodeItem.type}
                    </td>
                    <td style="border-collapse: collapse;
            border: 1px solid #c6c6c6;
            padding: 8px 9px;
            font-size: 12px;
            font-weight: normal;
            text-align: center;
            word-break: break-all;">${apiResponseCodeItem.desc}
                    </td>
                </tr>
            </#list>
            </tbody>
        </table>
    </div>
</div>
<span style="page-break-after:always;"></span>
<!---错误代码示例 end-->

</body>

</html>