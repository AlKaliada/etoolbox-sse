package com.exadel.etoolbox.sse.core.servlets;

import ch.qos.logback.classic.Level;
import com.exadel.etoolbox.sse.core.dto.AsyncContextWrapper;
import com.exadel.etoolbox.sse.core.util.ResourceResolverUtil;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardContextSelect;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletAsyncSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
        service = Servlet.class,
        property = {
                HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN
                        + "=/services/sse",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT
                        + "=("
                        + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME
                        + "=org.apache.sling)",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_ASYNC_SUPPORTED
                        + "=true",
                HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED
                        + "=true"
        },
        scope = ServiceScope.PROTOTYPE)
@HttpWhiteboardContextSelect("(osgi.http.whiteboard.context.name=org.apache.sling)")
@HttpWhiteboardServletAsyncSupported
public class SSE extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSE.class);

    public static final Queue<AsyncContextWrapper> ongoingRequests = new ConcurrentLinkedQueue<>();
    private static final List<Level> DEFAULT_LEVELS = List.of(Level.ALL);

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/event-stream");
        response.setHeader("Dispatcher", "no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");


        final AsyncContext ac = request.startAsync();
        ac.setTimeout(60 * 1000);
        ac.addListener(new AsyncListener() {
            @Override public void onComplete(AsyncEvent event) throws IOException {
//                ongoingRequests.remove(ac);
            }
            @Override public void onTimeout(AsyncEvent event) throws IOException {
//                ongoingRequests.remove(ac);
            }
            @Override public void onError(AsyncEvent event) throws IOException {
//                ongoingRequests.remove(ac);
            }
            @Override public void onStartAsync(AsyncEvent event) throws IOException {}
        });
        try (ResourceResolver resourceResolver = ResourceResolverUtil.getResourceResolver(resolverFactory)) {
            ongoingRequests.add(new AsyncContextWrapper(ac, request.getRemoteUser(), getLevels(resourceResolver, request.getRemoteUser())));
        }
    }

    private List<Level> getLevels(ResourceResolver resourceResolver, String user) {
        Resource resource = resourceResolver.getResource("/var/sse/users");
        if (resource == null) {
            return DEFAULT_LEVELS;
        }
        String[] levels = resource.getValueMap().get(user, String[].class);
        if (levels != null && levels.length > 0) {
            return Stream.of(levels).map(Level::toLevel).collect(Collectors.toList());
        }
        return DEFAULT_LEVELS;
    }

    @Activate
    private void activate() {
        final Runnable notifier = () -> {
            final Iterator<AsyncContextWrapper> iterator = ongoingRequests.iterator();

            while (iterator.hasNext()) {
                AsyncContextWrapper asyncContextWrapper = iterator.next();

                final ServletResponse res = asyncContextWrapper.getAsyncContext().getResponse();
                Queue<String> messages = asyncContextWrapper.getMessages();
                PrintWriter out;
                try {
                    out = res.getWriter();
                    if (!messages.isEmpty()) {
                        String join = String.join("!!!", messages);
                        messages.clear();
                        out.write("data:" + join + "\n\n");
                    }

                    if (out.checkError()) {
//                            iterator.remove();
                    }
                } catch (IOException e) {
//                        iterator.remove();
                    LOGGER.error("qwer", e);
                }
            }
        };
        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
        service.scheduleAtFixedRate(notifier, 1, 1, TimeUnit.SECONDS);
    }
}