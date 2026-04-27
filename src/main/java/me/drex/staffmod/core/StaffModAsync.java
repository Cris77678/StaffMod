package me.drex.staffmod.core;

import me.drex.staffmod.StaffMod;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StaffModAsync {

    // Hilo dedicado a tareas de escritura/lectura pesadas (I/O).
    // Usa un cálculo inteligente basado en los procesadores disponibles del host.
    private static final ExecutorService WORKER_POOL = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
        r -> {
            Thread t = new Thread(r, "StaffMod-Worker-" + new AtomicInteger().getAndIncrement());
            t.setDaemon(true); // Permite al servidor cerrarse limpiamente sin esperar a que el hilo muera
            return t;
        }
    );

    // Hilo dedicado a tareas programadas (ej. limpiar expiraciones de mutes/bans, autoguardados).
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(
        r -> {
            Thread t = new Thread(r, "StaffMod-Scheduler");
            t.setDaemon(true);
            return t;
        }
    );

    /**
     * Ejecuta una tarea pesada en un hilo asíncrono para no dropear TPS.
     */
    public static void runAsync(Runnable runnable) {
        WORKER_POOL.execute(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                StaffMod.LOGGER.error("[StaffMod] Excepción no controlada en hilo Worker:", e);
            }
        });
    }

    /**
     * Programa una tarea repetitiva asíncrona (ej. cada 5 minutos).
     */
    public static void scheduleAsync(Runnable runnable, long delay, long period, TimeUnit unit) {
        SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                StaffMod.LOGGER.error("[StaffMod] Excepción en hilo Scheduler:", e);
            }
        }, delay, period, unit);
    }

    /**
     * Apagado seguro de los hilos al detener el servidor para evitar Memory Leaks.
     */
    public static void shutdown() {
        StaffMod.LOGGER.info("[StaffMod] Deteniendo hilos asíncronos...");
        SCHEDULER.shutdown();
        WORKER_POOL.shutdown();
        try {
            if (!WORKER_POOL.awaitTermination(3, TimeUnit.SECONDS)) {
                WORKER_POOL.shutdownNow();
            }
        } catch (InterruptedException e) {
            WORKER_POOL.shutdownNow();
        }
    }
}