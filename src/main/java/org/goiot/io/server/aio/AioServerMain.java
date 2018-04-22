package org.goiot.io.server.aio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import org.goiot.io.Config;

/**
 * <p>Title: AioServerMain</p>
 * <p>Description: Function Description</p>
 * <p>Copyright: Ruijie Co., Ltd. (c) 2018</p>
 * <p>@Author: chenxing</p>
 * <p>@Date: 2018/4/22 13:59</p>
 */
public class AioServerMain {

    public static void main(String[] args) throws Throwable {
        printThreadInfo("Main");
        AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(Config.AIO_SERVER_PORT));
        serverSocketChannel.accept(serverSocketChannel,
            new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
                @Override
                public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {
                    printThreadInfo("AcceptorHandler#completed");
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    result.read(buffer, result, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
                        @Override
                        public void completed(Integer result, AsynchronousSocketChannel attachment) {
                            printThreadInfo("ReadHandler#completed");
                            buffer.flip();
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            /**
                             * demo不做粘包的处理,直接答应单词收到的包
                             */
                            try {
                                business(new String(bytes, "UTF-8"));
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }

                            String outputMessage = String
                                .format("%s:%s\n", AioServerMain.class.getName(), Config.SERVER_MSG);
                            ByteBuffer byteBuffer = ByteBuffer.wrap(outputMessage.getBytes());
                            attachment.write(byteBuffer, attachment,
                                new CompletionHandler<Integer, AsynchronousSocketChannel>() {
                                    @Override
                                    public void completed(Integer result, AsynchronousSocketChannel attachment) {
                                        printThreadInfo("WriteHandler#completed");
                                    }

                                    @Override
                                    public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
                                        printThreadInfo("WriteHandler#failed");
                                        exc.printStackTrace();
                                    }
                                });
                            buffer.clear();
                            //读取还未读完的数据
                            attachment.read(buffer, attachment, this);
                        }

                        @Override
                        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
                            printThreadInfo("ReaderHandler#failed");
                        }
                    });
                    serverSocketChannel.accept(serverSocketChannel, this);
                }

                @Override
                public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
                    printThreadInfo("AcceptorHandler#failed");
                }
            });
        //由于AIO完全异步，需要在这里让主线程保持运行中
        while (true) {

        }
    }

    private static void printThreadInfo(String prefix) {
        System.out
            .println(String.valueOf(prefix) + ": this method is invoke by thread " + Thread.currentThread().getName());
    }

    private static void business(String businessData) {
        System.out.println("来自客户端的数据");
        System.out.println(businessData);
    }
}
