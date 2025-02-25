package cn.gsq.dns.config.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DnsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {
        String isOpen = conditionContext.getEnvironment().getProperty("galaxy.dns.server.enable");
        return Boolean.parseBoolean(isOpen);
    }
}
