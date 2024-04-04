package com.exadel.etoolbox.sse.core.util;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

public class ResourceResolverUtil {

    private static final String SUBSERVICE_NAME = "sseWriteService";

    private ResourceResolverUtil() {
        throw new AssertionError();
    }

    public static ResourceResolver getResourceResolver(ResourceResolverFactory factory) {
        Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);
        paramMap.put(ResourceResolverFactory.USER, "sse_system_user");
        try {
            return factory.getServiceResourceResolver(paramMap);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
    }

    public static void cleanUp(ResourceResolver resolver, Session session) {
        if (resolver != null && resolver.isLive()) {
            resolver.close();
        }
        if (session != null && session.isLive()) {
            session.logout();
        }
    }
}