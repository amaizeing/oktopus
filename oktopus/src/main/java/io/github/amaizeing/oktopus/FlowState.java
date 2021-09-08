package io.github.amaizeing.oktopus;

class FlowState {

    private volatile State state = State.INITIATE;

    void ready() {
        state = State.READY;
    }

    void running() {
        state = State.RUNNING;
    }

    void complete() {
        state = State.COMPLETED;
    }

    void build() {
        state = State.BUILDING;
    }

    void error() {
        state = State.ERROR;
    }

    boolean isInitiate() {
        return state == State.INITIATE;
    }

    boolean isReady() {
        return state == State.READY;
    }

    boolean isRunning() {
        return state == State.RUNNING;
    }

    boolean isComplete() {
        return state == State.COMPLETED;
    }

    boolean isError() {
        return state == State.ERROR;
    }

    enum State {
        INITIATE, BUILDING, READY, RUNNING, ERROR, COMPLETED
    }

}
