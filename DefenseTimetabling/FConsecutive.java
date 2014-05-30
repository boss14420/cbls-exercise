import java.util.HashSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.Map;
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

    private final int _numStudents;
    private final VarIntLS[][] _solutionPro;
    private final VarIntLS[] _solutionSlot;
    private final HashMap<VarIntLS, Integer> _indexOfProfVar;

    private HashMap<VarIntLS, Integer> _varsSlot;       // Slot set

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

        _varsSlot = new HashMap<VarIntLS, Integer>();
        for (int s = 0; s < _numStudents; ++s) {
            for (VarIntLS p : _solutionPro[s])
                if (p.getValue() == _prof) {
                    VarIntLS slotVar = _solutionSlot[s];
                    int slot = slotVar.getValue();
                    ++_occ[slot-1];
                    if (!_varsSlot.containsKey(slotVar)) {
                        _varsSlot.put(slotVar, 1);
                        
                        if (slot < _minSlot)
                            _minSlot = slot;
                        if (slot > _maxSlot)
                            _maxSlot = slot;
                    } else {
                        _varsSlot.put(slotVar, _varsSlot.get(slotVar) + 1);
                    }
                }
        }

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

    // Get delta when adding slot
    private int _getAddDelta(int val) {
        // System.out.printf("getAddDelta(%d)\n", val);
        // System.out.printf("min = %d, max = %d, occ = %s\n", minSlot, maxSlot, Arrays.toString(occurrence));

        if (_maxSlot < _minSlot)
            return 0; 
        if (_occ[val - 1] > 0)
            return 0;
        if (val < _minSlot)
            return (val == _minSlot - 1) ? 0 : 1;
        if (val > _maxSlot)
            return (val == _maxSlot + 1) ? 0 : 1;

        //try {
        if (_occ[val - 1 - 1] > 0 && _occ[val + 1 - 1] > 0)
            return -1;
        if (_occ[val - 1 - 1] == 0 && _occ[val + 1 - 1] == 0)
            return 1;
        //} catch (Exception e) {
        //  System.out.printf("getAddDelta(%d)\n", val);
        //  debug();
        //  throw e;
        //  }
        //
        return 0;
    }

    // Get delta when removing slot
    private int _getRemoveDelta(int val, int num) {
        // if (occurrence[val - getMinValue()] > 0) return 0;
        if (_occ[val - 1] > num || _occ[val - 1] == 0)
            return 0;

        if (_maxSlot == _minSlot)
            return 0;
        if (val == _minSlot)
            return (_occ[_minSlot + 1 - 1] == 0) ? -1 : 0;
        if (val == _maxSlot)
            return (_occ[_maxSlot - 1 - 1] == 0) ? -1 : 0;

        if (_occ[val - 1 - 1] > 0 && _occ[val + 1 - 1] > 0)
            return 1;
        if (_occ[val - 1 - 1] == 0 && _occ[val + 1 - 1] == 0)
            return -1;
        return 0;
    }

    // -------------------------------------------------------------------------
    // Get assign delta by value
    private int _getAssignDelta(int oldVal, int newVal, int num) {
        int oldMin = _minSlot, oldMax = _maxSlot;
        int removeDelta = _getRemoveDelta(oldVal, num);
        
        _occ[oldVal - 1] -= num;
        _updateMinMaxAfterRemove(oldVal);

        int addDelta = _getAddDelta(newVal);
        _occ[oldVal - 1] += num;

        _minSlot = oldMin; _maxSlot = oldMax;

        return removeDelta + addDelta;
    }
    
    @Override
    public int getAssignDelta(VarIntLS variable, int val) {
        if (variable.getValue() == val)
            return 0;

        if (_varsSlot.containsKey(variable)) {
            return _getAssignDelta(variable.getValue(), val, 
                                   _varsSlot.get(variable));
        }

        if (_indexOfProfVar.containsKey(variable)) {
            int student = _indexOfProfVar.get(variable);
            VarIntLS varSlot = _solutionSlot[student];
            
            if (variable.getValue() == _prof) {
                if (_occ[varSlot.getValue() - 1] > 0 && 
                        _varsSlot.containsKey(varSlot))
                    return _getRemoveDelta(varSlot.getValue(), 1);
                return 0;
            }
            
            if (val == _prof) {
                if (!_varsSlot.containsKey(varSlot))
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
        if (_minSlot == _maxSlot && slot == _minSlot && _occ[slot-1] == 0) {
            _minSlot = _solutionSlot[0].getMaxValue() + 1;
            _maxSlot = _solutionSlot[0].getMinValue() - 1;
            return;
        }

        // System.out.printf("updateMinMaxAfterRemove(%d)\n", slot);
        // debug();
        
        // Update min slot
        if (slot == _minSlot && _occ[slot - 1] == 0) {
            int v;
            boolean found = false;
            for (v = _minSlot + 1; v <= _maxSlot && !found; ++v)
                if (_occ[v - 1] > 0) {
                    found = true;
                    // break;
                }
            
            if (found) {
                _minSlot = v - 1;
                // System.out.println("Min slot <- " + minSlot);
            } else _minSlot = _solutionSlot[0].getMaxValue()+1;
        }
        // System.out.println("Min slot " + minSlot);

        // Update max slot
        if (slot == _maxSlot && _occ[slot - 1] == 0) {
            int v;
            boolean found = false;
            for (v = _maxSlot - 1; v >= _minSlot && !found; --v)
                if (_occ[v - 1] > 0) {
                    found = true;
                    // break;
                }
            
            if (found) {
                _maxSlot = v + 1;
                // System.out.println("Max slot <- " + maxSlot);
            } else _maxSlot = _solutionSlot[0].getMinValue() - 1;
        }
        // System.out.println("Max slot " + maxSlot);
        
        // debug();
        // System.out.printf("updateMinMaxAfterRemove(%d) finished!\n\n", slot);
    }

    // Remove slot
    private void _removeSlot(int slot, int num) {
        _setValue(getValue() + _getRemoveDelta(slot, num));
        _occ[slot - 1] -= num;
        _updateMinMaxAfterRemove(slot);
    }

    // Add slot
    private void _addSlot(int slot, int num) {
        if (_varsSlot.isEmpty()) {
            _minSlot = _maxSlot = slot;
            _occ[slot - 1] += num;
            _setValue(0);
        }
        else    {
            _setValue(getValue() + _getAddDelta(slot));
            _occ[slot - 1] += num;
            if (slot > _maxSlot) {
                _maxSlot = slot;
            }
            if (slot < _minSlot) {
                _minSlot = slot;
            }
        }
    }

    @Override
    public void propagateInt(VarIntLS variable, int val) {
        if (variable.getOldValue() == val)
            return;

        int oldValue = getValue();

        if (_indexOfProfVar.containsKey(variable)) {
            int student = _indexOfProfVar.get(variable);
            VarIntLS varSlot = _solutionSlot[student];
            int slot = varSlot.getValue();

            if (val == _prof) {
//                System.out.println("add new slot " + slot);
//                debug();
                if (_varsSlot.containsKey(varSlot)) {
                    _varsSlot.put(varSlot, _varsSlot.get(varSlot)+1);
                    ++_occ[slot - 1];
                } else {
                    _addSlot(slot, 1);
                    _varsSlot.put(varSlot, 1);
                }
//                debug();
//                System.out.println();
            }

            if (variable.getOldValue() == _prof) {
//                System.out.printf("remove slot %d, hash %d\n", slot, varSlot.hashCode());
//                debug();
                if (_occ[slot - 1] > 0 && _varsSlot.containsKey(varSlot)) {
                    // System.out.printf("in varsSlot %b\n", varsSlot.contains(varSlot));
                    _removeSlot(slot, 1);
                    _varsSlot.put(varSlot, _varsSlot.get(varSlot) - 1);
                    if (_varsSlot.get(varSlot) == 0)
                        _varsSlot.remove(varSlot);
                }
//                debug();
//                System.out.println();
            }
        } else if (_varsSlot.containsKey(variable)) {
            int oldSlot = variable.getOldValue(), slot = variable.getValue();
//            System.out.println("change slot " + oldSlot + " ->  " + slot);
//            debug();
            int num = _varsSlot.get(variable);
            // if (occurrence[oldSlot - 1] > 0) {
                _removeSlot(oldSlot, num);
                _varsSlot.remove(variable);
            // }
            _addSlot(slot, num);
            _varsSlot.put(variable, num);
//            debug();
//            System.out.println();
        }

        //if (!verify()) {
            /*
            debug();
            if (indexOfProfVar.containsKey(variable)) {
                int student = indexOfProfVar.get(variable);
                VarIntLS varSlot = solutionSlot[student];
                int slot = varSlot.getValue();
                if (val == prof)
                    System.out.println("add new slot " + slot);
                else if (variable.getOldValue() == prof)
                    System.out.println("remove slot " + slot);
            } else {
                int oldSlot = variable.getOldValue(), slot = variable.getValue();
                System.out.println("change slot " + oldSlot + " ->  " + slot);
            }
            */
//            System.out.printf("value %d -> %d\n", oldValue, getValue());
//            debug();
//            throw new RuntimeException();
        //}
        
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

    private void _setValue(int newVal) {
        _value = newVal;
    }
    
    // Debug and check all variables of this function
    public void debug() {
        System.out.printf("FConsecutive of prof %d: ", _prof);
        System.out.printf("occurrence = %s, value = %d, ", Arrays.toString(_occ), getValue());
        System.out.printf("minSlot = %d, maxSlot = %d, ", _minSlot, _maxSlot);
        System.out.printf("varsSlot = [");
        
        for (Map.Entry<VarIntLS, Integer> e : _varsSlot.entrySet())
            System.out.printf("(%d, %d), ", e.getKey().hashCode(), e.getValue());
        System.out.printf("], varsSlot size = %d\n", _varsSlot.size());
    }

    public LocalSearchManager getLocalSearchManager() { return _ls; }

    public String name() { return "FConsecutive"; }

    // Verify this function
    @Override
    public boolean verify() {
        // Verify min and max slot
        int min = 1, max = _occ.length;
        
        while (min <= _occ.length && _occ[min - 1] == 0)
            ++min;
        while (max >= 1 && _occ[max - 1] == 0)
            --max;

        if (min > max) {
            min = _solutionSlot[0].getMaxValue() + 1;
            max = _solutionSlot[0].getMinValue() - 1;
        }

        if (min != this._minSlot || max != this._maxSlot) {
            System.out.printf("min / max not true %d/%d\n", min, max);
            return false;
        }

        // Verify occurrence array size
        int sz1 = 0, sz2 = 0;
        for (int occ : _varsSlot.values()) {
            if (occ <= 0) return false;
            else sz2 += occ;
        }
        for (int occ : _occ) {
            if (occ < 0) return false;
            else sz1 += occ;
        }

        if (sz1 != sz2) {
            System.out.printf("size not true %d, %d\n", sz1, sz2);
            return false;
        }

        // Verify value
        int val = 0;
        int sl = min + 1;
        
        while (sl < max) {
            if (_occ[sl-1] == 0) {
                ++val;
                
                do { ++sl; }
                while (_occ[sl-1] == 0);
            }
            ++sl;
        }
        
        if (val != getValue()) {
            System.out.printf("value not true %d\n", val);
            return false;
        }
        return true;
    }
}
