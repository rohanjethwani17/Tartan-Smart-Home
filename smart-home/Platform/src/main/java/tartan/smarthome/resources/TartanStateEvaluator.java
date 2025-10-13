
package tartan.smarthome.resources;

import java.util.Map;

public interface TartanStateEvaluator {
    public TartanState evaluateState(TartanState inState, StringBuffer log);
}