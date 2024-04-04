package com.exadel.etoolbox.sse.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ConfigurationModel {

    @SlingObject
    private ResourceResolver resolver;

    @Self
    private SlingHttpServletRequest request;

    private boolean error = true;
    private boolean warn = true;
    private boolean info = true;
    private boolean debug = true;
    private boolean trace = true;

    private String remoteUser;

    public boolean isError() {
        return error;
    }

    public boolean isWarn() {
        return warn;
    }

    public boolean isInfo() {
        return info;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isTrace() {
        return trace;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    @PostConstruct
    private void init() {
        remoteUser = request.getRemoteUser();
        Resource resource = resolver.getResource("/var/sse/users");
        if (resource == null) {
            return;
        }
        String[] levels = resource.getValueMap().get(remoteUser, String[].class);
        if (levels != null && levels.length > 0) {
            error = Arrays.asList(levels).contains("ERROR") || Arrays.asList(levels).contains("ALL");
            warn = Arrays.asList(levels).contains("WARN") || Arrays.asList(levels).contains("ALL");
            info = Arrays.asList(levels).contains("INFO") || Arrays.asList(levels).contains("ALL");
            debug = Arrays.asList(levels).contains("DEBUG") || Arrays.asList(levels).contains("ALL");
            trace = Arrays.asList(levels).contains("TRACE") || Arrays.asList(levels).contains("ALL");
        }
    }
}
