package cn.gsq.dns.protocol.entity;

/**
 * @Description :
 * @Author : syu
 * @Date : 2024/4/9
 */
public class Question
{
    public String name;
    public int type;
    // public int class;        // 因为关键字的原因，就不设置此字段了，反正也都是固定值
    public Question(String name, int type)
    {
        this.name = name;
        this.type = type;
    }
}
