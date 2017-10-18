/* 
 * Copyright © 2017-2017 Elton Santos Vianna. Distributed under GNU General Public License v3.0.
 */
package com.esv.net.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.esv.utile.logging.Logger;
import com.esv.utile.utils.PropertiesUtils;

/**
 * 
 * @author Elton S. Vianna <elton.vianna@yahoo.co.uk>
 * @version 1.0
 * @since 30/09/2017
 */
public class MicroServer {

    private static final Logger LOGGER = Logger.getLogger(MicroServer.class);

    private static final int serverPort;
    private static final Executor threadPool;
    private static final long startTime;
    private static AtomicBoolean started = new AtomicBoolean(false);

    static {
        startTime = System.currentTimeMillis();
        try {
            serverPort = PropertiesUtils.getIntProperty("microserver.port", 80);
            final Integer maxThreads = PropertiesUtils.getIntProperty("microserver.max.threads", 100);
            threadPool = Executors.newFixedThreadPool(maxThreads);
            LOGGER.info(() -> "Starting micro server at port: " + serverPort + ", max threads: " + maxThreads);
        } catch (Exception e) {
            LOGGER.fatal("Unexpected error on initialization", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * <p>
     * Starts the micro server, creating a thread pool to handle
     * resources and rest service requests
     * </p>
     * 
     * @param args
     * @throws RuntimeException
     */
    public static void run(String... args) {
        MicroServer.started.set(true);
        try (final ServerSocket serverSocket = new ServerSocket(MicroServer.serverPort)) {
            LOGGER.info(() -> "Micro server successfully started in " + (System.currentTimeMillis() - startTime) + " milliseconds");
            while (MicroServer.isStarted()) {
                final Socket socket = serverSocket.accept();
                MicroServer.threadPool.execute(() -> SocketHandler.handle(socket));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failure to start the micro server", e);
        }
    }

    /**
     * @return
     */
    public static boolean isStarted() {
        return MicroServer.started.get();
    }
    
    /**
     * 
     */
    public static void stop() {
        MicroServer.started.set(false);
    }
}