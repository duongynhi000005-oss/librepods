package me.kavishdevar.librepods.privileged;

interface ILibrePodsPrivilegedService {
    boolean setBluetoothMetadata(String address, int key, in byte[] value);
    byte[] getBluetoothMetadata(String address, int key);
    int getServiceUid();
    void destroy();
}
