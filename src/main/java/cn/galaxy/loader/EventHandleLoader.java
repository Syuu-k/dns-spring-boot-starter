package cn.galaxy.loader;

import cn.gsq.common.AbstractInformationLoader;
import cn.hutool.core.collection.CollUtil;

import java.util.List;

public class EventHandleLoader extends AbstractInformationLoader {

    /**
     * @Description : agent进程不启动
     * @Param : []
     * @Return : boolean
     * @Author : gsq
     * @Date : 15:21
     * @note : An art cell !
     **/
    public boolean isEnable() {
        return !System.getenv("ROLE").equals("agent");
    }

    //将dns中处理LDAP的事件加到容器
    @Override
    public List<String> eventHandleSupply() {
        return CollUtil.newArrayList("cn.gsq.dns.event");
    }



}

