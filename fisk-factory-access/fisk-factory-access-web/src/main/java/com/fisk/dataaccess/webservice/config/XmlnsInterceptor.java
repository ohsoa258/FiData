package com.fisk.dataaccess.webservice.config;
import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
/**
 * @Author: wangjian
 * @Date: 2023-12-14
 * @Description:
 */
public class XmlnsInterceptor extends AbstractPhaseInterceptor<SoapMessage> {
    private static Logger logger = LoggerFactory.getLogger(XmlnsInterceptor.class);

    public XmlnsInterceptor(String phase) {
        super(phase);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
//        try {
////            //需要放置在根节点的命名空间
////            Map<String, String> envMap = new HashMap<>();
////            envMap.put("soap-env", "http://schemas.xmlsoap.org/soap/envelope/");
//
//            //在命名空间下的元素都以自定义前缀生成
//            Map<String, String> namespaceMap = new HashMap<>();
////            namespaceMap.put("http://schemas.xmlsoap.org/soap/envelope/", "soap-env");
//            namespaceMap.put("http://tempuri.org/", "a");
//            JAXBDataBinding dataBinding = (JAXBDataBinding) message.getExchange().getEndpoint().getService()
//                    .getDataBinding();
//            dataBinding.setNamespaceMap(namespaceMap);
//
////            message.put("soap.env.ns.map", envMap);
//            message.put("disable.outputstream.optimization", true);
//        } catch (Exception e) {
//            logger.error("cxfInterceptor error", e);
//        }

//        try {
//            // 从流中获取请求消息体并以字符串形式输出，注意IOUtils是cxf的包；
//            String input = IOUtils.toString(message.getContent(InputStream.class), "UTF-8");
//            // 如果内容不为空（第一次连接也会被拦截，此时input为空）
//            if (StringUtils.isNotBlank(input)){
//                // 修改请求消息体为webservice服务要求的格式
//                input = input.replace("<ns2:KSF_NoticeResponse xmlns:ns2=\"http://tempuri.org/\">","<KSF_NoticeResponse xmlns=\"http://tempuri.org/\">")
//                        .replace("</a:KSF_NoticeResponse>","</KSF_NoticeResponse>");
//            }
//            // 重新写入
//            message.setContent(InputStream.class, new ByteArrayInputStream(input.getBytes()));
//        } catch (Exception e) {
//            System.out.println(String.format("解析报文异常: %s", e.getMessage()));
//        }

        try {
            // 从流中获取返回内容
            OutputStream os = message.getContent(OutputStream.class);
            CachedStream cs = new CachedStream();
            message.setContent(OutputStream.class, cs);
            message.getInterceptorChain().doIntercept(message);
            CachedOutputStream cachedOutputStream = (CachedOutputStream) message.getContent(OutputStream.class);
            InputStream in = cachedOutputStream.getInputStream();
            String output = IOUtils.toString(in, "UTF-8");
            // 修改内容为集成平台要求的格式
            output = output.replace("<ns2:KSF_NoticeResponse xmlns:ns2=\"http://tempuri.org/\">","<KSF_NoticeResponse xmlns=\"http://tempuri.org/\">")
                        .replace("</ns2:KSF_NoticeResponse>","</KSF_NoticeResponse>");
            // 处理完后写回流中
            IOUtils.copy(new ByteArrayInputStream(output.getBytes()), os);
            cs.close();
            os.flush();
            message.setContent(OutputStream.class, os);
        } catch (Exception e) {
            System.out.println(String.format("解析报文异常: %s", e.getMessage()));
        }
    }
    private static class CachedStream extends CachedOutputStream {
        public CachedStream() {
            super();
        }
        @Override
        protected void doFlush() throws IOException {
            currentStream.flush();
        }
        @Override
        protected void doClose() throws IOException {
        }
        @Override
        protected void onWrite() throws IOException {
        }
    }
}
