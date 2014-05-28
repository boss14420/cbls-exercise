//package localsearch.search;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import localsearch.model.IConstraint;
import localsearch.model.IFunction;
import localsearch.model.VarIntLS;
import localsearch.search.*;

public class TabuSearch2
{
  private Random rand = null;

  public TabuSearch2() {
    this.rand = new Random();
  }

  public void greedySearchMinMultiObjectives(IFunction[] f, IConstraint[] S, int maxIter, int maxTime) {
    HashSet V = new HashSet();
    for (int i = 0; i < f.length; i++) {
      VarIntLS[] y = f[i].getVariables();
      if (y != null) {
        for (int j = 0; j < y.length; j++)
          V.add(y[j]);
      }
    }
    for (int i = 0; i < S.length; i++) {
      VarIntLS[] y = S[i].getVariables();
      if (y != null) {
        for (int j = 0; j < y.length; j++)
          V.add(y[j]);
      }
    }
    VarIntLS[] x = new VarIntLS[V.size()];
    HashMap map = new HashMap();
    int idx = -1;
    Iterator it = V.iterator();
    while (it.hasNext()) {
      VarIntLS xi = (VarIntLS)it.next();
      idx++;
      x[idx] = xi;
      map.put(xi, Integer.valueOf(idx));
    }

    int iter = 0;
    maxTime *= 1000;
    double t0 = System.currentTimeMillis();
    ArrayList moves = new ArrayList();
    int[] deltaF = new int[f.length];
    int[] deltaS = new int[S.length];
    while ((iter < maxIter) && (System.currentTimeMillis() - t0 < maxTime)) {
      moves.clear();
      for (int i = 0; i < x.length; i++) {
        for (int v = x[i].getMinValue(); v <= x[i].getMaxValue(); v++) if (x[i].isValue(v)) {
            boolean ok = true;
            for (int j = 0; j < f.length; j++) {
              deltaF[j] = f[j].getAssignDelta(x[i], v);
              if (deltaF[j] > 0) {
                ok = false;
                break;
              }
            }
            if (ok) for (int j = 0; j < f.length; j++) {
                deltaS[j] = S[j].getAssignDelta(x[i], v);
                if (deltaS[j] > 0) {
                  ok = false;
                  break;
                }
              }
            if (ok) {
              ok = false;
              for (int j = 0; j < f.length; j++) if (deltaF[j] < 0) ok = true;
              for (int j = 0; j < S.length; j++) if (deltaS[j] < 0) ok = true;
            }
            if (ok) {
              moves.add(new OneVariableValueMove(MoveType.OneVariableValueAssignment, -1.0D, x[i], v));
            }
          }
      }
      for (int i = 0; i < x.length - 1; i++) {
        for (int j = i + 1; j < x.length; j++) {
          boolean ok = true;
          for (int k = 0; k < f.length; k++) {
            deltaF[k] = f[k].getSwapDelta(x[i], x[j]);
            if (deltaF[k] > 0) {
              ok = false;
              break;
            }
          }
          for (int k = 0; k < S.length; k++) {
            deltaS[k] = S[k].getSwapDelta(x[i], x[j]);
            if (deltaS[k] > 0) {
              ok = false;
              break;
            }
          }
          if (ok) {
            ok = false;
            for (int k = 0; k < f.length; k++) if (deltaF[k] < 0) ok = true;
            for (int k = 0; k < S.length; k++) if (deltaS[k] < 0) ok = true;
          }
          if (ok) {
            moves.add(new TwoVariablesSwapMove(MoveType.TwoVariablesSwap, -1.0D, x[i], x[j]));
          }
        }
      }

      if (moves.size() <= 0) break;
      Move move = (Move)moves.get(this.rand.nextInt(moves.size()));
      if (move.getType() == MoveType.OneVariableValueAssignment) {
        OneVariableValueMove m1 = (OneVariableValueMove)move;
        VarIntLS sel_x = m1.getVariable();
        int sel_v = m1.getValue();
        sel_x.setValuePropagate(sel_v);
        System.out.println(name() + "::greedySearchMinMultiObjectives move assign x[" + sel_x.getID() + "] to " + sel_v);
      } else if (move.getType() == MoveType.TwoVariablesSwap) {
        TwoVariablesSwapMove m2 = (TwoVariablesSwapMove)move;
        VarIntLS xi = m2.getVar1();
        VarIntLS xj = m2.getVar2();
        int tmp = xi.getValue();
        xi.setValuePropagate(xj.getValue());
        xj.setValuePropagate(tmp);
        System.out.println(name() + "::greedySearchMinMultiObjectives move swap x[" + xi.getID() + "] and x[" + xj.getID() + "]");
      } else {
        System.out.println("TabuSearch::greedySearchMinMultiObjectives -> exception, unknown move");
        //if (!$assertionsDisabled) 
        throw new AssertionError();

      }

      iter++;
    }
  }
  public String name() { return "TabuSearch"; }


  public void searchMaintainConstraintsFunction(IFunction f1, IFunction[] f2, IConstraint S, int tabulen, int maxTime, int maxIter, int maxStable)
  {
    HashSet<VarIntLS> _S = new HashSet<VarIntLS>();

    VarIntLS[] a = S.getVariables();
    HashMap map = new HashMap();
    for (int i = 0; i < a.length; i++)
    {
      _S.add(a[i]);
    }
    VarIntLS[] b = f1.getVariables();
    if (b != null)
    {
      for (int i = 0; i < b.length; i++)
      {
        _S.add(b[i]);
      }

    }

    for (int i = 0; i < f2.length; i++)
    {
      VarIntLS[] c = f2[i].getVariables();

      if (c != null)
      {
        for (int j = 0; j < c.length; j++)
        {
          _S.add(c[j]);
        }

      }

    }

    VarIntLS[] x = new VarIntLS[_S.size()];
    int g1 = 0;
    for (VarIntLS e : _S)
    {
      x[g1] = e;
      g1++;
    }
    for (int i = 0; i < x.length; i++)
    {
      map.put(x[i], Integer.valueOf(i));
    }

    int[] f2Value = new int[f2.length];
    for (int i = 0; i < f2.length; ++i) {
        f2Value[i] = f2[i].getValue();
    }

    int n = x.length;
    int maxV = -1000000;
    int minV = 1000000;
    for (int i = 0; i < n; i++) {
      if (maxV < x[i].getMaxValue()) maxV = x[i].getMaxValue();
      if (minV > x[i].getMinValue()) minV = x[i].getMinValue();
    }
    System.out.println("minV  =   " + minV + "  maxV   =  " + maxV);
    int D = maxV - minV;

    int[][] tabu = new int[n][D + 1];
    for (int i = 0; i < n; i++) {
      for (int v = 0; v <= D; v++)
        tabu[i][v] = -1;
    }
    int it = 0;
    maxTime *= 1000;
    double t0 = System.currentTimeMillis();
    int best = f1.getValue();
    int[] x_best = new int[x.length];
    for (int i = 0; i < x.length; i++) x_best[i] = x[i].getValue();

    System.out.println("TabuSearch, init S = " + S.violations());
    int nic = 0;
    ArrayList moves = new ArrayList();
    Random R = new Random();

    while ((it < maxIter) && (System.currentTimeMillis() - t0 < maxTime)) {
      int sel_i = -1;
      int sel_v = -1;
      int minDelta = 100000000;
      moves.clear();
      for (int i = 0; i < n; i++) {
        for (int v = x[i].getMinValue(); v <= x[i].getMaxValue(); v++) {
          int deltaS = S.getAssignDelta(x[i], v);
          int maxDeltaF2 = -10000000;
          //int[] deltaF2 = new int[f2.length];
          for (int t = 0; t < f2.length; t++)
          {
            //deltaF2[t] = f2[t].getAssignDelta(x[i], v);
            if (f2[t].getAssignDelta(x[i], v) > maxDeltaF2)
                maxDeltaF2 = f2[t].getAssignDelta(x[i], v);
          }
          //Arrays.sort(deltaF2);
          int deltaF = f1.getAssignDelta(x[i], v);

          //if ((deltaS <= 0) && (deltaF2[(f2.length - 1)] <= 0) && ((tabu[i][(v - minV)] <= it) || (f1.getValue() + deltaF < best))) {
          if ((deltaS <= 0) && (maxDeltaF2 <= 0) && ((tabu[i][(v - minV)] <= it) || (f1.getValue() + deltaF < best))) {
            if (deltaF < minDelta) {
              minDelta = deltaF;
              sel_i = i;
              sel_v = v;
              moves.clear();
              moves.add(new OneVariableValueMove(MoveType.OneVariableValueAssignment, minDelta, x[i], v));
            } else if (deltaF == minDelta) {
              moves.add(new OneVariableValueMove(MoveType.OneVariableValueAssignment, minDelta, x[i], v));
            }
          }
        }
      }

      if (moves.size() <= 0) {
        System.out.println("TabuSearch::restart.....");
        restartMaintainConstraint(x, S, tabu);
        nic = 0;
      }
      else {
        if (moves.size() > 0)
        {
          OneVariableValueMove m = (OneVariableValueMove)moves.get(R.nextInt(moves.size()));

          sel_i = ((Integer)map.get(m.getVariable())).intValue();
          sel_v = m.getValue();
          if (sel_v >= 0)
          {
            x[sel_i].setValuePropagate(sel_v);
          }
        }

        if (sel_v >= 0)
        {
          tabu[sel_i][(sel_v - minV)] = (it + tabulen);
        }
        System.out.println("Step " + it + ", S = " + S.violations() + "   f2[0]   =     " + f2[0].getValue() + ", f1 = " + f1.getValue() + ", best = " + best + ", delta = " + minDelta + ", nic = " + nic);

        if (f1.getValue() < best) {
            boolean better = true;
            for (int i = 0; i < f2.length; ++i)
                if (f2[i].getValue() > f2Value[i]) {
                    better = false;
                    break;
                }

            if (better) {
                best = f1.getValue();
                for (int i = 0; i < x.length; i++) {
                    x_best[i] = x[i].getValue();
                }
            }
        }

        if (minDelta >= 0) {
          nic++;
          if (nic > maxStable) {
            System.out.println("TabuSearch::restart.....");
            restartMaintainConstraint(x, S, tabu);
            nic = 0;
          }
        } else {
          nic = 0;
        }
      }
      it++;
    }
    for (int i = 0; i < x.length; i++)
      x[i].setValuePropagate(x_best[i]);
  }

  public void searchMaintainConstraints(IFunction f, IConstraint S, int tabulen, int maxTime, int maxIter, int maxStable)
  {
    VarIntLS[] x = S.getVariables();
    HashMap map = new HashMap();
    for (int i = 0; i < x.length; i++) {
      map.put(x[i], Integer.valueOf(i));
    }
    int n = x.length;
    int maxV = -1000000000;
    int minV = 100000000;
    for (int i = 0; i < n; i++) {
      if (maxV < x[i].getMaxValue()) maxV = x[i].getMaxValue();
      if (minV > x[i].getMinValue()) minV = x[i].getMinValue();
    }
    int D = maxV - minV;
    int[][] tabu = new int[n][D + 1];
    for (int i = 0; i < n; i++) {
      for (int v = 0; v <= D; v++)
        tabu[i][v] = -1;
    }
    int it = 0;
    maxTime *= 1000;
    double t0 = System.currentTimeMillis();
    int best = f.getValue();
    int[] x_best = new int[x.length];
    for (int i = 0; i < x.length; i++) x_best[i] = x[i].getValue();

    System.out.println("TabuSearch, init S = " + S.violations());
    int nic = 0;
    ArrayList moves = new ArrayList();
    Random R = new Random();

    while ((it < maxIter) && (System.currentTimeMillis() - t0 < maxTime)) {
      int sel_i = -1;
      int sel_v = -1;
      int minDelta = 10000000;
      moves.clear();
      for (int i = 0; i < n; i++) {
        for (int v = x[i].getMinValue(); v <= x[i].getMaxValue(); v++) {
          int deltaS = S.getAssignDelta(x[i], v);
          int deltaF = f.getAssignDelta(x[i], v);

          if ((deltaS <= 0) && ((tabu[i][(v - minV)] <= it) || (f.getValue() + deltaF < best))) {
            if (deltaF < minDelta) {
              minDelta = deltaF;
              sel_i = i;
              sel_v = v;
              moves.clear();
              moves.add(new OneVariableValueMove(MoveType.OneVariableValueAssignment, minDelta, x[i], v));
            } else if (deltaF == minDelta) {
              moves.add(new OneVariableValueMove(MoveType.OneVariableValueAssignment, minDelta, x[i], v));
            }
          }
        }

      }

      if (moves.size() <= 0) {
        System.out.println("TabuSearch::restart.....");
        restartMaintainConstraint(x, S, tabu);
        nic = 0;
      } else {
        OneVariableValueMove m = (OneVariableValueMove)moves.get(R.nextInt(moves.size()));
        sel_i = ((Integer)map.get(m.getVariable())).intValue();
        sel_v = m.getValue();
        x[sel_i].setValuePropagate(sel_v);
        tabu[sel_i][(sel_v - minV)] = (it + tabulen);
        System.out.println("Step " + it + ", S = " + S.violations() + ", f = " + f.getValue() + ", best = " + best + ", delta = " + minDelta + ", nic = " + nic);

        if (f.getValue() <= best) {
          best = f.getValue();
          for (int i = 0; i < x.length; i++) {
            x_best[i] = x[i].getValue();
          }
        }
        if (minDelta >= 0) {
          nic++;
          if (nic > maxStable) {
            System.out.println("TabuSearch::restart.....");
            restartMaintainConstraint(x, S, tabu);
            nic = 0;
          }
        } else {
          nic = 0;
        }
      }
      it++;
    }

    for (int i = 0; i < x.length; i++)
      x[i].setValuePropagate(x_best[i]);
  }

  public void search(IConstraint S, int tabulen, int maxTime, int maxIter, int maxStable) {
    VarIntLS[] x = S.getVariables();
    HashMap map = new HashMap();
    for (int i = 0; i < x.length; i++) {
      map.put(x[i], Integer.valueOf(i));
    }
    int n = x.length;
    int maxV = -1000000;
    int minV = 1000000;
    for (int i = 0; i < n; i++) {
      if (maxV < x[i].getMaxValue()) maxV = x[i].getMaxValue();
      if (minV > x[i].getMinValue()) minV = x[i].getMinValue();
    }
    int D = maxV - minV;

    int[][] tabu = new int[n][D + 1];
    for (int i = 0; i < n; i++) {
      for (int v = 0; v <= D; v++)
        tabu[i][v] = -1;
    }
    int it = 0;
    maxTime *= 1000;
    double t0 = System.currentTimeMillis();
    int best = S.violations();
    int[] x_best = new int[x.length];
    for (int i = 0; i < x.length; i++) x_best[i] = x[i].getValue();

    System.out.println("TabuSearch, init S = " + S.violations());
    int nic = 0;
    ArrayList moves = new ArrayList();
    Random R = new Random();
    while ((it < maxIter) && (System.currentTimeMillis() - t0 < maxTime) && (S.violations() > 0)) {
      int sel_i = -1;
      int sel_v = -1;
      int minDelta = 10000000;
      moves.clear();
      for (int i = 0; i < n; i++) {
        for (int v = x[i].getMinValue(); v <= x[i].getMaxValue(); v++) {
          int delta = S.getAssignDelta(x[i], v);

          if ((tabu[i][(v - minV)] <= it) || (S.violations() + delta < best)) {
            if (delta < minDelta) {
              minDelta = delta;
              sel_i = i;
              sel_v = v;
              moves.clear();
              moves.add(new OneVariableValueMove(MoveType.OneVariableValueAssignment, minDelta, x[i], v));
            } else if (delta == minDelta) {
              moves.add(new OneVariableValueMove(MoveType.OneVariableValueAssignment, minDelta, x[i], v));
            }
          }
        }
      }

      if (moves.size() <= 0) {
        System.out.println("TabuSearch::restart.....");
        restartMaintainConstraint(x, S, tabu);
        nic = 0;
      }
      else {
        OneVariableValueMove m = (OneVariableValueMove)moves.get(R.nextInt(moves.size()));
        sel_i = ((Integer)map.get(m.getVariable())).intValue();
        sel_v = m.getValue();
        x[sel_i].setValuePropagate(sel_v);
        tabu[sel_i][(sel_v - minV)] = (it + tabulen);

        System.out.println("Step " + it + ", S = " + S.violations() + ", best = " + best + ", delta = " + minDelta + ", nic = " + nic);

        if (S.violations() < best) {
          best = S.violations();
          for (int i = 0; i < x.length; i++) {
            x_best[i] = x[i].getValue();
          }
        }
        if (minDelta >= 0) {
          nic++;
          if (nic > maxStable) {
            System.out.println("TabuSearch::restart.....");
            restartMaintainConstraint(x, S, tabu);
            nic = 0;
          }
        } else {
          nic = 0;
        }
      }
      it++;
    }
    for (int i = 0; i < x.length; i++)
      x[i].setValuePropagate(x_best[i]);
  }

  private void restart(VarIntLS[] x, int[][] tabu) {
    for (int i = 0; i < x.length; i++) {
      int d = x[i].getMaxValue() - x[i].getMinValue() + 1;
      int v = this.rand.nextInt() % d;
      if (v < 0) v = -v;
      v = x[i].getMinValue() + v;
      x[i].setValuePropagate(v);
    }
    for (int i = 0; i < tabu.length; i++)
      for (int j = 0; j < tabu[i].length; j++)
        tabu[i][j] = -1;
  }

  private void restartMaintainConstraint(VarIntLS[] x, IConstraint S, int[][] tabu)
  {
    for (int i = 0; i < x.length; i++) {
      ArrayList L = new ArrayList();
      for (int v = x[i].getMinValue(); v <= x[i].getMaxValue(); v++) {
        if (S.getAssignDelta(x[i], v) <= 0)
          L.add(Integer.valueOf(v));
      }
      int idx = this.rand.nextInt(L.size());
      int v = ((Integer)L.get(idx)).intValue();
      x[i].setValuePropagate(v);
    }
    for (int i = 0; i < tabu.length; i++)
      for (int j = 0; j < tabu[i].length; j++)
        tabu[i][j] = -1;
  }

  public static void main(String[] args)
  {
  }
}
