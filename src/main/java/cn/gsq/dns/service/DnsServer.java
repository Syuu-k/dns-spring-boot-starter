package cn.gsq.dns.service;

import cn.gsq.dns.protocol.NameServer;
import cn.gsq.dns.protocol.RecursiveResolver;
import cn.gsq.dns.protocol.RuleManager;
import cn.gsq.dns.protocol.entity.Rule;
import cn.gsq.dns.protocol.entity.UpStreamDns;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

public class DnsServer {

    @Autowired
    NameServer nameServer;

    @Autowired
    RuleManager ruleManager;

    @Autowired
    RecursiveResolver recursiveResolver;

    public void start() {
        ruleManager.init();
        nameServer.init();
        recursiveResolver.init();
    }

    /**
     * @Description : 添加上游Dns服务
     * @Param : [serverAddress]
     * @Return : void
     * @Author : syu
     * @Date : 2024/4/12
     */
    public void addUpStreamDns(InetSocketAddress serverAddress) {
        recursiveResolver.addUpstreamNameServer(serverAddress);
    }

    /**
     * @Description : 移除上游Dns服务
     * @Param : [serverAddress]
     * @Return : void
     * @Author : syu
     * @Date : 2024/4/12
     */
    public void removeUpStreamDns(InetSocketAddress serverAddress) {
        recursiveResolver.removeUpstreamNameServer(serverAddress);
    }

    /**
     * @Description : 获取当前dns服务的上游dns服务列表
     * @Param : []
     * @Return : java.util.Set<cn.gsq.dns.protocol.entity.UpStreamDns>
     * @Author : syu
     * @Date : 2024/4/22
     */
    public Set<UpStreamDns> getUpStreamDns() {
        return recursiveResolver.getUpstreamNameServer();
    }


    /**
     * @Description : 添加策略
     * @Param : [rule]
     * @Return : void
     * @Author : syu
     * @Date : 2024/4/12
     */
    public void addRule(Rule rule) {
        ruleManager.add(rule);
    }

    /**
     * @Description : 移除策略
     * @Param : [rule]
     * @Return : void
     * @Author : syu
     * @Date : 2024/4/12
     */
    public void removeRule(Rule rule) {
        ruleManager.remove(rule);
    }


    /**
     * @Description : 获取存于内存，不存于数据库中的策略
     * @Param : []
     * @Return : java.util.List<cn.gsq.dns.protocol.entity.Rule>
     * @Author : syu
     * @Date : 2024/5/10
     */
    public List<Rule> getDefaultRules() {
      return ruleManager.getDefaultRules();
    }

    /**
     * @Description : 启用策略
     * @Param : [ruleId]
     * @Return : void
     * @Author : syu
     * @Date : 2024/4/12
     */
    public void setRuleEnable(Long ruleId) {
        ruleManager.enable(ruleId);
    }

    /**
     * @Description : 停用策略
     * @Param : [ruleId]
     * @Return : void
     * @Author : syu
     * @Date : 2024/4/12
     */
    public void setRuleDisable(Long ruleId) {
        ruleManager.disable(ruleId);
    }



}
