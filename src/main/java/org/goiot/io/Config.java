package org.goiot.io;

/**
 * <p>Title: Config</p>
 * <p>Description: Function Description</p>
 * <p>Copyright: Ruijie Co., Ltd. (c) 2018</p>
 * <p>@Author: chenxing</p>
 * <p>@Date: 2018/4/22 10:42</p>
 */
public interface Config {

    String LOCAL_HOST = "127.0.0.1";
    int BIO_SERVER_PORT = 9001;
    int NIO_SERVER_PORT = 9002;
    int AIO_SERVER_PORT = 9003;
    String CLIENT_MSG = "Hello, this is Client!";
    String SERVER_MSG = "Hello, server has accept your Information !";
}
