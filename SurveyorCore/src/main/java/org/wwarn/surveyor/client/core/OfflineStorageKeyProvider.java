package org.wwarn.surveyor.client.core;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * Created by nigelthomas on 03/07/2015.
 */
class OfflineStorageKeyProvider implements IsSerializable, Serializable {
    private String currentKey;
    private String oldKey;

    private OfflineStorageKeyProvider() {
    }

    public OfflineStorageKeyProvider(String currentKey, String oldKey) {
        this.currentKey = currentKey;
        this.oldKey = oldKey;
    }

    public OfflineStorageKeyProvider(String dataSourceHash) {
        this.currentKey = dataSourceHash;
        this.oldKey = null;
    }

    public String getCurrentKey() {
        return currentKey;
    }

    public String getOldKey() {
        return oldKey;
    }

    @Override
    public String toString() {
        return "OfflineStorageKeyProvider{" +
                "currentKey='" + currentKey + '\'' +
                ", oldKey='" + oldKey + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OfflineStorageKeyProvider that = (OfflineStorageKeyProvider) o;

        if (!currentKey.equals(that.currentKey)) return false;
        return !(oldKey != null ? !oldKey.equals(that.oldKey) : that.oldKey != null);

    }

    @Override
    public int hashCode() {
        int result = currentKey.hashCode();
        result = 31 * result + (oldKey != null ? oldKey.hashCode() : 0);
        return result;
    }
}
