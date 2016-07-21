package ca.uqac.lif.cep.sets;

import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.SingleProcessor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;

public class ToStringHashSet extends SingleProcessor {
    public ToStringHashSet() {
        super(1, 1);
    }

    @Override
    protected Queue<Object[]> compute(Object[] inputs) {
        if (inputs[0] instanceof Collection) {
            Collection col = (Collection)inputs[0];
            HashSet<String> set = new HashSet<>();

            for (Object o : col) {
                set.add(o.toString());
            }

            return wrapObject(set);
        }
        return null;
    }

    @Override
    public Processor clone() {
        return null;
    }
}
