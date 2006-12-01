
package com.sun.sgs.impl.kernel;

import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.NameNotBoundException;

import com.sun.sgs.impl.util.LoggerWrapper;

import com.sun.sgs.kernel.KernelRunnable;

import com.sun.sgs.service.DataService;
import com.sun.sgs.service.Service;
import com.sun.sgs.service.TaskService;
import com.sun.sgs.service.TransactionProxy;
import com.sun.sgs.service.TransactionRunner;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This implementation of <code>KernelRunnable</code> is one of two runnables
 * used when an application is starting up. This runnable is resposible for
 * configuring all of the application's <code>Service</code>s, and then
 * scheduling a <code>AppStartupRunner</code> to start the application.
 * <p>
 * This runnable must be run in a transactional context.
 *
 * @since 1.0
 * @author Seth Proctor
 */
class ServiceConfigRunner implements KernelRunnable {

    // logger for this class
    private final static LoggerWrapper logger =
        new LoggerWrapper(Logger.getLogger(ServiceConfigRunner.
                                           class.getName()));

    // the reference back to the kernel
    private final Kernel kernel;

    // the services that this runnable will configure
    private final List<Service> services;

    // the proxy that provides transaction state
    private final TransactionProxy proxy;

    // the listener used to boot an application
    private final AppListener app;

    // the name of the application
    private final String appName;

    // the properties that are passed to the app on startup
    private final Properties appProperties;

    /**
     * Creates an instance of <code>ServiceConfigRunner</code>.
     *
     * @param kernel the kernel that started this runnable
     * @param services the <code>Service</code>s to configure, in order
     *                 of how they will be configured
     * @param proxy the proxy used to access the current transaction
     * @param app the <code>AppListener</code> used to start the application
     * @param appName the name of the application being started
     * @param appProperties the <code>Properties</code> provided to the
     *                      application on startup
     */
    ServiceConfigRunner(Kernel kernel, List<Service> services,
                        TransactionProxy proxy, AppListener app,
                        String appName, Properties appProperties) {
        this.kernel = kernel;
        this.services = services;
        this.proxy = proxy;
        this.app = app;
        this.appName = appName;
        this.appProperties = appProperties;
    }

    /**
     * Configures each of the <code>Service</code>s in order, in preparation
     * for starting up an application. At completion, this schedules an
     * <code>AppStartupRunner</code> to finish application startup.
     *
     * @throws Exception if any failure occurs during service configuration
     */
    public void run() throws Exception {
        if (logger.isLoggable(Level.CONFIG))
            logger.log(Level.CONFIG, "{0}: starting service config", appName);

        // initialize the services in the correct order, adding them to the
        // registry as we go
        ComponentRegistryImpl serviceComponents = new ComponentRegistryImpl();
        for (Service service : services) {
            try {
                service.configure(serviceComponents, proxy);
            } catch (Exception e) {
                if (logger.isLoggable(Level.CONFIG))
                    logger.log(Level.CONFIG, "{1}: failed to configure " +
                               " service {2}", e, appName, service.getName());
                throw e;
            }
            serviceComponents.addComponent(service);
        }

        // At this point the services are now configured, so the final step
        // is to boot the application, making sure to manage the app listener
        // if this has never happened before. This uses the data and task
        // services, which are always first and second (respectively) in
        // the service list
        Iterator<Service> serviceIterator = services.iterator();
        DataService dataService = (DataService)(serviceIterator.next());
        TaskService taskService = (TaskService)(serviceIterator.next());

        try {
            // test to see if this name is already used...
            dataService.getBinding(Kernel.LISTENER_BINDING, AppListener.class);
        } catch (NameNotBoundException nnbe) {
            // ...and if it's not, bind the listener
            dataService.setBinding(Kernel.LISTENER_BINDING, app);
        }

        // get the context so we can provide it to the next runnable, which
        // runs in a new transaction, and is responsible for booting the app
        AppKernelAppContext appContext =
            (AppKernelAppContext)(proxy.getCurrentOwner().getContext());
        AppStartupRunner startupRunner =
            new AppStartupRunner(appContext, appProperties, kernel);
        TransactionRunner transactionRunner =
            new TransactionRunner(startupRunner);
        try {
            appContext.setServices(serviceComponents);
            taskService.scheduleNonDurableTask(transactionRunner);
        } catch (Exception e) {
            if (logger.isLoggable(Level.CONFIG))
                logger.log(Level.CONFIG, "{0}: failed to schedule app " +
                           "startup task", e, appName);
            throw e;
        }

        if (logger.isLoggable(Level.CONFIG))
            logger.log(Level.CONFIG, "{0}: finished service config runner",
                       appName);
    }

}
