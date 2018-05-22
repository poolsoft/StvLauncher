
package com.xstv.launcher.provider.db;

interface Migration<T> {
    void migrate(T instance);
}
