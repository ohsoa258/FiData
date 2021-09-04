package com.fisk.dataaccess.apollo.listener;

import org.springframework.stereotype.Component;

/**
 * @author Lock
 */
@Component
public class PropertiesConfigLoader {
//    /**
//     * logger:日志对象.
//     *
//     * @since JDK 1.7
//     */
//    private static Logger logger = LoggerFactory.getLogger(PropertiesConfigLoader.class);
//
//    @Value("${app.id}")
//    private String appId;
//
//    private void load(String externalConfigFileLocation) throws IOException, JoranException {
//        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//
//        File externalConfigFile = new File(externalConfigFileLocation);
//        if (!externalConfigFile.exists()) {
//            throw new IOException("配置文件不存在");
//        } else {
//            if (!externalConfigFile.isFile()) {
//                throw new IOException("配置文件不正确");
//            } else {
//                if (!externalConfigFile.canRead()) {
//                    throw new IOException("配置文件不能被读取");
//                } else {
//                    JoranConfigurator configurator = new JoranConfigurator();
//                    configurator.setContext(lc);
//                    lc.reset();
//                    configurator.doConfigure(externalConfigFileLocation);
//                    StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
//                }
//            }
//        }
//    }
//
//    @PostConstruct
//    private void initLog() {
//        try {
//            String pathName = getPathName();
//            String xmlPath = prop2Xml(pathName);
//            load(xmlPath);
//        } catch (Exception e) {
//            logger.warn("获取apollo配置文件data-service.properties失败，data-service.properties使用默认配置！  原因:" + e.getMessage());
//        }
//    }
//
//    @ApolloConfigChangeListener("data-service.properties")
//    private void anotherOnChange(ConfigChangeEvent changeEvent) {
//        //当logback.xml文件改变的时候动态更新
//        try {
//            String pathName = getPathName();
//            String xmlPath = prop2Xml(pathName);
//            load(xmlPath);
//        } catch (Exception e) {
//            logger.warn("apollo配置文件data-service.properties热更新失败！  原因:" + e.getMessage());
//        }
//    }
//
//    private String getPathName() {
//        String pathName;
//        String system = System.getProperty("os.name");
//        if (system.toLowerCase().startsWith("win")) {
//            pathName = "C:/opt/data/" + appId + "/config-cache/";
//        } else {
//            //除了win其他系统路径一样
//            pathName = "/opt/data/" + appId + "/config-cache/";
//        }
//
//        String cluster = "";
//        List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
//        for (String in : inputArgs) {
//            if (in.contains("Dapollo") && in.contains("cluster")) {
//                String[] clusters = in.split("=");
//                cluster = clusters[1].replaceAll(" ", "");
//            }
//        }
//        pathName += appId + "+default" + cluster + "+" + "data-service.properties.properties";
//        return pathName;
//    }
//
//    private String prop2Xml(String path) throws Exception {
//        StringBuffer fileContent = new StringBuffer();
//        File filename = new File(path);
//        InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
//        BufferedReader br = new BufferedReader(reader);
//        int f = 0;
//        String line = "";
//        line = br.readLine();
//        while (line != null) {
////            if (f > 1) {
//                //前两行注释不要
////                fileContent.append(line);
////            }
//            line = br.readLine();
//            f++;
//        }
//
//        //去掉content=
////        fileContent.replace(0, 8, "");
//        //java反转义
//        String outContent = StringEscapeUtils.unescapeJava(fileContent.toString());
//
//        //生成xml文件
//        String outPath = path.replaceAll(".properties", "");
//        File file = new File(outPath);
//        if (!file.exists()) {
//            file.createNewFile();
//        } else {
//            //先删除再重新创建不然会报错
//            file.delete();
//            file.createNewFile();
//        }
//        FileOutputStream out = new FileOutputStream(file, true);
//        out.write(outContent.getBytes("utf-8"));
//        out.close();
//        return outPath;
//    }
}
