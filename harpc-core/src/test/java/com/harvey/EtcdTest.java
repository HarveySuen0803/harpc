package com.harvey;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.options.GetOption;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Harvey Suen
 */
public class EtcdTest {
    @Test
    public void basicUsage() throws ExecutionException, InterruptedException {
        Client etcdClient = Client.builder().endpoints("http://127.0.0.1:2379").build();
        KV kvClient = etcdClient.getKVClient();
        ByteSequence key = ByteSequence.from("k1", StandardCharsets.UTF_8);
        ByteSequence val = ByteSequence.from("v1", StandardCharsets.UTF_8);
        
        CompletableFuture<PutResponse> putFuture = kvClient.put(key, val);
        PutResponse putResponse = putFuture.get();
        System.out.println(putResponse);
        
        CompletableFuture<GetResponse> getFuture = kvClient.get(key);
        GetResponse getResponse = getFuture.get();
        System.out.println(getResponse);
        getResponse.getKvs().forEach(kv -> System.out.println("Value retrieved: " + kv.getValue().toString(StandardCharsets.UTF_8)));
        
        CompletableFuture<DeleteResponse> delFuture = kvClient.delete(key);
        DeleteResponse delResponse = delFuture.get();
        System.out.println(delResponse);
    }
    
    @Test
    public void put() throws ExecutionException, InterruptedException {
        Client etcdClient = Client.builder().endpoints("http://127.0.0.1:2379").build();
        KV kvClient = etcdClient.getKVClient();
        
        ByteSequence key = ByteSequence.from("k1", StandardCharsets.UTF_8);
        
        ByteSequence val = ByteSequence.from("v1", StandardCharsets.UTF_8);
        kvClient.put(key, val).get();
        
        val = ByteSequence.from("v2", StandardCharsets.UTF_8);
        kvClient.put(key, val).get();
        
        val = ByteSequence.from("v3", StandardCharsets.UTF_8);
        kvClient.put(key, val).get();
        
        val = ByteSequence.from("v4", StandardCharsets.UTF_8);
        kvClient.put(key, val).get();
        
        kvClient.close();
    }
    
    @Test
    public void get() throws ExecutionException, InterruptedException {
        Client etcdClient = Client.builder().endpoints("http://127.0.0.1:2379").build();
        KV kvClient = etcdClient.getKVClient();
        
        ByteSequence key = ByteSequence.from("k1", StandardCharsets.UTF_8);
        
        GetOption getOption = GetOption.builder()
            .withSortField(GetOption.SortTarget.MOD)
            .withSortOrder(GetOption.SortOrder.DESCEND)
            .withKeysOnly(false)
            .withSerializable(false)
            .build();
        
        List<KeyValue> kvList = kvClient.get(key, getOption).get().getKvs();
        for (KeyValue kv : kvList) {
            System.out.printf("%s, %s, %s%n", kv.getKey(), kv.getValue(), kv.getModRevision());
        }
        
        kvClient.close();
    }
}
