package org.goiot.io.server.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import org.goiot.io.Config;

/**
 * <p>Title: NioServerMain</p>
 * <p>Description: Function Description</p>
 * <p>Copyright: Ruijie Co., Ltd. (c) 2018</p>
 * <p>@Author: chenxing</p>
 * <p>@Date: 2018/4/22 10:40</p>
 */
public class BioServerMain {

    private static ServerSocket server;

    public static void main(String[] args) throws Throwable {
        try {
            server = new ServerSocket(Config.BIO_SERVER_PORT);
            System.out.println("服务器已启动，端口号：" + Config.BIO_SERVER_PORT);
            while (true) {
                /**
                 * 1.使用主线程来接收和处理客户端的连接
                 * {@link ServerSocket#accept()} 将会阻塞线程,直到有新的socket进来
                 */
                final Socket socket = server.accept();
                //当有新的客户端接入时，会执行下面的代码
                //然后创建一个新的线程处理这条Socket链路
                /**
                 * 2.为每一个进来的socket创建一个新的线程来处理
                 */
                System.out.println("有新的连接进来了!");
                new Thread(() -> {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                        /**
                         * 3.读取客户端的数据,它只能是面向字节或者字符
                         * {@link InputStream#read()} 将会阻塞线程直到数据的到来
                         */
                        for (String businessData = bufferedReader.readLine(); businessData != null;
                            businessData = bufferedReader.readLine()) {
                            /**
                             * 4.业务处理
                             * 一般情况下会另外启动一个线程来处理业务
                             */
                            business(businessData);
                            /**
                             * 5.往客户端写回数据,它只能是面向字节或者字符,它同样会阻塞线程
                             */
                            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
                            printWriter.println(
                                String.format("%s:%s", BioServerMain.class.getName(), Config.SERVER_MSG));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        } finally {
            //一些必要的清理工作
            if (server != null) {
                System.out.println("服务器已关闭。");
                server.close();
                server = null;
            }
        }
    }

    private static void business(String businessData) {
        System.out.println("来自客户端的数据");
        System.out.println(businessData);
    }

}
