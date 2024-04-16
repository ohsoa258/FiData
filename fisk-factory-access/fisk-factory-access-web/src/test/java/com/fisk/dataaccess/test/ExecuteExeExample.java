package com.fisk.dataaccess.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * java调用exe文件 此处调用windows计算器
 */
public class ExecuteExeExample {
    public static void main(String[] args) {
        try {
            //C:\Windows\WinSxS\amd64_microsoft-windows-calc_31bf3856ad364e35_10.0.22621.1_none_0b53ccef0e7a283c
            // 替换为实际的.exe文件路径
            String exePath = "C:\\Windows\\WinSxS\\amd64_microsoft-windows-calc_31bf3856ad364e35_10.0.22621.1_none_0b53ccef0e7a283c\\calc.exe";
            String[] command = {exePath, "arg1", "arg2"}; // 如果需要传入参数，可以在此处指定

            // 调用exe文件并获取其进程对象
            Process process = Runtime.getRuntime().exec(command);

            // 可选：处理子进程的输出流和错误流（例如打印到控制台或写入文件）
            new Thread(() -> {
                try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = outputReader.readLine()) != null) {
                        System.out.println("Output: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("Error: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 等待子进程执行完毕并获取其退出状态
            int exitCode = process.waitFor();
            System.out.println("External program exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

