import java.security.InvalidAlgorithmParameterException;
import java.io.*;
import java.util.*;

/**
 * Models a weighted graph of latitude-longitude points
 * and supports various distance and routing operations.
 * To do: Add your name(s) as additional authors
 * 
 * @author Brandon Fain
 * @author Owen Astrachan modified in Fall 2023
 *
 */
public class GraphProcessor {
    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * 
     * @param file a FileInputStream of the .graph file
     * @throws Exception if file not found or error reading
     */

    // include instance variables here
    HashMap<Point, HashSet<Point>> map;

    public GraphProcessor() {
        map = new HashMap<>();
    }

    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * 
     * @param file a FileInputStream of the .graph file
     * @throws IOException if file not found or error reading
     */

    public void initialize(FileInputStream file) throws IOException {
        Scanner scanner = new Scanner(file);
        String[] firstLine = scanner.nextLine().split(" ");
        int numVerts = Integer.parseInt(firstLine[0]);
        int numEdges = Integer.parseInt(firstLine[1]);
        Point[] points = new Point[numVerts];
        for (int i = 0; i < numVerts; i++) {
            String[] line = scanner.nextLine().split(" ");
            Point newPoint = new Point(Double.parseDouble(line[1]), Double.parseDouble(line[2]));
            points[i] = newPoint;
            map.put(newPoint, new HashSet<Point>());
        }
        for (int i = 0; i < numEdges; i++) {
            String[] line = scanner.nextLine().split(" ");
            Point point1 = points[Integer.parseInt(line[0])];
            Point point2 = points[Integer.parseInt(line[1])];
            map.get(point1).add(point2);
            map.get(point2).add(point1);
        }
        scanner.close();
    }

    /**
     * NOT USED IN FALL 2023, no need to implement
     * 
     * @return list of all vertices in graph
     */

    public List<Point> getVertices() {
        return null;
    }

    /**
     * NOT USED IN FALL 2023, no need to implement
     * 
     * @return all edges in graph
     */
    public List<Point[]> getEdges() {
        return null;
    }

    /**
     * Searches for the point in the graph that is closest in
     * straight-line distance to the parameter point p
     * 
     * @param p is a point, not necessarily in the graph
     * @return The closest point in the graph to p
     */
    public Point nearestPoint(Point p) {
        Point minPoint = null;
        int minDistance = Integer.MAX_VALUE;
        if (map.containsKey(p)) {
            for (Point p2 : map.keySet()) {
                if (!p.equals(p2)) {
                    if (p.distance(p2) < minDistance) {
                        minDistance = (int) p.distance(p2);
                        minPoint = p2;
                    }
                }
            }
        }
        return minPoint;
    }

    /**
     * Calculates the total distance along the route, summing
     * the distance between the first and the second Points,
     * the second and the third, ..., the second to last and
     * the last. Distance returned in miles.
     * 
     * @param start Beginning point. May or may not be in the graph.
     * @param end   Destination point May or may not be in the graph.
     * @return The distance to get from start to end
     */
    public double routeDistance(List<Point> route) {
        double total = 0.0;
        for (int i = 1; i < route.size(); i++) {
            total += route.get(i - 1).distance(route.get(i));
        }
        return total;
    }

    /**
     * Checks if input points are part of a connected component
     * in the graph, that is, can one get from one to the other
     * only traversing edges in the graph
     * 
     * @param p1 one point
     * @param p2 another point
     * @return true if and onlyu if p2 is reachable from p1 (and vice versa)
     */
    public boolean connected(Point p1, Point p2) {
        if (p1 == null || p2 == null || !map.containsKey(p1) || !map.containsKey(p2)) {
            return false;
        }
        Stack<Point> explore = new Stack<>();
        HashSet<Point> visited = new HashSet<>();
        visited.add(p1);
        explore.add(p1);
        Point currentPoint;

        while (!explore.isEmpty()) {
            currentPoint = explore.pop();
            for (Point neighbor : map.get(currentPoint)) {
                if (neighbor.equals(p2)) {
                    return true;
                }
                if (!visited.contains(neighbor)) {
                    explore.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }
        return false;
    }

    /**
     * Returns the shortest path, traversing the graph, that begins at start
     * and terminates at end, including start and end as the first and last
     * points in the returned list. If there is no such route, either because
     * start is not connected to end or because start equals end, throws an
     * exception.
     * 
     * @param start Beginning point.
     * @param end   Destination point.
     * @return The shortest path [start, ..., end].
     * @throws IllegalArgumentException if there is no such route,
     *                                  either because start is not connected to end
     *                                  or because start equals end.
     */
    public List<Point> route(Point start, Point end) throws IllegalArgumentException {
        if (!connected(start, end) || start.equals(end)) {
            throw new IllegalArgumentException("No path between start and end");
        }
        Map<Point, Double> pathDistances = new HashMap<>();
        Comparator<Point> comparator = (a, b) -> (int) (pathDistances.get(a) - pathDistances.get(b));
        PriorityQueue<Point> queue = new PriorityQueue<>(comparator);
        Map<Point, Point> thePath = new HashMap<>();

        Point currentPoint = start;
        queue.add(currentPoint);
        pathDistances.put(currentPoint, 0.0);

        while (!queue.isEmpty()) {
            currentPoint = queue.remove();
            for (Point neighbor : map.get(currentPoint)) {
                double currentDist = currentPoint.distance(neighbor);
                if (!pathDistances.containsKey(neighbor)
                        || pathDistances.get(neighbor) > (pathDistances.get(currentPoint) + currentDist)) {
                    pathDistances.put(neighbor, pathDistances.get(currentPoint) + currentDist);
                    thePath.put(currentPoint, neighbor);
                    queue.add(neighbor);
                }
            }
        }

        ArrayList<Point> finalPath = new ArrayList<>();
        Point current = start;
        while (!current.equals(end)) {
            finalPath.add(current);
            current = thePath.get(current);
        }
        finalPath.add(end);
        return finalPath;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        String name = "data/usa.graph";
        GraphProcessor gp = new GraphProcessor();
        gp.initialize(new FileInputStream(name));
        System.out.println("running GraphProcessor");
    }

}
