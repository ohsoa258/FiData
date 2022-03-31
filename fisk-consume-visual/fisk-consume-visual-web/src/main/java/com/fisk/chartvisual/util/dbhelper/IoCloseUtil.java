package com.fisk.chartvisual.util.dbhelper;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author WangYan
 * @date 2022/2/10 18:03
 */
public class IoCloseUtil {

    /**
     *   IO流关闭工具类
     */
    public static void close(Closeable... io) {
        for (Closeable temp : io) {
            try {
                if (null != temp) {
                    temp.close();
                }
            } catch (IOException e) {
                System.out.println("" + e.getMessage());
            }
        }
    }

    public static <T extends Closeable> void closeAll(T... io) {
        for (Closeable temp : io) {
            try {
                if (null != temp) {
                    temp.close();
                }
            } catch (IOException e) {
                System.out.println("" + e.getMessage());
            }
        }

    }
}
