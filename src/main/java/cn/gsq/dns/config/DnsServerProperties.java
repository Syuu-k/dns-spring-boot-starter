package cn.gsq.dns.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix="galaxy.dns.server")
public class DnsServerProperties {

    private String addr = "0.0.0.0";

    private Integer port = 53;


}
