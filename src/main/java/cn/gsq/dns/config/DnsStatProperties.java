package cn.gsq.dns.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix="galaxy.dns.stat")
public class DnsStatProperties {

    private String logger = "off";

    private String loggerMem = "200m";

}
