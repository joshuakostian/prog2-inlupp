package se.su.inlupp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ListGraph<T> implements Graph<T> {

  private Map<T, Set<Edge<T>>> connections;

  public ListGraph() {
    connections = new HashMap<>();
  }

  @Override
  public void add(T node) {
    connections.putIfAbsent(node, new HashSet<>());
  }

  @Override
  public void connect(T node1, T node2, String name, int weight) {
    if (!nodesExist(node1, node2))
      throw new NoSuchElementException();
    if (weight < 0)
      throw new IllegalArgumentException();

    Set<Edge<T>> edges = connections.get(node1);
    for (Edge<T> e : edges) {
      if (e.getDestination().equals(node2))
        throw new IllegalStateException();
    }
    connections.get(node1).add(new Edge<T>(node1, node2, name, weight));
    connections.get(node2).add(new Edge<T>(node2, node1, name, weight));
  }

  @Override
  public void setConnectionWeight(T node1, T node2, int weight) {
    if (!nodesExist(node1, node2))
      throw new NoSuchElementException();
    if (getEdgeBetween(node1, node2) == null)
      throw new NoSuchElementException();
    if (weight < 0)
      throw new IllegalArgumentException();

    for (Edge<T> e : connections.get(node1)) {
      if (e.getDestination().equals(node2))
        e.setWeight(weight);
    }
    for (Edge<T> e : connections.get(node2)) {
      if (e.getDestination().equals(node1))
        e.setWeight(weight);
    }
  }

  @Override
  public Set<T> getNodes() {
    Set<T> res = new HashSet<>();
    res.addAll(connections.keySet());
    return res;
  }

  @Override
  public Collection<Edge<T>> getEdgesFrom(T node) {
    if (!nodeExist(node))
      throw new NoSuchElementException();

    return new HashSet<>(connections.get(node));
  }

  @Override
  public Edge<T> getEdgeBetween(T node1, T node2) {
    if (!nodesExist(node1, node2))
      throw new NoSuchElementException();

    Set<Edge<T>> edges = connections.get(node1);
    for (Edge<T> e : edges) {
      if (e.getDestination().equals(node2))
        return e;
    }
    return null;
  }

  @Override
  public void disconnect(T node1, T node2) {
    if (!nodesExist(node1, node2))
      throw new NoSuchElementException();

    Edge<T> edge1 = getEdgeBetween(node1, node2);
    Edge<T> edge2 = getEdgeBetween(node2, node1);

    if (edge1 == null || edge2 == null)
      throw new IllegalStateException();

    connections.get(node1).remove(edge1);
    connections.get(node2).remove(edge2);
  }

  @Override
  public void remove(T node) {
    if (!nodeExist(node))
      throw new NoSuchElementException();

    Set<Edge<T>> links = new HashSet<>(connections.get(node));

    for (Edge<T> edge : links) {
      disconnect(edge.getDestination(), node);
    }
    connections.remove(node);
  }

  @Override
  public boolean pathExists(T from, T to) {
    if (!nodesExist(from, to))
      return false;
    return !(getPath(from, to) == null);
  }

  @Override
  public List<Edge<T>> getPath(T from, T to) {
    if (!nodesExist(from, to))
      throw new NoSuchElementException();

    Map<T, T> connection = new HashMap<>();
    connection.put(from, null);

    LinkedList<T> queue = new LinkedList<>();
    queue.add(from);

    while (!queue.isEmpty()) {
      T current = queue.pollFirst();
      for (Edge<T> e : connections.get(current)) {
        T next = e.getDestination();
        if (!connection.containsKey(next)) {
          connection.put(next, current);
          queue.add(next);
        }
      }
    }
    LinkedList<Edge<T>> path = new LinkedList<>();
    T current = to;
    if (!connection.containsKey(to))
      return null;

    while (current != null && !current.equals(from)) {
      T next = connection.get(current);
      Edge<T> edge = getEdgeBetween(next, current);
      current = next;
      path.addFirst(edge);
    }
    return path;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();

    for (Map.Entry<T, Set<Edge<T>>> conn : connections.entrySet()) {
      str.append(conn.getKey().toString() + ":\n");
      for (Edge<T> ed : conn.getValue()) {
        str.append(ed.toString() + "\n");
      }
      str.append("\n");
    }
    return str.toString();
  }

  public boolean nodeExist(T node) {
    return connections.containsKey(node);
  }

  private boolean nodesExist(T node1, T node2) {
    return connections.containsKey(node1) && connections.containsKey(node2);
  }
}
