package cn.gsq.dns.protocol.entity;

import java.net.SocketAddress;

/**
 * @Description :
 * @Author : syu
 * @Date : 2024/4/9
 */
public class Response
{
    public short sequence;
    public SocketAddress remoteAddress;
    public byte[] packet;

    public Response(short seq, SocketAddress remoteAddress, byte[] packet)
    {
        this.sequence = seq;
        this.remoteAddress = remoteAddress;
        this.packet = packet;
    }

    public Response(SocketAddress remoteAddress, byte[] packet)
    {
        this.packet = packet;
        this.remoteAddress = remoteAddress;
    }
}
