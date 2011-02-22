package es.robjam

/**
 * Created by IntelliJ IDEA.
 * User: rob
 * Date: 13/01/11
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */
class Feature {
  String name
  String description
  boolean active
  def roles = []

  public String toString() {
    return "name: ${name}, description: ${description}, active:${active}, roles:${roles}"
  }
}
