package org.goit.io.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.goiot.io.Config;

/**
 * <p>Title: ClientMain</p>
 * <p>Description: Function Description</p>
 * <p>Copyright: Ruijie Co., Ltd. (c) 2018</p>
 * <p>@Author: chenxing</p>
 * <p>@Date: 2018/4/22 11:07</p>
 */
public class ClientMain {

    public static void main(String[] args) throws Throwable {
        Socket socket = new Socket(Config.LOCAL_HOST, Config.AIO_SERVER_PORT);
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        printWriter.println(String.format("%s:%s", ClientMain.class.getName(), Config.CLIENT_MSG));

        BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        String businessData = bufferedReader.readLine();
        System.out.println("server's message: " + businessData.toString());
    }
}
