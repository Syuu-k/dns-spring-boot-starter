package cn.gsq.dns.protocol;

import cn.gsq.dns.config.DnsServerProperties;
import cn.gsq.dns.protocol.entity.Request;
import cn.gsq.dns.protocol.entity.Response;
import cn.gsq.dns.stat.StatManager;
import cn.gsq.dns.utils.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @Description :
 * @Author : syu
 * @Date : 2024/4/9
 */
public class NameServer extends Thread {

    @Autowired
    DnsServerProperties properties;

    @Autowired
    StatManager statManager;

    static Logger logger = LoggerFactory.getLogger(NameServer.class);

    ArrayBlockingQueue<Request> queries = null;
    ArrayBlockingQueue<Response> responses = null;

    public NameServer() {

        this.setName("nameserver-thread");
        this.queries = new ArrayBlockingQueue<Request>(65535);
        this.responses = new ArrayBlockingQueue<Response>(65535);
    }

    public void run() {
        DatagramChannel datagramChannel = null;
        try {
//            StatManager statMgr = StatManager.getInstance();

            String bindIP = properties.getAddr();
            int port = properties.getPort();

            Selector selector = Selector.open();

            datagramChannel = DatagramChannel.open();
            datagramChannel.socket().bind(new InetSocketAddress(bindIP, port));
            datagramChannel.configureBlocking(false);

            new Sender(this, datagramChannel, statManager).start();
            logger.info("NameServer started at {}:{}", bindIP, port);

            datagramChannel.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            datagramChannel.register(selector, SelectionKey.OP_READ);
            while (!this.isInterrupted()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = (SelectionKey) iterator.next();
                    if (selectionKey.isReadable()) {
                        buffer.clear();
                        SocketAddress addr = datagramChannel.receive(buffer);
                        buffer.flip();
                        byte[] message = new byte[buffer.limit()];
                        buffer.get(message, 0, message.length);

                        logger.debug("##############################################################################################");
                        logger.debug("received: from = {}, length = {}, ", addr.toString(), message.length);
                        queries.put(new Request(addr, Packet.create(message)));
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("nameserver receive error", ex);
        } finally {
            try {
                datagramChannel.close();
            } catch (Exception e) {
            }
            logger.info("NameServer app exited...");
            System.exit(1);
        }
    }

    public Request takeRequest() {
        try {
            return queries.take();
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean putResponse(Response response) {
        try {
            responses.put(response);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public Response takeResponse() throws InterruptedException {
        return responses.take();
    }

    // 使用独立的线籔程去发送回应
    static class Sender extends Thread {
        NameServer nameServer;
        DatagramChannel datagramChannel;

        StatManager statManager;

        public Sender(NameServer nameServer, DatagramChannel datagramChannel, StatManager statManager) {
            this.nameServer = nameServer;
            this.datagramChannel = datagramChannel;
            this.statManager = statManager;
            this.setName("name-server-sender");
        }

        public void run() {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
//            StatManager statMgr = StatManager.getInstance();
            while (!this.isInterrupted()) {
                try {
                    Response response = nameServer.takeResponse();
//                    statMgr.addAnswerCount();
                    statManager.addAnswerCount();
                    buffer.clear();
                    buffer.put(response.packet);
                    buffer.flip();
                    datagramChannel.send(buffer, response.remoteAddress);
                    logger.debug("send: to = {}, length = {}", response.remoteAddress, response.packet.length);
                } catch (Exception e) {
                    if (e instanceof InterruptedException) break;
                    logger.error("send error", e);
                }
            }
        }
    }

    public void init() {
        this.start();
    }
}
