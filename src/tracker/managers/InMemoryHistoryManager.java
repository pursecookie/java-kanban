package tracker.managers;

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
        }
        historyMap.put(task.getId(), linkLast(task));
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        removeNode(historyMap.remove(id));
    }

    Node<Task> linkLast(Task task) {
        Node<Task> tail = historyLinkedList.tail;
        Node<Task> node = new Node<>(tail, task, null);
        historyLinkedList.tail = node;
        if (tail == null) {
            historyLinkedList.head = node;
        } else {
            tail.next = node;
        }
        historyLinkedList.size++;
        return node;
    }

    List<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        Node<Task> node = historyLinkedList.head;
        while (node != null) {
            tasks.add(node.data);
            node = node.next;
        }
        return tasks;
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