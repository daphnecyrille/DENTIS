package com.dentis.DENTIS.config;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Sets maxParameterCount = -1 (unlimited) on every ApplicationContext refresh,
 * including DevTools hot-restarts where WebServerFactoryCustomizer does not re-run.
 */
@Component
public class TomcatFileCountFix {

    private static final Logger log = LoggerFactory.getLogger(TomcatFileCountFix.class);

    private final ApplicationContext applicationContext;

    public TomcatFileCountFix(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void removeFileCountLimit() {
        if (!(applicationContext instanceof ServletWebServerApplicationContext swCtx)) {
            log.warn("TomcatFileCountFix: not a ServletWebServerApplicationContext, skipping");
            return;
        }
        if (!(swCtx.getWebServer() instanceof TomcatWebServer tws)) {
            log.warn("TomcatFileCountFix: web server is not TomcatWebServer, skipping");
            return;
        }
        Connector[] connectors = tws.getTomcat().getService().findConnectors();
        for (Connector connector : connectors) {
            int before = connector.getMaxParameterCount();
            connector.setMaxParameterCount(-1);
            log.info("TomcatFileCountFix: connector {} maxParameterCount {} -> -1 (unlimited)",
                    connector.getPort(), before);
        }
    }
}
