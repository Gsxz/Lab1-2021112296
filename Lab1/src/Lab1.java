import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Lab1 {
    static class Graph {
        Map<String, Set<String>> adjList = new HashMap<>();
        Map<String, Map<String, Integer>> weights = new HashMap<>();

        public void Add(String l, String r) {
            adjList.computeIfAbsent(l, k -> new HashSet<>()).add(r);
            weights.computeIfAbsent(l, k -> new HashMap<>()).compute(r, (k, v) -> (v == null) ? 1 : v + 1);
        }
    }

    static Graph Build(String Path) throws IOException {
        Graph graph = new Graph();
        BufferedReader r = new BufferedReader(new FileReader(Path));
        String line;
        while ((line = r.readLine()) != null) {
            String[] words = line.toLowerCase().replaceAll("[^a-z\\s]", " ").trim().split("\\s+");
            for (int i = 0; i < words.length - 1; i++) 
                graph.Add(words[i], words[i + 1]);
        }
        r.close();
        return graph;
    }

    static void Show(Graph graph) {
        for (String l : graph.adjList.keySet()) {
            System.out.print(l + " --> ");
            for (String r : graph.adjList.get(l)) 
                System.out.print(r + "(" + graph.weights.get(l).get(r) + ") ");
            System.out.println();
        }
    }

    static String QueryBridge(Graph graph, String l, String r) {
        if (!graph.adjList.containsKey(l) || !graph.adjList.containsKey(r)) 
            return "No " + l + " or " + r + " found.";
        Set<String> bridge = new HashSet<>();
        for (String target : graph.adjList.getOrDefault(l, new HashSet<>())) 
            if (graph.weights.getOrDefault(target, new HashMap<>()).containsKey(r)) 
                bridge.add(target);
        if (bridge.isEmpty()) 
            return "No bridge from " + l + " to " + r + ".";
        List<String> list = new ArrayList<>(bridge);
        if (list.size() > 1) 
            return "Bridge from " + l + " to " + r + " : " + String.join(", ", list.subList(0, list.size() - 1)) + " and " + list.getLast() + ".";
        else 
            return "Bridge from " + l + " to " + r + " : " + list.getFirst();
    }

    static String GraphGenerate(Graph graph, String input) {
        if (input == null || input.trim().isEmpty()) 
            return "Input Empty!";

        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder text = new StringBuilder();
        List<String> list = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < words.length - 1; i++) {
            text.append(words[i]).append(" ");
            String ans = QueryBridge(graph, words[i], words[i + 1]);

            if (ans.startsWith("Bridge")) {
                String[] parts = ans.substring(ans.indexOf(':') + 2).split(",| and ");
                List<String> possible = Arrays.asList(parts);
                String bridgeWord = possible.get(random.nextInt(possible.size())).trim();
                text.append(bridgeWord).append(" ");
                list.add(bridgeWord);
            }
        }
        text.append(words[words.length - 1]);
        System.out.println("Bridge words used: " + list);
        return text.toString();
    }

    static List<String> Dij(Graph graph, String l, String r) {
        Map<String, Set<String>> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        List<String> paths = new ArrayList<>();

        int lIndex = 0;
        for (String node : graph.adjList.keySet())
            if (node.startsWith(l)) {
                queue.offer(node + lIndex);
                previous.computeIfAbsent(node + lIndex, k -> new HashSet<>()).add("");
                lIndex++;
            }
        if (lIndex == 0) {
            System.out.println("No \"" + l + "\" in the graph!");
            return paths;
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            String currentWord = current.replaceAll("\\d", "");
            visited.add(current);

            if (currentWord.equals(r))
                paths.addAll(BuildPath(previous, current).stream()
                            .map(s -> s.replaceAll("\\d", ""))
                            .toList());
            else {
                Map<String, Integer> currentWeights = graph.weights.get(currentWord);
                if (currentWeights != null) {
                    for (String neighbor : graph.adjList.get(currentWord)) {
                        String neighborWithIndex = neighbor + "0";
                        if (!visited.contains(neighborWithIndex)) {
                            queue.offer(neighborWithIndex);
                            previous.computeIfAbsent(neighborWithIndex, k -> new HashSet<>()).add(current);
                            visited.add(neighborWithIndex);
                        }
                    }
                }
            }
        }
        if (paths.isEmpty())
            System.out.println("No path from " + l + " to " + r + ".");
        return paths;
    }

    static Set<String> BuildPath(Map<String, Set<String>> previous, String current) {
        Set<String> paths = new HashSet<>();
        if (previous.get(current).contains(""))
            paths.add(current);
        else
            for (String prev : previous.get(current))
                for (String path : BuildPath(previous, prev))
                    paths.add(path + " " + current);
        return paths;
    }

    static void Walk(Graph graph) {
        List<String> visitedNodes = new ArrayList<>();
        Set<String> visitedWords = new HashSet<>();
        AtomicReference<String> current = new AtomicReference<>(new ArrayList<>(graph.adjList.keySet()).get(new Random().nextInt(graph.adjList.size())));
        visitedNodes.add(current.get());
        visitedWords.add(current.get().toLowerCase());

        while (true) {
            List<String> neighbors = new ArrayList<>(graph.adjList.getOrDefault(current.get(), new HashSet<>()));

            if (neighbors.isEmpty())
                break;
            String next = neighbors.get(new Random().nextInt(neighbors.size()));
            if (!visitedWords.add(next.toLowerCase()))
                break;

            visitedNodes.add(next);
            visitedWords.add(next.toLowerCase());
            current.set(next);
        }

        String output = String.join(" ", visitedNodes);
        System.out.println("Visited nodes : " + output);
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the path to the text file: ");
        String Path = scanner.nextLine();
        Graph graph = Build(Path);

        while (true) {
            System.out.println("\nChoose a function:");
            System.out.println("1. Show directed graph");
            System.out.println("2. Query bridge words");
            System.out.println("3. GraphGenerate new text");
            System.out.println("4. Calculate shortest path");
            System.out.println("5. Random walk");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    Show(graph);
                    break;
                case 2:
                    System.out.print("Enter the first word: ");
                    String l = scanner.nextLine().toLowerCase();
                    System.out.print("Enter the second word: ");
                    String r = scanner.nextLine().toLowerCase();
                    System.out.println(QueryBridge(graph, l, r));
                    break;
                case 3:
                    System.out.print("Enter a line of text: ");
                    String input = scanner.nextLine();
                    System.out.println("GraphGenerated text: " + GraphGenerate(graph, input));
                    break;
                case 4:
                    System.out.print("Enter the first word: ");
                    l = scanner.nextLine().toLowerCase();
                    System.out.print("Enter the second word: ");
                    r = scanner.nextLine().toLowerCase();
                    List<String> shortestPaths = Dij(graph, l, r);
                    if (!shortestPaths.isEmpty()) {
                        System.out.println("Shortest paths from \"" + l + "\" to \"" + r + "\":");
                        for (String path : shortestPaths)
                            System.out.println(path);
                    }
                    break;
                case 5:
                    Walk(graph);
                    break;
                case 0:
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}