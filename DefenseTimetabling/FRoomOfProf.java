import java.util.HashSet;
import java.util.HashMap;
import localsearch.model.*;

public class FRoomOfProf extends AbstractInvariant
    implements IFunction
{
    private int _value;
    private int _minValue;
    private int _maxValue;

    private LocalSearchManager _ls;

    private VarIntLS[] _x;
    private int _prof;
    private int _slot;
    private int _curStudent;
    private int _numStudents;

    private VarIntLS _varRoom;
    private VarIntLS _varSlot;

    private VarIntLS[][] _solutionPro;
    private VarIntLS[] _solutionRoom;
    private VarIntLS[] _solutionSlot;

    private HashMap<VarIntLS, Integer> _indexOfSlotVar;
    private HashMap<VarIntLS, Integer> _indexOfProfVar;

    public FRoomOfProf(int prof, int slot, VarIntLS[][] solutionPro, VarIntLS[] solutionSlot, VarIntLS[] solutionRoom, HashMap indexOfProfVar, HashMap indexOfSlotVar)
    {
        _prof = prof;
        _slot = slot;
        
        _numStudents = solutionSlot.length;
        _solutionPro = solutionPro;
        _solutionSlot = solutionSlot;
        _solutionRoom = solutionRoom;
        
        _indexOfProfVar = indexOfProfVar;
        _indexOfSlotVar = indexOfSlotVar;

        //_x = new VarIntLS[];

        this._ls = solutionRoom[0].getLocalSearchManager();
        post();
    }

    private void post()
    {
        //throw new UnsupportedOperationException("FRoomOfProf.getSwapDelta()");
        _value = 0;
        _curStudent = -1;
        for (int s = 0; s < _numStudents; ++s) {
            if (_solutionSlot[s].getValue() == _slot) {
                for (int i = 0; i < 5; ++i)
                    if (_solutionPro[s][i].getValue() == _prof) {
                        _curStudent = s;
                        _varRoom = _solutionRoom[s];
                        //_varSlot = _solutionSlot[s];

                        _value = _solutionRoom[s].getValue();
                        // TODO: break search_done
                        break;
                    }
            }
        }
//search_done:

        _minValue = 0;
        _maxValue = _solutionRoom[0].getMaxValue();
        _ls.post(this);
    }

    public int getMinValue() { return _minValue; }
    public int getMaxValue() { return _maxValue; }
    public int getValue() { return _value; }

    public VarIntLS[] getVariables() { 
        throw new UnsupportedOperationException("FRoomOfProf.getVariables()");
        //return _x; 
    }

    private int _getAssignDelta(VarIntLS x, int val) {
        return val == _value ? 0 : 1;
    }

    public int getAssignDelta(VarIntLS x, int val) {
        if (x == _varRoom)
            return _getAssignDelta(x, val);

        if (val == _prof && _indexOfProfVar.containsKey(x)) {
            int student = _indexOfProfVar.get(x);
            if (student == _curStudent)
                return 0;
            if (_solutionSlot[student].getValue() == _slot)
                return _getAssignDelta(x, _solutionRoom[student].getValue());
        }

        if (val == _slot && _indexOfSlotVar.containsKey(x)) {
            int student = _indexOfSlotVar.get(x);
            if (student == _curStudent)
                return 0;
            for (int i = 0; i < 5; ++i)
                if (_solutionPro[student][i].getValue() == _prof)
                    return _getAssignDelta(x, _solutionRoom[student].getValue());
        }
        
        return 0;
        //return !(x == _prof) ? 0 : (_subjectMatch.get(getKey(_curStudent, val)) - _value);
    }

    public int getSwapDelta(VarIntLS x, VarIntLS y) { 
        /*
        if (x != y) {
            if (x == _varRoom)
                return _getAssignDelta(y, y.getValue());
            else if (y == _varRoom)
                return _getAssignDelta(x, x.getValue());
        }
        */
        throw new UnsupportedOperationException("FRoomOfProf.getSwapDelta()");
        //return 0;
    }

    public void propagateInt(VarIntLS x, int val) {
        //throw new UnsupportedOperationException("FRoomOfProf.getSwapDelta()");
        if (_varRoom == x)
            _value = val;
        else if (val == _prof && _indexOfProfVar.containsKey(x)) {
            int student = _indexOfProfVar.get(x);
            if (student == _curStudent)
                return;
            if (_solutionSlot[student].getValue() == _slot) {
                _curStudent = student;
                _varRoom = _solutionRoom[_curStudent];
                _value = _varRoom.getValue();
            }
        } else if (val == _slot && _indexOfSlotVar.containsKey(x)) {
            int student = _indexOfSlotVar.get(x);
            if (student == _curStudent)
                return;
            for (int i = 0; i < 5; ++i) {
                if (_solutionPro[student][i].getValue() == _prof) {
                    _curStudent = student;
                    _varRoom = _solutionRoom[student];
                    _value = _varRoom.getValue();
                }
            }
        }
    }

    public void initPropagate() {
        _value = _varRoom.getValue();
    }

    public LocalSearchManager getLocalSearchManager() { return _ls; }

    public String name() { return "FRoomOfProf"; }

    public boolean verify() { return false; }
}
