package cn.gsq.dns.stat;


import cn.gsq.dns.utils.IPUtils;

/**
 * @Description :
 * @Author : syu
 * @Date : 2024/4/11
 */
public class IPStat
{
    public int ip;
    public int queryCount;

    public IPStat(int ip, int count)
    {
        this.ip = ip;
        this.queryCount = count;
    }

    public String getIP()
    {
        return IPUtils.fromInteger(ip);
    }

    public int getQueryCount()
    {
        return this.queryCount;
    }
}
