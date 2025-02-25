package cn.gsq.dns.event;

import cn.gsq.common.EventHandleClass;
import cn.gsq.common.EventHandleMethod;
import cn.gsq.common.config.event.GalaxyGeneralEvent;
import cn.gsq.dns.protocol.entity.Address;
import cn.gsq.dns.protocol.entity.Rule;
import cn.gsq.dns.service.DnsManager;
import cn.gsq.dns.service.DnsServer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@EventHandleClass
public class LdapEvent {

    @Autowired
    DnsManager dnsManager;

    @Autowired
    DnsServer dnsServer;

    @EventHandleMethod(module = "ldap_dns")
    private void handleLdap(GalaxyGeneralEvent event) {
        String hostname = (String)event.getSource();
        List<Rule> rules = dnsManager.getRules();
        Rule rule = new Rule();
        Long id;
        boolean flag = false;
        // 查询ldap的dns代理是否已经存在
        for (Rule r : rules) {
            if (r.getName().equals("LDAP") && r.getType().equals("default")) {
                flag = true;
                rule = r;
                break;
            }
        }
        // 如果存在,通过id对已经存在的ldap进行修改
        if (flag) {
            id = rule.getId();
            dnsServer.removeRule(rule);
        } else {
            id = (long) Math.abs(UUID.randomUUID().hashCode());
        }
        Address address = new Address();
        address.setRuleId(id);
        address.setAddress(hostname);
        address.setType("IPv4");
        rule.setId(id);
        rule.setName("LDAP");
        rule.setType("default");
        rule.setMatchName("sugon.ldap.cn");
        rule.setMatchMode("contains");
        rule.setPriority(0);
        rule.setEnabled(true);
        rule.setDispatchMode(Rule.DispatchMode.ROUND_ROBIN.getName());
        rule.setAddresses(Collections.singletonList(address));
        dnsManager.addRule(rule);
        dnsServer.addRule(rule);
//        System.out.println("第一个处理器处理一个系统事件：" + event.getClass().getSimpleName());
    }
}

