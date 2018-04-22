package org.goiot.io.server.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.goiot.io.Config;

/**
 * <p>Title: NioServerMain</p>
 * <p>Description: Function Description</p>
 * <p>Copyright: Ruijie Co., Ltd. (c) 2018</p>
 * <p>@Author: chenxing</p>
 * <p>@Date: 2018/4/22 12:36</p>
 */
public class NioServerMain {

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws Throwable {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(Config.NIO_SERVER_PORT));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            if (selector.select(1000L) <= 0) {
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (Iterator<SelectionKey> iterator = selectionKeys.iterator(); iterator.hasNext(); iterator.remove()) {
                SelectionKey selectionKey = iterator.next();
                try {
                    //只有ServerSocketChannel 有接收请求的
                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel socketChannel = channel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ, new ArrayDeque<String>());
                    }
                    if (selectionKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        for (int readBytes = socketChannel.read(buffer); readBytes > 0;
                            readBytes = socketChannel.read(buffer)) {
                            buffer.flip();
                            /**
                             * demo不做粘包的处理,直接答应单词收到的包
                             */
                            byte[] bytes = new byte[buffer.remaining()];
                            buffer.get(bytes);
                            business(new String(bytes, "UTF-8"));
                            ArrayDeque<String> outputMessageBuffer = (ArrayDeque<String>) selectionKey.attachment();
                            outputMessageBuffer
                                .add(String.format("%s:%s\n", NioServerMain.class.getName(), Config.SERVER_MSG));
                            selectionKey.interestOps(selectionKey.readyOps() | SelectionKey.OP_WRITE);
                        }

                    }
                    if (selectionKey.isWritable()) {
                        ArrayDeque<String> outputMessageBuffer = (ArrayDeque<String>) selectionKey.attachment();
                        String outputMessage = outputMessageBuffer.poll();
                        if (outputMessage != null) {
                            executor.submit(() -> {
                                try {
                                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                                    ByteBuffer byteBuffer = ByteBuffer.wrap(outputMessage.getBytes());
                                    socketChannel.write(byteBuffer);
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    selectionKey.cancel();
                    if (selectionKey.channel() != null) {
                        selectionKey.channel().close();
                    }
                }

            }
        }
    }

    private static void business(String businessData) {
        System.out.println("来自客户端的数据");
        System.out.println(businessData);
    }
}
