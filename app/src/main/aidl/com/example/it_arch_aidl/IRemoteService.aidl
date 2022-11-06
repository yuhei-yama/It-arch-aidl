// IRemoteService.aidl
package com.example.it_arch_aidl;

// Declare any non-default types here with import statements

interface IRemoteService{
    void registerCallback(IRemoteServiceCallback cb);
    void unregisterCallback(IRemoteServiceCallback cb);
}