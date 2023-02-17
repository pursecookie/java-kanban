package tracker.service;

import tracker.model.Node;
import tracker.model.Task;

import java.util.*;

class CustomLinkedList<T> {
    public Node<T> head;
    public Node<T> tail;
    int size = 0;
}

public class InMemoryHistoryManager implements HistoryManager {
    CustomLinkedList<Task> historyLinkedList = new CustomLinkedList<>();
    Map<Integer, Node<Task>> historyMap = new HashMap<>();

    @Override
    public void add(Task task) {
        if (historyMap.containsKey(task.getId())) {
            remove(task.getId());
            linkLast(task);
        } else {
            linkLast(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks(historyLinkedList);
    }

    @Override
    public void remove(int id) {
        removeNode(historyMap.get(id));
        historyMap.remove(id);
    }

    void linkLast(Task task) {
        Node<Task> tail = historyLinkedList.tail;
        Node<Task> node = new Node<>(tail, task, null);
        historyLinkedList.tail = node;
        if (tail == null) {
            historyLinkedList.head = node;
        } else {
            tail.next = node;
        }
        historyLinkedList.size++;
        historyMap.put(task.getId(), node);
    }

    List<Task> getTasks(CustomLinkedList<Task> linkedList) {
        Task[] result = new Task[linkedList.size];
        int i = 0;
        for (Node<Task> x = linkedList.head; x != null; x = x.next) {
            result[i++] = x.data;
        }
        return new ArrayList<>(Arrays.asList(result));
    }

    void removeNode(Node<Task> node) {
        if (node == null) {
            return;
        }
        final Node<Task> next = node.next;
        final Node<Task> prev = node.prev;

        if (prev == null) {
            historyLinkedList.head = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            historyLinkedList.tail = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }

        node.data = null;
        historyLinkedList.size--;
    }
}