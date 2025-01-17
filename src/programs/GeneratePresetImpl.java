package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


import java.util.ArrayList;
import java.util.List;

public class GeneratePresetImpl implements GeneratePreset {
    final int MAX_UNITS_IN_SQUAD = 11;

    // Класс обёртка штатной единицы, будет участвовать во всех расчётах, во время расстановки превратится в Unit
    static class Rookie {
        private final Unit unit;
        private final int experience;

        public Rookie(Unit unit) {
            this.unit = unit;
            this.experience = (unit.getBaseAttack() + unit.getHealth()) / 2;
        }

        protected String getUnitType() {
            return unit.getUnitType();
        }

        protected int getExperience() {
            return experience;
        }

        protected int getCost() {
            return unit.getCost();
        }

        protected int getHealth() {
            return unit.getHealth();
        }

        protected int getAttack() {
            return unit.getBaseAttack();
        }

        protected Unit toUnit(Integer id, int xCoord, int yCoord) {
            // Присваиваем бойцу позывной и дислоцируем его по указанным координатам
            return new Unit(
                    unit.getName() + " " + id,
                    unit.getUnitType(),
                    unit.getHealth(),
                    unit.getBaseAttack(),
                    unit.getCost(),
                    unit.getAttackType(),
                    unit.getAttackBonuses(),
                    unit.getDefenceBonuses(),
                    xCoord,
                    yCoord
            );
        }
    }

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {

        // Формируем штурмовое подразделение
        List<Rookie> assaultGroup = generateSimpleMethod(unitList, maxPoints);

        System.out.printf("На %d денег сформировано следующее подразделение:%n", maxPoints);
        AtomicInteger budget = new AtomicInteger(maxPoints);
        assaultGroup.forEach(unit -> {
                    System.out.println(unit.getUnitType() + " (Атака: " + unit.getAttack() +
                            ", Здоровье: " + unit.getHealth() +
                            ", Стоимость: " + unit.getCost() + ", Опыт: " + unit.getExperience() + ")");
                    budget.addAndGet(-unit.getCost());
                }
        );
        System.out.println("Оставшийся бюджет: " + budget);

        return buildArmy(assaultGroup);
    }

    // Реализация алгоритма "хапуга". Не очень эффективный, но быстрый. Хотя в данном конкретном случае в аккурат подходит. Принцип - хватаем как можно больше самого дорогого
    // Алгоритмическая сложность O(n log n): сортировка O(n log n), формирование O(n), память O(n), итого: O(n log n)
    //
    public List<Rookie> generateSimpleMethod(List<Unit> unitList, int maxPoints) {

        // Отсортируем штатные единицы по эффективности. Лучше отсортировать типы, чем полный набор
        unitList.sort(Comparator.comparing(unit -> {
            return (unit.getBaseAttack() + unit.getHealth()) / 2;
        }, Comparator.reverseOrder()));

        int cost = maxPoints;
        List<Rookie> assaultGroup = new ArrayList<>();

        // А теперь каждой твари по... по нужному количеству единиц, как говорится, на все деньги
        for (Unit unit : unitList) {
            for (int i = 0; i < MAX_UNITS_IN_SQUAD; i++, cost -= unit.getCost()) {
                if (cost < unit.getCost()) {
                    break;
                }
                // Создаём штатную единицу
                assaultGroup.add(new Rookie(unit));
            }
        }

        return assaultGroup;
    }

    // Формируем армию, расставляя её по позициям
    private Army buildArmy(List<Rookie> assaultGroup) {
        List<Unit> armyUnits = new ArrayList<>();
        Map<String, Boolean> hashPositions = new HashMap<>();
        for (Rookie rookie : assaultGroup) {
            int maxAttempt = 1000;
            while (--maxAttempt >= 0) {
                int xCoord = (int) (Math.random() * 3);
                int yCoord = (int) (Math.random() * 21);
                String hash = String.valueOf(xCoord) + yCoord;
                if (hashPositions.containsKey(hash)) {
                    continue;
                }
                armyUnits.add(rookie.toUnit(Integer.valueOf(hash), xCoord, yCoord));
                hashPositions.put(hash, true);
                break;
            }
            ;

            // Отряд не заметил потери бойца
            if (maxAttempt < 0) {
                System.out.println("Не удалось найти доступные координаты для бойца подразделения " + rookie.getUnitType());
            }
        }

        Army army = new Army();
        army.setUnits(armyUnits);
        return army;
    }
}