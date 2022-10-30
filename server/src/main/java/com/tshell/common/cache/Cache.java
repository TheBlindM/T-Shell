package com.tshell.common.cache;

import java.util.Optional;

public interface Cache<K,V> {

    Optional<Object> get(K key);
    void put(K key, V value);



}
