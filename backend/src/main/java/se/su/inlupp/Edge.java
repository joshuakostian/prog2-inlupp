package se.su.inlupp;

public class Edge<T> {

  private T source;
  private T destination;
  private String name;
  private int weight;

  public Edge(T source, T destination, String name, int weight)  {
    this.name = name;
    this.source = source;
    this.destination = destination;
    this.weight = weight;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    if (weight < 0)
      throw new IllegalArgumentException();
    this.weight = weight;
  }

  public T getDestination() {
    return destination;
  }

  String getName() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (obj instanceof Edge e)
      return this.destination.equals(e.getDestination());
    return false;
  }

  @Override
  public int hashCode() {
    return this.destination.hashCode();
  }

  @Override
  public String toString() {
    return "till " + destination + " med " + source + " -> " + destination + " tar " + weight;
  }
}
