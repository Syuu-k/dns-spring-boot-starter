package cn.gsq.dns.stat;

/**
 * @Description :
 * @Author : syu
 * @Date : 2024/4/11
 */
public class DomainNameStat
{
    public int id;
    public String name;
    public int queryCount;
    public boolean success;

    public DomainNameStat(int id, String name)
    {
        this.id = id;
        this.name = name;
        this.queryCount = 0;
    }
}
