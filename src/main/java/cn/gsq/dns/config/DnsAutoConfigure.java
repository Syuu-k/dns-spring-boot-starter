package cn.gsq.dns.config;


import cn.gsq.dns.config.condition.DnsCondition;
import cn.gsq.dns.protocol.*;
import cn.gsq.dns.service.DnsServer;
import cn.gsq.dns.stat.StatManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Conditional(DnsCondition.class)
@EnableConfigurationProperties({DnsServerProperties.class, DnsStatProperties.class})
public class DnsAutoConfigure {

    @Autowired
    DnsStatProperties dnsStatProperties;

    @Bean
    public DnsServer getDnsServer() {
        return new DnsServer();
    }

    @Bean
    public StatManager getStatManager() {
        return new StatManager(dnsStatProperties);
    }

    @Bean
    public RuleManager getRuleManager() {
        return new RuleManager();
    }

    @Bean
    public NameServer getNameServer() {
        return new NameServer();
    }

    @Bean
    public RecursiveResolver getRecursiveResolver() {
        return new RecursiveResolver();
    }

    /**
     * @Description : 管理recursiveResolveWorker的线程池
     * @Param : []
     * @Return : org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
     * @Author : syu
     * @Date : 2024/4/12
     */
    @Bean
    public ThreadPoolTaskExecutor recursiveResolveWorkerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(100); // 设置队列容量
        executor.setThreadNamePrefix("recursive-resolve-worker-");
        executor.initialize();
        return executor;
    }

    /**
     * @Description : 管理nameResolveWorker的线程池
     * @Param : []
     * @Return : org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
     * @Author : syu
     * @Date : 2024/4/12
     */
    @Bean
    public ThreadPoolTaskExecutor nameResolveWorkerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 4);
        executor.setQueueCapacity(100); // 设置队列容量
        executor.setThreadNamePrefix("name-resolve-worker-");
        executor.initialize();
        return executor;
    }

    @Bean
    public RecursiveResolveWorker[] getRecursiveResolveWorkers(ThreadPoolTaskExecutor recursiveResolveWorkerExecutor) {
        RecursiveResolveWorker[] resolveWorkers = new RecursiveResolveWorker[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < resolveWorkers.length; i++) {
            RecursiveResolveWorker worker = new RecursiveResolveWorker(getNameServer(), getRecursiveResolver());
            recursiveResolveWorkerExecutor.execute(worker);
            resolveWorkers[i] = worker;
        }
        return resolveWorkers;
    }

    @Bean
    public NameResolveWorker[] getNameResolveWorkers(ThreadPoolTaskExecutor nameResolveWorkerExecutor) {
        NameResolveWorker[] resolveWorkers = new NameResolveWorker[Runtime.getRuntime().availableProcessors() * 2];
        for (int i = 0; i < resolveWorkers.length; i++) {
            NameResolveWorker worker = new NameResolveWorker(getNameServer(), getRuleManager(), getRecursiveResolver(), getStatManager());
            nameResolveWorkerExecutor.execute(worker);
            resolveWorkers[i] = worker;
        }
        return resolveWorkers;
    }


}
