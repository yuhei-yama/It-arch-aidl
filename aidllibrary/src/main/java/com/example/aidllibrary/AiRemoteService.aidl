// AiRemoteService.aidl
package com.example.it_arch_aidl;

// Declare any non-default types here with import statements
import com.example.it_arch_aidl.RemoteServiceCallback;

interface AiRemoteService {
    void registerCallback(RemoteServiceCallback cb);
    void unregisterCallback(RemoteServiceCallback cb);
}