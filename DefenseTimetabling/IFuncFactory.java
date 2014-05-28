import java.util.HashSet;
import java.util.HashMap;
import localsearch.model.*;

public class IFuncFactory
{
    private HashMap<VarIntLS, Integer> _indexOfSlotVar;
    private HashMap<VarIntLS, Integer> _indexOfProfVar;

    private VarIntLS[][] _solutionPro;
    private VarIntLS[] _solutionRoom;
    private VarIntLS[] _solutionSlot;

    private int _numProfs;
    private int _numStudents;
    private int _numSlots;

    private FRoomOfProf[] _rpFuncs;
    private FConsecutive[] _consecutiveFuncs;

    public IFuncFactory(VarIntLS[][] solutionPro, VarIntLS[] solutionSlot, VarIntLS[] solutionRoom)
    {
        //
        //

        _solutionPro = solutionPro;
        _solutionRoom = solutionRoom;
        _solutionSlot = solutionSlot;

        _indexOfSlotVar = new HashMap<VarIntLS, Integer>();
        _indexOfProfVar = new HashMap<VarIntLS, Integer>();

        _numStudents = _solutionSlot.length;
        for (int s = 0; s < _numStudents; ++s) {
            _indexOfSlotVar.put(_solutionSlot[s], s);
            for (int i = 0; i < 5; ++i)
                _indexOfProfVar.put(_solutionPro[s][i], s);
        }


        //
        //

        _numProfs = _solutionPro[0][0].getMaxValue() + 1;
        _numSlots = _solutionSlot[0].getMaxValue();

        //
        generateAllIFunctions();
    }

    private void generateAllIFunctions()
    {
        /*
        _rpFuncs = new FRoomOfProf[_numProfs * _numSlots];
        for (int p = 0; p < _numProfs; ++p)
            for (int sl = 0; sl < _numSlots; ++sl)
                _rpFuncs[sl + p * _numSlots] = new FRoomOfProf(p, sl, _solutionPro, _solutionSlot, 
                                                               _solutionRoom, _indexOfProfVar, 
                                                               _indexOfSlotVar);
        */


        VarIntLS[] variables = new VarIntLS[_numStudents * 6];
        for (int s = 0; s < _numStudents; ++s) {
            for (int i = 0; i < 5; ++i) variables[s*6+i] = _solutionPro[s][i];
            variables[s*6+5] = _solutionSlot[s];
        }
        _consecutiveFuncs = new FConsecutive[_numProfs];
        for (int p = 0; p < _numProfs; ++p)
            _consecutiveFuncs[p] = new FConsecutive(p, _solutionPro, _solutionSlot, _indexOfProfVar, variables);
    }

    public FRoomOfProf getRPFunc(int p, int sl) {
        return _rpFuncs[p * _numSlots + sl - 1];
    }

    public FConsecutive getConsecutiveFunc(int p) {
        return _consecutiveFuncs[p];
    }
}
