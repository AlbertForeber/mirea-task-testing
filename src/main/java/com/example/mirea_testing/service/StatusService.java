package com.example.mirea_testing.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class StatusService {

    ConcurrentMap<String, String> base = new ConcurrentHashMap<>();

    public StatusService() {
        base.put("simple_user", "This is default status");
        base.put("admin_user", "This is default status");
    }

    public String upsertStatus(String username, String status) {
        base.putIfAbsent(username, status);
        return status;
    }

    public String getStatus(String username) {
        return base.get(username);
    }
}
