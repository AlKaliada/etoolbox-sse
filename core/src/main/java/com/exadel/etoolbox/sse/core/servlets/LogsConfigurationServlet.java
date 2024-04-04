package com.exadel.etoolbox.sse.core.servlets;

import ch.qos.logback.classic.Level;
import com.day.crx.JcrConstants;
import com.google.common.collect.ImmutableMap;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/services/sse/configuration",
                "sling.servlet.methods=post"
        })
public class LogsConfigurationServlet extends SlingAllMethodsServlet {

    private static final List<String> LOG_LEVELS = List.of("ERROR", "WARN", "INFO", "DEBUG", "TRACE");

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        String remoteUser = request.getRemoteUser();
        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource resource = org.apache.sling.api.resource.ResourceUtil.getOrCreateResource(
                resourceResolver,
                "/var/sse/users",
                ImmutableMap.of(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED),
                null,
                true
        );
        ModifiableValueMap modifiableValueMap = resource.adaptTo(ModifiableValueMap.class);
        if (modifiableValueMap == null) {
            return;
        }
        List<String> levels = request.getParameterMap().keySet().stream().filter(LOG_LEVELS::contains).collect(Collectors.toList());
        modifiableValueMap.put(remoteUser, levels.toArray());
        List<Level> levelsObjects = levels.stream().map(Level::toLevel).collect(Collectors.toList());
        SSE.ongoingRequests.stream()
                .filter(acw -> acw.getUser().equals(remoteUser))
                .forEach(acw -> acw.setLevels(levelsObjects));
        resourceResolver.commit();
    }
}
