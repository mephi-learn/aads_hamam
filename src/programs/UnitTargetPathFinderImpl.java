package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import com.battle.heroes.army.programs.UnitTargetPathFinder;

import java.util.*;

public class UnitTargetPathFinderImpl implements UnitTargetPathFinder {
    private static final int WIDTH = 27; // Ширина игрового поля
    private static final int HEIGHT = 21; // Высота игрового поля

    private static String hash(int x, int y) {
        return String.valueOf(x) + "|" + String.valueOf(y);
    }

    @Override
    // Метод получения кратчайшего пути.
    // Сложность алгоритма O(n log n), где n = WIDTH * HEIGHT (createVertexMap = O(1), compute = O(n log n), getEdges = O(n))
    public List<Edge> getTargetPath(Unit sourceUnit, Unit targetUnit, List<Unit> allUnits) {
        // Создаём граф возможных перемещений с учётом расположения бойцов
        Map<String, Vertex> vertexMap = createVertexMap(allUnits, targetUnit);

        // Инициируем класс поиска пути по алгоритму Дейкстры
        Dijkstra dijkstra = new Dijkstra();

        // Получаем ячейку атакующего
        Vertex sourceVertex = vertexMap.get(hash(sourceUnit.getxCoordinate(), sourceUnit.getyCoordinate()));

        // Получаем ячейку атакуемого
        Vertex targetVertex = vertexMap.get(hash(targetUnit.getxCoordinate(), targetUnit.getyCoordinate()));

        // Ищем кратчайший путь
        dijkstra.compute(sourceVertex);

        // Формируем шаги полученного пути и возвращаем их
        return dijkstra.getEdges(targetVertex);
    }

    // Метод генерации карты возможных перемещений. Из карты пути исключаются ячейки с живыми бойцами (кроме цели - она остаётся достижимой)
    // Ну а чтобы бойцы не шатались по карте словно пьяные, шатаясь из стороны в сторону (как, собственно, они и шатаются в исходном примере),
    // реализуем систему весов, реализующее правило, что гипотенуза короче двух катетов. Ходить через своих бойцов, кстати, тоже запрещено
    private Map<String, Vertex> createVertexMap(List<Unit> allUnits, Unit target) {

        // Сначала сформируем
        Map<String, Unit> unitMap = new HashMap<>();
        for (Unit unit : allUnits) {
            unitMap.put(hash(unit.getxCoordinate(), unit.getyCoordinate()), unit);
        }
        Map<String, Vertex> vertexMap = new HashMap<>();
        String targetHash = hash(target.getxCoordinate(), target.getyCoordinate());
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Vertex current = vertexMap.getOrDefault(hash(x, y), new Vertex(x, y));
                vertexMap.put(hash(x, y), current);

                // Если в текущей ячейке есть боец, он жив, но не является целью, то у данной ячейки не будет соседей
                if (unitMap.get(hash(x, y - 1)) != null && !hash(x, y).equals(targetHash) && unitMap.get(hash(x, y - 1)).isAlive()) {
                    continue;
                }

                // Здесь и далее, если соседняя ячейка находится по диагонали, то сложность пути до неё выставляем в 7, а если по прямой, то сложность выставляем в 5

                // Если вверху есть ячейка, не содержащая живого бойца, кроме цели, добавляем её как соседнюю
                if (y > 0 && (hash(x, y - 1).equals(targetHash) || unitMap.get(hash(x, y - 1)) == null || !unitMap.get(hash(x, y - 1)).isAlive())) {
                    Vertex up = vertexMap.getOrDefault(hash(x, y - 1), new Vertex(x, y - 1));
                    current.addNeighbor(new VertexEdge(5, current, up));
                    vertexMap.put(hash(x, y - 1), up);
                }

                // Если слева есть ячейка, не содержащая живого бойца, кроме цели, добавляем её как соседнюю
                if (x > 0 && (hash(x - 1, y).equals(targetHash) || unitMap.get(hash(x - 1, y)) == null || !unitMap.get(hash(x - 1, y)).isAlive())) {
                    Vertex left = vertexMap.getOrDefault(hash(x - 1, y), new Vertex(x - 1, y));
                    current.addNeighbor(new VertexEdge(5, current, left));
                    vertexMap.put(hash(x - 1, y), left);
                }

                // Если слева сверху есть ячейка, не содержащая живого бойца, кроме цели, добавляем её как соседнюю
                if (x > 0 && y > 0 && (hash(x - 1, y - 1).equals(targetHash) || unitMap.get(hash(x - 1, y - 1)) == null || !unitMap.get(hash(x - 1, y - 1)).isAlive())) {
                    Vertex upLeft = vertexMap.getOrDefault(hash(x - 1, y - 1), new Vertex(x - 1, y - 1));
                    current.addNeighbor(new VertexEdge(7, current, upLeft));
                    vertexMap.put(hash(x - 1, y - 1), upLeft);
                }

                // Если справа сверху есть ячейка, не содержащая живого бойца, кроме цели, добавляем её как соседнюю
                if (x != WIDTH - 1 && y > 0 && (hash(x + 1, y - 1).equals(targetHash) || unitMap.get(hash(x + 1, y - 1)) == null || !unitMap.get(hash(x + 1, y - 1)).isAlive())) {
                    Vertex upRight = vertexMap.getOrDefault(hash(x + 1, y - 1), new Vertex(x + 1, y - 1));
                    current.addNeighbor(new VertexEdge(7, current, upRight));
                    vertexMap.put(hash(x + 1, y - 1), upRight);
                }
            }
        }

        // Возвращаем карту возможных перемещений
        return vertexMap;

    }

    // Класс расчёта пути по алгоритму Дейкстры
    class Dijkstra {

        // Очередь вершин
        private final PriorityQueue<Vertex> queue;

        public Dijkstra() {
            this.queue = new PriorityQueue<>();
        }

        // Метод поиска пути
        public void compute(Vertex source) {
            // До текущей ячейки ходить не нужно, добавляем её в очередь
            source.setDistance(0);
            queue.add(source);

            // И, пока очередь не пуста
            while (!queue.isEmpty()) {

                // Получаем текущую ячейку пути и перебираем всех её соседей
                Vertex curr = queue.poll();
                for (VertexEdge e : curr.getNeighbors()) {

                    // Получаем ячейку текущего соседа и сложность пути
                    Vertex end = e.getEnd();
                    double w = e.getWeight();

                    // Если мы ещё не посещали его
                    if (!end.isVisited()) {

                        // Проверяем, если данный путь короче существующего (а он, по умолчанию равен почти бесконечности)
                        if (curr.getDistance() + w < end.getDistance()) {

                            // Будем считать текущий путь эталонным и сохраним его
                            end.setDistance(curr.getDistance() + w);

                            // Перемещаем ячейку в конец
                            queue.remove(end);
                            queue.add(end);

                            // И сохраняем в ней текущую ячейку, чтобы потом можно было восстановить путь, откуда пришли
                            end.setPrevious(curr);
                        }
                    }
                }

                // Отмечаем, что данная ячейка уже посещалась
                curr.setVisited(true);
            }
        }

        // Метод получения путь от начальной ячейки к конечной в формате пригодном для программы
        public List<Edge> getEdges(Vertex end) {
            List<Edge> path = new ArrayList<>();

            // Пока у ячейки есть соседняя, откуда мы в неё пришли
            while (end.getPrevious() != null) {

                // Добавляем эту ячейку в путь и делаем её текущей
                path.add(end.getEdge());
                end = end.getPrevious();
            }

            // Не забываем добавить исходную ячейку
            path.add(end.getEdge());

            // Поскольку мы не раки, чтобы пятиться задом наперёд, разворачиваем путь. Как это... Пусть лучше путь прогнётся под нас :)
            Collections.reverse(path);

            return path;
        }
    }

    // Класс вершин
    class Vertex implements Comparable<Vertex> {
        int coordX;
        int coordY;
        boolean visited;
        List<VertexEdge> neighbors;
        double distance;
        Vertex previous;

        public Vertex(int coordX, int coordY) {
            this.coordX = coordX;
            this.coordY = coordY;
            this.neighbors = new ArrayList<>();
            this.previous = null;

            // Изначально расстояние до всех точек бесконечно большое
            this.distance = Double.MAX_VALUE;
        }

        // Метод добавления соседа, работает в обе стороны
        public void addNeighbor(VertexEdge neighborEdge) {
            this.neighbors.add(neighborEdge);
            Vertex neighbor = neighborEdge.getEnd();
            neighbor.neighbors.add(new VertexEdge(5, neighbor, this));
        }

        // Далее всякие геттеры и сеттеры

        public Edge getEdge() {
            return new Edge(this.coordX, this.coordY);
        }

        public boolean isVisited() {
            return this.visited;
        }

        public void setVisited(boolean visited) {
            this.visited = visited;
        }

        public List<VertexEdge> getNeighbors() {
            return this.neighbors;
        }

        public double getDistance() {
            return this.distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public Vertex getPrevious() {
            return previous;
        }

        public void setPrevious(Vertex previous) {
            this.previous = previous;
        }

        @Override
        public int compareTo(Vertex o) {
            return Double.compare(this.getDistance(), o.getDistance());
        }
    }

    // Поскольку авторы задания зажали описание Edge, реализуем свой, с нужным функционалом
    class VertexEdge {
        // Координаты вершин источника и цели
        Vertex start;
        Vertex end;

        // Сложность пути
        double weight;


        public VertexEdge(double weight, Vertex start, Vertex end) {
            this.start = start;
            this.end = end;
            this.weight = weight;
        }

        // Далее всякие геттеры и сеттеры

        public Vertex getEnd() {
            return end;
        }

        public double getWeight() {
            return weight;
        }
    }
}
