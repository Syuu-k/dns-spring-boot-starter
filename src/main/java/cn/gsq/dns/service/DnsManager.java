package cn.gsq.dns.service;

import cn.gsq.dns.protocol.entity.Address;
import cn.gsq.dns.protocol.entity.Rule;
import cn.gsq.dns.protocol.entity.UpStreamDns;

import java.util.List;
import java.util.Set;

public interface DnsManager {

    List<Rule>  getRules();

    List<Address> getAddressByRuleId(long id);

    Set<UpStreamDns> getUpStreamDns();

    void addRule(Rule rule);

}
