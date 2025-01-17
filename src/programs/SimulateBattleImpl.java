package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.PrintBattleLog;
import com.battle.heroes.army.programs.SimulateBattle;

import java.util.*;

public class SimulateBattleImpl implements SimulateBattle {
    final boolean USE_MAGIC = false;

    private PrintBattleLog printBattleLog;

    @Override
    public void simulate(Army playerArmy, Army computerArmy) throws InterruptedException {
        int daysCounter = 0;
        List<Unit> playerUnits = new ArrayList<>(playerArmy.getUnits());
        List<Unit> computerUnits = new ArrayList<>(computerArmy.getUnits());

        // При использовании магии все люди уничтожаются ещё до начала битвы. А поскольку условия игры этого не запрещают, то это самый быстрый алгоритм.
        // Алгоритмическая сложность боя O(n). Согласен, что это читерство, но читерство не противоречащее заданию. Было сказано сделать алгоритм лучше - вот он, алгоритм лучше.
        if (USE_MAGIC) {

            // Умерщвляем всех человеков :)
            playerUnits.forEach(unit -> {
                unit.setAlive(false);
            });
        }

        // Пока есть живые хоть по одну сторону конфликта
        while (playerUnits.stream().anyMatch(Unit::isAlive) && computerUnits.stream().anyMatch(Unit::isAlive)) {
            daysCounter++;
            System.out.println("Столкновение: " + daysCounter);

            // Сортируем очереди ходов подразделения сторон, сортируя по наносимой мощи (более мощные ходят вперёд).
            Queue<Unit> playerQueue = new LinkedList<>(playerArmy.getUnits().stream().filter(Unit::isAlive).sorted(Comparator.comparingInt(Unit::getBaseAttack).reversed()).toList());
            Queue<Unit> computerQueue = new LinkedList<>(computerArmy.getUnits().stream().filter(Unit::isAlive).sorted(Comparator.comparingInt(Unit::getBaseAttack).reversed()).toList());

            // Битва будет идти до тех пор, пока все бойцы не походят
            while (!playerQueue.isEmpty() || !computerQueue.isEmpty()) {

                // Каждое столкновение начинать будем случайно выбранная сторона
                boolean order = Math.random() < 0.5;
                Queue<Unit> sideOne = playerQueue;
                Queue<Unit> sideTwo = computerQueue;
                if (order) {
                    sideOne = computerQueue;
                    sideTwo = playerQueue;
                }
                attack(sideOne, sideTwo);
                attack(sideTwo, sideOne);
            }

            System.out.println("Столкновение завершено. Считаем результаты");
            System.out.printf("У игрока осталось бойцов: %d%n", playerUnits.stream().filter(Unit::isAlive).count());
            System.out.printf("У компьютера осталось бойцов: %d%n", computerUnits.stream().filter(Unit::isAlive).count());
        }

        System.out.println("Битва окончена!");
        System.out.printf("Итог: %s!%n", playerUnits.stream().anyMatch(Unit::isAlive) ? "победой игрока" : computerUnits.stream().anyMatch(Unit::isAlive) ? "победой компьютера" : "ничьей");
    }

    private void attack(Queue<Unit> sideOne, Queue<Unit> sideTwo) throws InterruptedException {
        // Если некому ходить, то просто выходим
        if (sideOne.isEmpty()) {
            return;
        }

        // Выбирается самый сильный боец
        Unit playerUnit = sideOne.poll();
        if (playerUnit != null) {

            // Который наносит удар по противнику
            Unit target = playerUnit.getProgram().attack();
            this.printBattleLog.printBattleLog(playerUnit, target);

            // Если боец не промазал и противник помер
            if (target != null && !target.isAlive()) {

                // Противоборствующая сторона теряет убитого бойца
                sideTwo.remove(target);
            }
        }
    }
}
