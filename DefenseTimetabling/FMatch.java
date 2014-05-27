import localsearch.functions.*;
import localsearch.model.*;

import java.util.HashMap;

public class FMatch extends AbstractInvariant
    implements IFunction
{
    private int _value;
    private int _minValue;
    private int _maxValue;

    private LocalSearchManager _ls;

    private int _student;
    private int _numProfs;
    private VarIntLS[] _x;
    private VarIntLS _prof;

    private HashMap<Integer, Integer> _subjectMatch;

    public FMatch(int student, VarIntLS prof, HashMap<Integer, Integer> subjectMatch, int numProfs)
    {
        _student = student;
        _prof = prof;
        _numProfs = numProfs;
        _x = new VarIntLS[]{prof};
        _subjectMatch = subjectMatch;
        this._ls = prof.getLocalSearchManager();
        post();
    }

    private int getKey(int student, int prof) {
        return student * _numProfs + prof;
    }

    private void post()
    {
        _value = _subjectMatch.get(getKey(_student, _prof.getValue()));
        _minValue = 0;
        _maxValue = 10; //maxValue();
        _ls.post(this);
    }

    public int getMinValue() { return _minValue; }
    public int getMaxValue() { return _maxValue; }
    public int getValue() { return _value; }

    public VarIntLS[] getVariables() { return _x; }

    private int _getAssignDelta(VarIntLS x, int val) {
        return (_subjectMatch.get(getKey(_student, val)) - _value);
    }

    public int getAssignDelta(VarIntLS x, int val) {
        return !(x == _prof) ? 0 : _getAssignDelta(x, val);
    }

    public int getSwapDelta(VarIntLS x, VarIntLS y) { 
        if (x != y) {
            if (x == _prof)
                return _getAssignDelta(y, y.getValue());
            else if (y == _prof)
                return _getAssignDelta(x, x.getValue());
        }
        return 0; 
    }

    public void propagateInt(VarIntLS x, int val) {
        if (_prof == x)
            _value = _subjectMatch.get(getKey(_student, val));
    }

    public void initPropagate() {
        _value = _subjectMatch.get(getKey(_student, _prof.getValue()));
    }

    public LocalSearchManager getLocalSearchManager() { return _ls; }

    public String name() { return "FMatch"; }

    public boolean verify() { return false; }
}
