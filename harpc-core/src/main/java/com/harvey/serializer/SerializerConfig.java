package com.harvey.serializer;

import lombok.Data;

/**
 * @author Harvey Suen
 */
@Data
public class SerializerConfig {
    private String serializerType = SerializerConst.JSON;
}
