
package com.xstv.desktop.app.db;

interface Migration<T> {
    void migrate(T instance);
}
