//import localsearch.constrains.alldifferent.AllDifferentVarIntLS;
import localsearch.model.*;


public class AllDifferentVarIntLSInt extends AbstractInvariant
  implements IConstraint
{
  private int _violations;
  private int _minValue;
  private int _maxValue;
  private int[] _occ;
  private VarIntLS[] _x;
  private int[] _v;
  private LocalSearchManager _ls;

  public AllDifferentVarIntLSInt(VarIntLS[] x, int[] v)
  {
    this._x = x;
    this._v = v;
    this._ls = x[0].getLocalSearchManager();
    for (int i = 0; i < x.length; i++) {
      this._minValue = Math.min(this._minValue, x[i].getMinValue());
      this._maxValue = Math.max(this._maxValue, x[i].getMaxValue());
    }
    for (int i = 0; i < v.length; ++i) {
      this._minValue = Math.min(this._minValue, v[i]);
      this._maxValue = Math.max(this._maxValue, v[i]);
    }
    post();
    this._ls.post(this);
  }
  public String name() { return "AllDiferent"; } 
  private void post() {
    this._occ = new int[this._maxValue - this._minValue + 1];
    //for (int i = 0; i < this._occ.length; i++) this._occ[i] = 0;
    for (int value : _v) ++this._occ[value - this._minValue];
  }

  public int violations()
  {
    return this._violations;
  }

  public int violations(VarIntLS x) {
    int v = this._occ[(x.getValue() - this._minValue)];
    return x.IsElement(this._x) ? Math.max(0, v - 1) : 0;
  }

  public VarIntLS[] getVariables()
  {
    return this._x;
  }

  public int getAssignDelta(VarIntLS x, int val)
  {
    if ((!x.IsElement(this._x)) || (x.getValue() == val)) return 0;
    int newV = this._violations;
    int v1 = x.getValue() - this._minValue;
    int v2 = val - this._minValue;
    if (this._occ[v1] > 1) newV--;
    if (this._occ[v2] > 0) newV++;
    return newV - this._violations;
  }

  public int getSwapDelta(VarIntLS x, VarIntLS y)
  {
    if (!x.IsElement(this._x))
      return 0;
    if ((x.IsElement(this._x)) && (y.IsElement(this._x)))
      return 0;
    int newV = this._violations;
    int v1 = x.getValue() - this._minValue;
    int v2 = y.getValue() - this._minValue;
    if (this._occ[v1] > 1) newV--;
    if (this._occ[v2] > 0) newV++;
    return newV - this._violations;
  }

  public void propagateInt(VarIntLS x, int val)
  {
    if (!x.IsElement(this._x)) return;
    int v1 = x.getOldValue() - this._minValue;
    int v2 = val - this._minValue;
    if (v1 == v2) return;
    if (this._occ[v1] > 1) this._violations -= 1;
    this._occ[v1] -= 1;
    if (this._occ[v2] > 0) this._violations += 1;
    this._occ[v2] += 1;
  }

  public void initPropagate()
  {
    this._violations = 0;
    for (VarIntLS e : this._x) this._occ[(e.getValue() - this._minValue)] += 1;
    for (int i = 0; i < this._occ.length; i++) this._violations += Math.max(0, this._occ[i] - 1); 
  }

  public void print()
  {
    for (int i = 0; i < this._x.length; i++) {
      System.out.println("_x[" + i + "] = " + this._x[i].getValue());
    }
    for (int i = 0; i < this._v.length; i++) {
      System.out.println("_v[" + i + "] = " + this._v[i]);
    }
    for (int v = this._minValue; v <= this._maxValue; v++)
      System.out.println("_occ[" + v + "] = " + this._occ[v]);
  }

  public boolean verify()
  {
    int[] occ = new int[this._maxValue - this._minValue + 1];
    for (int i = 0; i < occ.length; i++) {
      occ[i] = 0;
    }
    for (int i = 0; i < this._x.length; i++) {
      int v = this._x[i].getValue();
      occ[(v - this._minValue)] += 1;
    }
    for (int value : _v) {
        ++occ[value - _minValue];
    }
    for (int v = this._minValue; v <= this._maxValue; v++) {
      if (this._occ[v] != occ[v]) {
        System.out.println(name() + "::verify failed, _occ[" + v + "] = " + this._occ[v] + " differs from occ[" + 
          v + "] = " + occ[v] + " by recomputation");
        return false;
      }
    }

    int violations = 0;
    for (int v = this._minValue; v <= this._maxValue; v++) {
      violations += Math.max(occ[v] - 1, 0);
    }
    if (violations != this._violations) {
      System.out.println(name() + "::verify failed, _violations = " + this._violations + " differs from violations = " + 
        violations + " by recomputation");
    }
    return true;
  }

  public LocalSearchManager getLocalSearchManager()
  {
    return this._ls;
  }

  public static void main(String[] args)
  {
    int n = 5;
    LocalSearchManager _ls = new LocalSearchManager();
    ConstraintSystem S = new ConstraintSystem(_ls);
    VarIntLS[] x = new VarIntLS[n];
    for (int i = 0; i < n; i++)
    {
      x[i] = new VarIntLS(_ls, 0, 100);
    }

    x[0].setValue(1);
    x[1].setValue(2);
    x[2].setValue(2);
    x[3].setValue(2);
    x[4].setValue(3);

    int[] v = {2};
    S.post(new AllDifferentVarIntLSInt(x, v));
    S.close();
    _ls.close();
    System.out.println(S.violations());
    System.out.println(S.getAssignDelta(x[2], 2));
  }
}
