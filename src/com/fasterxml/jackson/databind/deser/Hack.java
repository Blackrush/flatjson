package com.fasterxml.jackson.databind.deser;

import java.util.Collection;

public final class Hack {
    private Hack() {}

    public static Class<? extends Collection> findCollectionCallback(String name) {
        return BasicDeserializerFactory._collectionFallbacks.get(name);
    }
}
