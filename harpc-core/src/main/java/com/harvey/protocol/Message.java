package com.harvey.protocol;

import lombok.Data;

/**
 * @author Harvey Suen
 */
@Data
public class Message<T> {
    private Header header;
    
    private T body;
    
    @Data
    public static class Header {
        private byte[] magicNum; // 4B
        
        private byte version; // 1B
        
        private byte serializerTypeKey; // 1B

        private byte messageClazzKey; // 1B
        
        private byte status; // 1B
        
        private int sequenceId; // 4B
        
        private int bodyLength; // 4B
    }
}
