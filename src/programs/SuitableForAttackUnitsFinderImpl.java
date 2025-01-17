package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.*;

public class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByColumn, boolean humanAttack) {
        List<Unit> suitableUnits = new ArrayList<>();

        // Создаём новый список колонок, чтобы случайно не изменить оригинальный порядок
        List<List<Unit>> columns = new ArrayList<>(unitsByColumn);

        // Если это ход компьютера, то меняем крайние колонки местами
        if (!humanAttack) {
            List<Unit> tempColumn = columns.get(0);
            columns.set(0, columns.get(2));
            columns.set(2, tempColumn);
        }

        // Составим карту живых бойцов, чтобы не перебирать весь список каждый раз
        Map<Integer, Unit> map = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            for (Unit unit : columns.get(i)) {

                // Нас интересуют только живые бойцы
                if (unit.isAlive()) {

                    // Чтобы уложить две координаты в один int, придумаем простенький хеш, чтобы значения не пересекались. Для нашего диапазона вполне подойдёт
                    int hash = i * 2 + unit.getyCoordinate() * 100;
                    map.put(hash, unit);
                }
            }
        }

        // Перебирать будем поколоночно, от тыльной колонки к фронтовой
        for (int i = 0; i < 3; i++) {
            for (Unit unit : columns.get(i)) {

                // Мёртвые не в счёт
                if (!unit.isAlive()) {
                    continue;
                }

                // Получаем бойца и проверяем, нет ли кого спереди. Проверяем только по координате x, то есть максимум две проверки на бойца
                boolean covered = false;
                for (int j = i + 1; j < 3; j++) {
                    int hash = j * 2 + unit.getyCoordinate() * 100;
                    if (map.containsKey(hash)) {
                        covered = true;
                        break;
                    }
                }

                // И если не прикрывает, то он потенциальная цель
                if (!covered) {
                    suitableUnits.add(unit);
                }
            }
        }

        return suitableUnits;
    }
}
