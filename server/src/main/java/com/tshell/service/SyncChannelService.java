package com.tshell.service;

import cn.hutool.core.collection.CollUtil;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

/**
 * @author TheBlind
 */
@ApplicationScoped
public class SyncChannelService {


    public static final Map<String, List<String>> syncChannels = HashMap.newHashMap(5);
    public static final Map<String, String> sshChannelIdMapChannels = HashMap.newHashMap(5);


    public void bind(String channelId, String sshChannelId) {
        List<String> list = syncChannels.computeIfAbsent(channelId, (k)->new ArrayList<>(3));
        if (sshChannelIdMapChannels.containsKey(sshChannelId)) {
            String s = sshChannelIdMapChannels.get(sshChannelId);
            remove(s);
        }
        sshChannelIdMapChannels.put(sshChannelId, channelId);
        list.add(sshChannelId);
    }

    public void remove(String sshChannelId) {
        String channelId = sshChannelIdMapChannels.get(sshChannelId);
        List<String> list = syncChannels.get(channelId);
        if(CollUtil.isNotEmpty(list)){
            list.remove(sshChannelId);
            sshChannelIdMapChannels.remove(sshChannelId);
        }
    }


    public Optional<List<String>> getSshChannelIds(String sshChannelId) {
        String channelId = sshChannelIdMapChannels.get(sshChannelId);
        return Optional.ofNullable(syncChannels.get(channelId));
    }


    public String get(String sshChannelId) {
        return sshChannelIdMapChannels.get(sshChannelId);
    }
}
