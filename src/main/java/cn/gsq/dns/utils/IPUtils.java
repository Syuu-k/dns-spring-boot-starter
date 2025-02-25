package cn.gsq.dns.utils;

/**
 * @Description :
 * @Author : syu
 * @Date : 2024/4/9
 */
public final class IPUtils
{
    public static long toInteger(String addr)
    {
        String[] parts = addr.split("\\.");
        long ip = 0;
        for (int i = 0; i < 4; i++) ip |= (Integer.parseInt(parts[i]) & 0xff) << ((3 - i) * 8);
        return ip & 0xffffffffL;
    }

    public static String fromInteger(long ip)
    {
        long a = (ip >> 24) & 0xff,
                b = (ip >> 16) & 0xff,
                c = (ip >> 8) & 0xff,
                d = ip & 0xff;
        return a + "." + b + "." + c + "." + d;
    }
}
