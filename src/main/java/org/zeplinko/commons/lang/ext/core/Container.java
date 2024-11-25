package org.zeplinko.commons.lang.ext.core;

@SuppressWarnings("LombokGetterMayBeUsed")
public class Container<T> {

    private final T value;

    public Container(T value) {
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }
}
