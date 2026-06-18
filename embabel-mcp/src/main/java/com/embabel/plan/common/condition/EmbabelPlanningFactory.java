package com.embabel.plan.common.condition;

import java.util.Map;
import java.util.HashMap;
import kotlin.jvm.functions.Function1;

public class EmbabelPlanningFactory {

    private static Map<String, ConditionDetermination> convertMap(Map<String, Boolean> source) {
        Map<String, ConditionDetermination> target = new HashMap<>();
        if (source != null) {
            for (Map.Entry<String, Boolean> entry : source.entrySet()) {
                target.put(entry.getKey(), entry.getValue() ? ConditionDetermination.TRUE : ConditionDetermination.FALSE);
            }
        }
        return target;
    }

    public static SimpleConditionAction createAction(String name, Map<String, Boolean> preconditions, Map<String, Boolean> effects) {
        Function1<Object, Double> cost = ws -> 1.0;
        Function1<Object, Double> value = ws -> 0.0;
        return new SimpleConditionAction(name, convertMap(preconditions), convertMap(effects), cost, value);
    }

    public static SimpleConditionGoal createGoal(String name, Map<String, Boolean> preconditions) {
        Function1<Object, Double> value = ws -> 0.0;
        return new SimpleConditionGoal(name, convertMap(preconditions), value);
    }

    public static ConditionWorldState createWorldState(Map<String, Boolean> state) {
        return new ConditionWorldStateImpl(convertMap(state));
    }

    public static FromMapWorldStateDeterminer createDeterminer(Map<String, Boolean> state) {
        return new FromMapWorldStateDeterminer(convertMap(state));
    }
}
