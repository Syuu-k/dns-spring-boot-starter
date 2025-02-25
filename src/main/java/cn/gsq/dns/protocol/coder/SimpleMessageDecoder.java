package cn.gsq.dns.protocol.coder;


import cn.gsq.dns.protocol.entity.Message;
import cn.gsq.dns.utils.Packet;

/**
 * @Description : 查询消息包解码器
 * @Author : syu
 * @Date : 2024/4/9
 */
public final class SimpleMessageDecoder
{
    public static Message decode(Packet packet)
    {
        Message msg = new Message();
        msg.transactionId = packet.nextShort() & 0xffff;
        msg.flags = packet.nextShort() & 0xffff;
        msg.questions = packet.nextShort() & 0xffff;
        msg.answerRRs = packet.nextShort() & 0xffff;
        msg.authorityRRs = packet.nextShort() & 0xffff;
        msg.additionalRRs = packet.nextShort() & 0xffff;
        return msg;
    }
}
