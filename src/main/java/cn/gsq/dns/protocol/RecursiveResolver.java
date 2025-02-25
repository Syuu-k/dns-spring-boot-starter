package cn.gsq.dns.protocol;

import cn.gsq.dns.protocol.entity.Request;
import cn.gsq.dns.protocol.entity.Response;
import cn.gsq.dns.protocol.entity.UpStreamDns;
import cn.gsq.dns.service.DnsManager;
import cn.gsq.dns.utils.ByteUtils;
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
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

/**
 * @Description :
 * @Author : syu
 * @Date : 2024/4/12
 */
public class RecursiveResolver extends Thread {

    @Autowired
    DnsManager dnsManager;

    static Logger logger = LoggerFactory.getLogger(RecursiveResolver.class);
    ArrayBlockingQueue<Request> queries = null;
    ArrayBlockingQueue<Response> responses = null;
    Map<Short, OriginalRequest> transactionMap = null;
    Set<InetSocketAddress> upstreamNameServers = new HashSet<>(); // 修改点：保存多个上游DNS服务器地址

    public RecursiveResolver() {
        this.setName("recursive-resolver-thread");
        this.queries = new ArrayBlockingQueue<Request>(65535);
        this.responses = new ArrayBlockingQueue<Response>(65535);
        transactionMap = new HashMap<>(65535);
    }

    // 添加上游DNS服务器地址
    public void addUpstreamNameServer(InetSocketAddress serverAddress) {
        this.upstreamNameServers.add(serverAddress);
    }

    // 删除上游DNS服务器地址
    public void removeUpstreamNameServer(InetSocketAddress serverAddress) {
        this.upstreamNameServers.remove(serverAddress);
    }

    // 获取上游DNS服务列表
    public Set<UpStreamDns> getUpstreamNameServer() {
        return this.upstreamNameServers.stream().map(dns -> {
            UpStreamDns upStreamDns = new UpStreamDns();
            upStreamDns.setAddress(dns.getAddress().getHostAddress());
            upStreamDns.setPort(dns.getPort());
            return upStreamDns;
        }).collect(Collectors.toSet());
    }

    public void run() {
        DatagramChannel datagramChannel = null;
        try {
            Set<UpStreamDns> upStreamDns = dnsManager.getUpStreamDns();
            for (UpStreamDns upStreamDn : upStreamDns) {
                this.upstreamNameServers.add(new InetSocketAddress(upStreamDn.getAddress(), upStreamDn.getPort()));
            }
            Selector selector = Selector.open();

            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);

            new Sender(this, datagramChannel).start();

            logger.info("Recursive Resolver started...");

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

                        logger.info("##############################################################################################");
                        logger.info("answer received: from = {}, length = {}, ", addr.toString(), message.length);
                        short seq = 0;
                        OriginalRequest req = takeUpstreamRequest(seq = (short) ByteUtils.getShort(message, 0, 2));
                        if (req != null) responses.add(new Response(req.sequence, req.remoteAddress, message));
                        else logger.info("no original request found for: " + seq);
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

    public void putRequest(Request request) {
        try {
            this.queries.add(request);
        } catch (Exception ex) {
            logger.error("put request error", ex);
        }
    }

    public Request takeRequest() throws InterruptedException {
        return queries.take();
    }

    public Response takeResponse() {
        try {
            return responses.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    static class OriginalRequest {
        public short sequence;
        public SocketAddress remoteAddress;

        public OriginalRequest(short seq, SocketAddress addr) {
            this.sequence = seq;
            this.remoteAddress = addr;
        }
    }

    // 使用独立的线籔程去发送回应
    static class Sender extends Thread {
        RecursiveResolver recursiveResolver;
        DatagramChannel datagramChannel;

        public Sender(RecursiveResolver recursiveResolver, DatagramChannel datagramChannel) {
            this.recursiveResolver = recursiveResolver;
            this.datagramChannel = datagramChannel;
            this.setName("recursive-resolver-sender");
        }

        public void run() {
            short sequence = 1;
            ByteBuffer buffer = ByteBuffer.allocate(1024);
//            StatManager statMgr = StatManager.getInstance();
            while (!this.isInterrupted()) {
                try {
                    Request request = recursiveResolver.takeRequest();
                    Packet packet = request.packet;
                    for (InetSocketAddress upstreamNameServer : recursiveResolver.upstreamNameServers) {
                        short seq = sequence++;
                        request.sequence = seq;
                        recursiveResolver.saveUpstreamRequest(seq, new OriginalRequest(packet.seek(0).nextShort(), request.remoteAddress));
                        packet.seek(0).setShort(seq);
                        buffer.clear();
                        buffer.put(packet.getBytes());
                        buffer.flip();
                        datagramChannel.send(buffer, upstreamNameServer);
                        logger.debug("send request to upstream: to = {}, sequence = {}, length = {}", upstreamNameServer, seq & 0xffff, packet.size());
                    }
                } catch (Exception e) {
                    if (e instanceof InterruptedException) break;
//                    logger.error("send error", e);
                }
            }
        }
    }

    protected void saveUpstreamRequest(short sequence, OriginalRequest request) {
        transactionMap.put(sequence, request);
    }

    protected OriginalRequest takeUpstreamRequest(short sequence) {
        return transactionMap.remove(sequence);
    }

    public void init() {
        this.start();
    }
}
