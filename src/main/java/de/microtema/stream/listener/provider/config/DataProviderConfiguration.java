package de.microtema.stream.listener.provider.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stream-listener")
public class DataProviderConfiguration {

    private String baseUrl;

    private int limit = 100;

    private long lockDuration = 60000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public long getLockDuration() {
        return lockDuration;
    }

    public void setLockDuration(long lockDuration) {
        this.lockDuration = lockDuration;
    }
}
