package eu.arrowhead.core.serviceregistry_sqlite;


import com.github.danieln.dnssdjava.*;
import org.apache.log4j.Logger;

import java.util.*;

public class PingProvidersTask extends TimerTask {

    private static Logger log = Logger.getLogger(ServiceRegistry.class.getName());

    @Override
    public void run() {
        log.debug("Cleaning up SR SQLite at " + new Date().toString());
        pingAndRemoveServices();
    }

    public void pingAndRemoveServices() {

       }
}