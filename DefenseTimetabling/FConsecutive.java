import java.util.HashSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Arrays;
import localsearch.model.*;

public class FConsecutive extends AbstractInvariant
    implements IFunction
{
    private int _value;
    private int _minValue;
    private int _maxValue;

    private LocalSearchManager _ls;

    private VarIntLS[] _x;
    private int _prof;

    private int[] _occ;
    private int _minSlot;
    private int _maxSlot;
    private int _numStudents;

    private VarIntLS[][] _solutionPro;
    private VarIntLS[] _solutionSlot;

    private HashSet<VarIntLS> _varsSlot;;

    private HashMap<VarIntLS, Integer> _indexOfProfVar;

    public FConsecutive(int prof, VarIntLS[][] solutionPro, VarIntLS[] solutionSlot, HashMap indexOfProfVar, VarIntLS[] variables)
    {
        _prof = prof;
        
        _solutionPro = solutionPro;
        _solutionSlot = solutionSlot;
        _numStudents = solutionSlot.length;

        _indexOfProfVar = indexOfProfVar;
        _x = variables;

        //_x = new VarIntLS[];

        this._ls = solutionSlot[0].getLocalSearchManager();
        post();
    }

    private void post()
    {
        //throw new UnsupportedOperationException("FConsecutive.getSwapDelta()");
        _minValue = 0;
        _maxValue = _solutionSlot[0].getMaxValue() / 2 + 1;

        _minSlot = _solutionSlot[0].getMaxValue() + 1; 
        _maxSlot = _solutionSlot[0].getMinValue() - 1;
        _occ = new int[_minSlot - _maxSlot - 2 + 1];

        _varsSlot = new HashSet<VarIntLS>();
        for (int s = 0; s < _numStudents; ++s) {
            for (VarIntLS p : _solutionPro[s])
                if (p.getValue() == _prof) {
                    if (!_varsSlot.contains(_solutionSlot[s])) {
                        _varsSlot.add(_solutionSlot[s]);
                        int slot = _solutionSlot[s].getValue();
                        ++_occ[slot-1];
                        if (slot < _minSlot) _minSlot = slot;
                        if (slot > _maxSlot) _maxSlot = slot;
                    }
                }
        }
        if (_maxSlot < _minSlot) _minSlot = _maxSlot = -1;

        _value = -1;
        /*
        int sl = _minSlot + 1;
        while (sl < _maxSlot) {
            if (_occ[sl-1] == 0) ++_value;
            do { ++sl; } while (_occ[sl-1] == 0);
        }
        */

        _ls.post(this);
    }

    public int getMinValue() { return _minValue; }
    public int getMaxValue() { return _maxValue; }
    public int getValue() { return _value; }

    public VarIntLS[] getVariables() { 
        return _x;
        //throw new UnsupportedOperationException("FConsecutive.getVariables()");
    }

    private int _getAddDelta(int val) {
        //System.out.printf("_getAddDelta(%d)\n", val);
        //System.out.printf("min = %d, max = %d, occ = %s\n", _minSlot, _maxSlot, Arrays.toString(_occ));

        if (_varsSlot.size() == 0) return 0; 
        if (_occ[val - 1] > 0) return 0;
        if (val < _minSlot) return (val == _minSlot - 1) ? 0 : 1;
        if (val > _maxSlot) return (val == _maxSlot + 1) ? 0 : 1;

        //try {
        if (_occ[val - 1 - 1] > 0 && _occ[val + 1 - 1] > 0) return -1;
        if (_occ[val - 1 - 1] == 0 && _occ[val + 1 - 1] == 0) return 1;
        //} catch (Exception e) {
        //  System.out.printf("_getAddDelta(%d)\n", val);
        //  debug();
        //  throw e;
        //
        return 0;
    }

    private int _getRemoveDelta(int val) {
        //if (_occ[val - _minValue] > 0) return 0;
        if (_occ[val - 1] > 1 || _occ[val - 1] == 0) return 0;

        if (_maxSlot == _minSlot) return 0;
        if (val == _minSlot) return (_occ[_minSlot + 1 - 1] == 0) ? -1 : 0;
        if (val == _maxSlot) return (_occ[_maxSlot - 1 - 1] == 0) ? -1 : 0;

        if (_occ[val - 1 - 1] > 0 && _occ[val + 1 - 1] > 0) return 1;
        if (_occ[val - 1 - 1] == 0 && _occ[val + 1 - 1] == 0) return -1;
        return 0;
    }

    private int _getAssignDelta(int oldVal, int newVal) {
        // TODO
        int oldMin = _minSlot, oldMax = _maxSlot;
        int removeDelta = _getRemoveDelta(oldVal);
        --_occ[oldVal - 1];
        _updateMinMaxAfterRemove(oldVal);

        int addDelta = _getAddDelta(newVal);
        ++_occ[oldVal - 1];

        _minSlot = oldMin; _maxSlot = oldMax;

        return removeDelta + addDelta;
    }

    public int getAssignDelta(VarIntLS x, int val) {
        if (x.getValue() == val) return 0;

        if (_varsSlot.contains(x)) {
            return _getAssignDelta(x.getOldValue(), val);
        }

        if (_indexOfProfVar.containsKey(x)) {
            int student = _indexOfProfVar.get(x);
            VarIntLS varSlot = _solutionSlot[student];
            if (x.getValue() == _prof) {
                if (_occ[varSlot.getValue() - 1] > 0 && _varsSlot.contains(varSlot))
                    return _getRemoveDelta(varSlot.getValue());
                return 0;
            }
            
            if (val == _prof) {
                if (!_varsSlot.contains(varSlot))
                    return _getAddDelta(varSlot.getValue());
                return 0;
            }
        }

        return 0;
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
        throw new UnsupportedOperationException("FConsecutive.getSwapDelta()");
        //return 0;
    }

    private void _updateMinMaxAfterRemove(int slot) {
        //_updateMinMax(); // TODO
        if (_minSlot == _maxSlot && slot == _minSlot && _occ[slot-1] == 0) {
            _minSlot = _maxSlot = -1;
            return;
        }

        //System.out.printf("_updateMinMaxAfterRemove(%d)\n", slot);
        //debug();
        
        if (slot == _minSlot && _occ[slot - 1] == 0) {
            int v;
            boolean found = false;
            for (v = _minSlot + 1; v <= _maxSlot && !found; ++v)
                if (_occ[v - 1] > 0) {
                    found = true;
                    //break;
                }
            if (found) {
                _minSlot = v - 1;
                //System.out.println("Min slot <- " + _minSlot);
            } else _minSlot = -1;
        }
        //System.out.println("Min slot " + _minSlot);

        if (slot == _maxSlot && _occ[slot - 1] == 0) {
            int v;
            boolean found = false;
            for (v = _maxSlot - 1; v >= _minSlot && !found; --v)
                if (_occ[v - 1] > 0) {
                    found = true;
                    //break;
                }
            if (found) {
                _maxSlot = v + 1;
                //System.out.println("Max slot <- " + _maxSlot);
            } else _maxSlot = -1;
        }
        //System.out.println("Max slot " + _maxSlot);
        //debug();
        //System.out.printf("_updateMinMaxAfterRemove(%d) finished!\n\n", slot);
    }

    private void _removeSlot(int slot) {
        _value += _getRemoveDelta(slot);                
        --_occ[slot - 1];
        _updateMinMaxAfterRemove(slot);
    }

    private void _addSlot(int slot) {
        if (_varsSlot.size() == 0) {
            _value = 0;
            _minSlot = _maxSlot = slot;
            ++_occ[slot - 1];
            return;
        }
        _value += _getAddDelta(slot);
        ++_occ[slot - 1];
        if (slot > _maxSlot) _maxSlot = slot;
        if (slot < _minSlot) _minSlot = slot;
    }

    public void propagateInt(VarIntLS x, int val) {
        //throw new UnsupportedOperationException("FConsecutive.getSwapDelta()");
        if (x.getOldValue() == val) return;

        int oldValue = _value;

        if (_indexOfProfVar.containsKey(x)) {
            int student = _indexOfProfVar.get(x);
            VarIntLS varSlot = _solutionSlot[student];
            int slot = varSlot.getValue();

            if (val == _prof) {
                if (_varsSlot.contains(varSlot)) return;

                //System.out.println("add new slot " + slot);
                //debug();
                _addSlot(slot);
                _varsSlot.add(varSlot);
                //debug();
                //System.out.println();
            }

            if (x.getOldValue() == _prof) {
                if (_occ[slot - 1] > 0 && _varsSlot.contains(varSlot)) {
                    //System.out.printf("remove slot %d, hash %d\n", slot, varSlot.hashCode());
                    //System.out.printf("in varsSlot %b\n", _varsSlot.contains(varSlot));
                    //debug();
                    _removeSlot(slot);
                    _varsSlot.remove(varSlot);
                    //debug();
                    //System.out.println();
                }
            }
        } else if (_varsSlot.contains(x)) {
            int oldSlot = x.getOldValue(), slot = x.getValue();
            //System.out.println("change slot " + oldSlot + " ->  " + slot);
            //debug();
            //if (_occ[oldSlot - 1] > 0) {
                _removeSlot(oldSlot);
                _varsSlot.remove(x);
            //}
            _addSlot(slot);
            _varsSlot.add(x);
            //debug();
            //System.out.println();
        }

        if (!verify()) {
            /*
            debug();
            if (_indexOfProfVar.containsKey(x)) {
                int student = _indexOfProfVar.get(x);
                VarIntLS varSlot = _solutionSlot[student];
                int slot = varSlot.getValue();
                if (val == _prof)
                    System.out.println("add new slot " + slot);
                else if (x.getOldValue() == _prof)
                    System.out.println("remove slot " + slot);
            } else {
                int oldSlot = x.getOldValue(), slot = x.getValue();
                System.out.println("change slot " + oldSlot + " ->  " + slot);
            }
            */
            System.out.printf("value %d -> %d\n", oldValue, _value);
            debug();
            throw new RuntimeException();
        }
    }

    public void initPropagate() {
        _value = 0;
        int sl = _minSlot + 1;
        while (sl < _maxSlot) {
            if (_occ[sl-1] == 0) {
                ++_value;
                do { ++sl; } while (_occ[sl-1] == 0);
            }
            ++sl;
        }
    }

    public void debug() {
        System.out.printf("FConsecutive of prof %d: ", _prof);
        System.out.printf("occ = %s, value = %d, ", Arrays.toString(_occ), _value);
        System.out.printf("minSlot = %d, maxSlot = %d, ", _minSlot, _maxSlot);
        System.out.printf("varsSlot = [");
        for (Object o : _varsSlot) System.out.printf("%d, ", o.hashCode());
        System.out.printf("], varsSlot size = %d\n", _varsSlot.size());
    }

    public LocalSearchManager getLocalSearchManager() { return _ls; }

    public String name() { return "FConsecutive"; }

    public boolean verify() {
        int sz = 0;
        int minSlot = 1, maxSlot = _occ.length;
        while (minSlot <= _occ.length && _occ[minSlot - 1] == 0) ++minSlot;
        while (maxSlot >= 1 && _occ[maxSlot - 1] == 0) --maxSlot;

        if (minSlot > maxSlot) minSlot = maxSlot = -1;

        if (minSlot != _minSlot || maxSlot != _maxSlot) {
            System.out.printf("min / max not true %d/%d\n", minSlot, maxSlot);
            return false;
        }

        for (int c : _occ) {
            if (c < 0) return false;
            else sz += c;
        }
        if (sz != _varsSlot.size()) {
            System.out.printf("size not true %d\n", sz);
            return false;
        }

        int val = 0;
        int sl = _minSlot + 1;
        while (sl < _maxSlot) {
            if (_occ[sl-1] == 0) {
                ++val;
                do { ++sl; } while (_occ[sl-1] == 0);
            }
            ++sl;
        }
        if (val != _value) {
            System.out.printf("value not true %d\n", val);
            return false;
        }
        return true;
    }
}
