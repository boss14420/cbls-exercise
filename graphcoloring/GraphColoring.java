import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Random;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.File;

import localsearch.constraints.NotEqual;
import localsearch.model.ConstraintSystem;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.selectors.MinMaxSelector;


public class GraphColoring {
    private boolean[][] has_edge;
    private int vertices;
    VarIntLS[] x;
    Random R;


    static private String[] colorList = {"aquamarine", "azure", "beige", "bisque", "blanchedalmond", "blue", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson", "cyan", "darkgoldenrod", "darkgreen", "darkkhaki", "darkolivegreen", "darkorange", "darkorchid", "darksalmon", "darkseagreen", "darkslateblue", "darkslategray", "darkslategrey", "darkturquoise", "darkviolet", "deeppink", "deepskyblue", "dimgray", "dimgrey", "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro", "ghostwhite", "gold", "goldenrod", "gray", "grey", "green", "greenyellow", "honeydew", "hotpink", "indianred", "indigo", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink", "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey", "lightsteelblue", "lightyellow", "lime", "limegreen", "linen", "magenta", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab", "orange", "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue", "purple", "red", "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray", "slategrey", "snow", "springgreen", "steelblue", "tan", "teal", "thistle", "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke", "yellow", "yellowgreen"};

    public GraphColoring(String filename) {
        readGraph(filename);
        R = new Random();
    }

    public int solve(boolean verbose) {
        LocalSearchManager ls = new LocalSearchManager();
        ConstraintSystem s = new ConstraintSystem(ls);

        HashMap<VarIntLS, Integer> map = new HashMap<VarIntLS, Integer>();
        HashSet<Integer> nonUsingColors = new HashSet<Integer>();

        x = new VarIntLS[vertices];
        int[] vcount = new int[vertices];
        for (int i = 0; i < vertices; i++) {
            x[i] = new VarIntLS(ls, 0, vertices - 1);
            //int v = R.nextInt(4);
            int v = 0;
            x[i].setValue(v);
            ++vcount[v];
            map.put(x[i], i);
        }
        for (int v = 1; v != vertices; ++v) nonUsingColors.add(v);

        // generate random edges
        //boolean[][] has_edge = new boolean[vertices][vertices];
        for (int i = 0; i != vertices; ++i)
            for (int j = i + 1; j != vertices; ++j) {
                if (has_edge[i][j]) {
                    has_edge[i][j] = has_edge[j][i] = true;
                    s.post(new NotEqual(x[i], x[j]));
                } else {
                    has_edge[i][j] = has_edge[j][i] = false;
                }
            }


        ls.close();


        if (verbose)
            System.out.println("Init S = " + s.violations());
        MinMaxSelector mms = new MinMaxSelector(s);

        int it = 0;
        ArrayDeque<Integer> tabuList = new ArrayDeque<Integer>();
        ArrayList<Integer> mostPromissingValues = new ArrayList<Integer>();
        int tabuSize = 0;

        while (it < 10000 && s.violations() > 0) {
            mostPromissingValues.clear();
            int bestAssignDelta = -1;
            VarIntLS sel_x = mms.selectMostViolatedVariable();
            //int sel_v = mms.selectMostPromissingValue(sel_x);
            int sel_v;

            for(int v = 0; v != vertices; ++v) {
                if (v != sel_x.getValue()) {
                    if (vcount[v] > 0 && !tabuList.contains(map.get(sel_x) * vertices + v))
                    {
                        if (s.getAssignDelta(sel_x, v) < bestAssignDelta)
                        {
                            bestAssignDelta = s.getAssignDelta(sel_x, v);
                            mostPromissingValues.clear();
                            mostPromissingValues.add(v);
                        } else if (s.getAssignDelta(sel_x, v) == bestAssignDelta) {
                            mostPromissingValues.add(v);
                        }
                    }
                }
            }

            if (mostPromissingValues.isEmpty()) {
                sel_v = nonUsingColors.iterator().next();
                nonUsingColors.remove(sel_v);
            } else {
                sel_v = mostPromissingValues.get(Math.abs(R.nextInt()) % mostPromissingValues.size());
            }

            --vcount[sel_x.getValue()];
            ++vcount[sel_v];

            if (vcount[sel_x.getValue()] == 0) {
                nonUsingColors.add(sel_x.getValue());
            }

            tabuList.add(map.get(sel_x) * vertices + sel_v);
            if (tabuList.size() > tabuSize)
                tabuList.removeFirst();

            sel_x.setValuePropagate(sel_v);

            if (verbose)
                System.out.println("Step " + it + ", x[" + map.get(sel_x) + "] = " + sel_v 
                        + ", s = " + s.violations() 
                        + ", using " + (vertices - nonUsingColors.size()) + " colors");

            it++;
        }

        if (verbose) {
            System.out.println("\nColors:");
            for (VarIntLS c : x)
                System.out.print(c.getValue() + " ");
            System.out.println();
        }

        int numColors = vertices - nonUsingColors.size();

        nonUsingColors.clear();
        map.clear();

        //return s.violations();
        return (s.violations() == 0) ? numColors : -numColors;
    }

    public void readGraph(String filename) {
        int edges;
        char ch;
        Scanner scanner;

        try {
            scanner = new Scanner(new File(filename));

            int start, end;
            while(scanner.hasNext()) {
                ch = scanner.findInLine(".").charAt(0);

                if (ch == 'p') {
                    scanner.next();
                    vertices = scanner.nextInt();
                    edges = scanner.nextInt();

                    has_edge = new boolean[vertices][vertices];
                } else if (ch == 'e') {
                    start = scanner.nextInt();
                    end = scanner.nextInt();
                    has_edge[start-1][end-1] = has_edge[end-1][start-1] = true;
                } else {
                }
                scanner.nextLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void exportGraph(String filename) {
        // print
        PrintWriter wt;
        try {
            wt = new PrintWriter(filename);
            wt.write("digraph G {\n");

            for (int i = 0; i != vertices; ++i) {
                for (int j = 0; j != vertices; ++j) {
                    if (i < j && has_edge[i][j])
                        wt.write(i + "->" + j + " [dir=none]\n");
                }
            }

            for (int i = 0; i != vertices; ++i) {
                wt.write(i + " [style=filled,label = " + i + ", fillcolor=" + colorList[x[i].getValue()] + "]\n");
            }
            wt.write("}\n");
            wt.close();
        } catch (Exception e) {
        }
    }

    public void test(int testNum) {
        double sum = 0, sumsquare = 0, mean, stddev;
        int min = vertices, max = 0, count = 0;

        for (int tn = 0; tn != testNum; ++tn) {
            int numColors = solve(false);

            if (numColors > 0) {
                if (numColors < min)
                    min = numColors;
                else if (numColors > max)
                    max = numColors;
                sum += numColors;
                sumsquare += numColors * numColors;
                ++count;
            }

            System.out.println("Test " + tn + ", use " + numColors + " color");
        }

        mean = sum / count;
        //stddev = Math.sqrt(mean * mean + (sumsquare - 2 * sum * mean) / testNum);
        stddev = Math.sqrt(sumsquare / count - mean * mean);
        System.err.println("min = " + min + ", max = " + max + ", mean = " + mean
                            + ", stddev = " + stddev + ", failed = " + (testNum - count));
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: ./graph-coloring.sh FILENAME [TEST_NUM]");
        }

        String filename = args[0];
        int testNum = 0;
        boolean verbose = false;
        if (args.length > 1) {
            testNum = Integer.valueOf(args[1]);
        }
        
        GraphColoring gc = new GraphColoring(filename);

        if (testNum == 0) {
            gc.solve(true);
            gc.exportGraph("graph-colored.dot");
        } else {
            gc.test(testNum);
        }
    }

}
