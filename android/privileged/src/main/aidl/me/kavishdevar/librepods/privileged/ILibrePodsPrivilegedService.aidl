package me.kavishdevar.librepods.privileged;

interface ILibrePodsPrivilegedService {
    boolean setBluetoothMetadata(String address, int key, in byte[] value);
    byte[] getBluetoothMetadata(String address, int key);
    boolean setConnectionPolicy(String address, int profile, int policy);
    int getServiceUid();
    void destroy();
}
