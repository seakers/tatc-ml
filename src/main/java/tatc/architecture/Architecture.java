package tatc.architecture;

import tatc.architecture.variable.MonolithVariable;

import java.util.HashSet;

public interface Architecture {
    HashSet<MonolithVariable> getSatellites();
}
