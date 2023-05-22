package xyz.andrick.figures;

import javafx.beans.property.*;

import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ObservableTimer {
    private volatile boolean running=false;
    ScheduledExecutorService executorService;
    ScheduledExecutorService timerService;
    private Runnable userTickFunction;
    private SimpleLongProperty interval, precision;
    private SimpleLongProperty timeElapsed;
    private ReadOnlyIntegerProperty maxCalls;
    private int numCalls = 0;

    public ObservableTimer(long inIntervalMillieconds, int inMaxCalls, Runnable _userTickFunction){
        interval = new SimpleLongProperty(inIntervalMillieconds);
        maxCalls = new ReadOnlyIntegerWrapper(inMaxCalls);
        timeElapsed = new SimpleLongProperty(0);
        precision = new SimpleLongProperty(1);
        userTickFunction = _userTickFunction;
    }

    public void start(){
        if(running)
            return;
        running = true;
        long delay = (long) (interval.get() - (timeElapsed.get()%interval.get()));

        executorService = Executors.newSingleThreadScheduledExecutor();
        timerService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate( this::onTick, delay, interval.get(), TimeUnit.MILLISECONDS);
        timerService.scheduleAtFixedRate(this::updateClock, precision.get(),precision.get(), TimeUnit.MILLISECONDS);
    }

    private void updateClock(){
        timeElapsed.set(timeElapsed.getValue() + precision.getValue());
        int maxNumCalls = maxCalls.getValue();

        if(maxNumCalls==-1)
            return;

        if (numCalls> maxCalls.getValue())
        {
            running=false;
            timerService.shutdownNow();
        }

    }
    private void onTick(){
        if(!running) {
            return;
        }
        userTickFunction.run();
        timeElapsed.set(0);
        numCalls++;
        if(maxCalls.getValue()==-1)
            return;

        if(numCalls>=maxCalls.getValue())
        {
            executorService.shutdownNow();
            running = false;
        }
    }

    public void pause(){
        running = false;
        executorService.shutdownNow();
        timerService.shutdownNow();
    }

    public boolean isRunning() {
        return running;
    }

    public void stop(){
        running = false;
        executorService.shutdownNow();
        timerService.shutdownNow();
        timeElapsed.set(0);
    }

    public void restart(){
        stop();
        start();
    }

    public void resume(){
        start();
    }

    public SimpleLongProperty timeElapsedProperty() {
        return timeElapsed;
    }

    public double getTimeElapsed() {
        if (timeElapsed == null)
            timeElapsed = new SimpleLongProperty(0);
        return timeElapsed.get();
    }

    public void killAll() {
        executorService.shutdownNow();
        timerService.shutdownNow();
    }

    public void setUserTickFunction(Runnable userTickFunction) {
        this.userTickFunction = userTickFunction;
    }

    public void setInterval(long interval) {
        this.interval.set(interval);
    }
}
