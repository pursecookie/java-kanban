package tracker.models;

public class Counter {
    public int counter = 1;

    public int count() {
        return counter++;
    }
}