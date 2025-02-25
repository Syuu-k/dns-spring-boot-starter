package cn.gsq.dns.protocol;


import cn.gsq.dns.protocol.entity.Address;
import cn.gsq.dns.protocol.entity.Rule;
import cn.gsq.dns.service.DnsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Description : 域名解析规则管理器
 * @Author : syu
 * @Date : 2024/4/11
 */
public final class RuleManager {

    @Autowired
    DnsManager dnsManager;

    static Logger logger = LoggerFactory.getLogger(RuleManager.class);

    ConcurrentLinkedQueue<Rule> rules;

    public RuleManager() {
        this.rules = new ConcurrentLinkedQueue<Rule>();
    }

    // 匹配是否有已经设定的解析规则，如果有，则根据分发模式给出应答地址，否则返回null交由上游DNS服务器进行解答
    public Address matches(int now, long ip, String domainName) {

        for (Rule rule : rules) {
            if (rule.getEnabled() && rule.matches(now, ip, domainName)) {
                return rule.dispatchAddress(ip);
            }
        }
        return null;
    }

    public void remove(Rule rule) {
        rules.remove(rule);
    }

    public void enable(Long ruleId) {
        for (Rule rule : rules) {
            if (rule.getId() != null && rule.getId().equals(ruleId)) {
                rule.setEnabled(true);
                break;
            }
        }
    }

    public void disable(Long ruleId) {
        for (Rule rule : rules) {
            if (rule.getId() != null && rule.getId().equals(ruleId)) {
                rule.setEnabled(false);
                break;
            }
        }
    }

    public void add(Rule rule) {
        this.rules.add(rule);
    }

    public List<Rule> getDefaultRules() {
        List<Rule> list = new ArrayList<>();
        for (Rule rule : rules) {
            if (rule.getType() != null && rule.getType().equals("default")) {
                list.add(rule);
            }
        }
        return list;
    }


    // 初始时默认的域名映射
    private void firstLoad() {
        String mapIp = "127.0.0.1";
        try {
            mapIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("获取本机ip错误");
        }

        List<String> realmNames = Arrays.asList("时钟服务_pool.ntp.org",
                "Nebula系统_galaxy.nebula.com",
                "Harbor系统_registry-1.docker.io",
                "Push Gateway_push.gateway.com");

        Address address = new Address();
        address.setType("IPv4");
        address.setAddress(mapIp);

        for (String realmName : realmNames) {
            String[] str = realmName.split("_");
            Rule rule = new Rule();
            rule.setName(str[0]);
            rule.setType("default");
            rule.setMatchMode("contains");
            rule.setMatchName(str[1]);
            rule.setPriority(0);
            rule.setEnabled(true);
            rule.setDispatchMode(Rule.DispatchMode.ROUND_ROBIN.getName());
            rule.setAddresses(Collections.singletonList(address));

            this.rules.add(rule);
        }

    }

    // 初始化，从数据库中加载全部设定的应答规则
    public void init() {
        try {
            firstLoad();
            List<Rule> ruleList = dnsManager.getRules();
            if (ruleList != null) {
                for (int i = ruleList.size() - 1; i >= 0; i--) {
                    Rule rule = ruleList.get(i);
                    List<Address> addresses = dnsManager.getAddressByRuleId(rule.getId());
                    rule.setAddresses(addresses);
                    this.rules.add(rule);
                }
            }
            logger.info("load {} rules from database...", rules.size());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
