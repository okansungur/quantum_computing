import org.redfx.strange.*;

import org.redfx.strange.gate.*;
import org.redfx.strange.local.*;
import org.redfx.strangefx.render.Renderer;
import java.util.*;

public class MyGrover {
    static SimpleQuantumExecutionEnvironment sqee = new SimpleQuantumExecutionEnvironment();



    public static void main (String[] args) {
        doGrover(3,5);
    }

    private static void doGrover(int numofqubit, int solution) {
        int N = 1 << numofqubit;

        double cnt = Math.PI*Math.sqrt(N)/4;

        Program myprogram = new Program(numofqubit);
        Step steph = new Step();
        for (int i = 0; i < numofqubit; i++) {
            steph.addGate(new Hadamard(i));
        }
        myprogram.addStep(steph);
        Oracle oracle = createOracle(numofqubit, solution);
        oracle.setCaption("O");
        Complex[][] dif = createDiffMatrix(numofqubit);
        Oracle difOracle = new Oracle(dif);
        difOracle.setCaption("D");
        for (int i = 1; i < cnt; i++) {
            Step prob0 = new Step("Prob "+i);
            prob0.addGate(new ProbabilitiesGate(0));
            Step step1 = new Step("Oracle "+i);
            step1.addGate(oracle);

            Step step2 = new Step("Diffuser "+i);
            step2.addGate(difOracle);
            Step step3 = new Step("Prob "+i);
            step3.addGate(new ProbabilitiesGate(0));
            myprogram.addStep(step1);
            myprogram.addStep(step2);
            myprogram.addStep(step3);

        }


        Result result = sqee.runProgram(myprogram);
        Complex[] probability = result.getProbability();

        Renderer.renderProgram(myprogram);
        Renderer.showProbabilities(myprogram,4); //display  probabilities  over x runs


        List <Complex>liste=Arrays.asList(probability);
        for(int i=0;i<liste.size();i++){
            if(Math.round(liste.get(i).abssqr())==1)
                System.out.println("Answer is => "+i);
        }

    }

    static Complex[][] createDiffMatrix(int numofqubit) {
        int N = 1<<numofqubit;
        Gate gate = new Hadamard(0);
        Complex[][] matrix = gate.getMatrix();
        Complex[][] cmp1 = matrix;
        for (int i = 1; i < numofqubit; i++) {
            cmp1 = sqee.tensor(cmp1, matrix);
        }
        Complex[][] cmp2 = new Complex[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j <N;j++) {
                if (i!=j) {
                    cmp2[i][j] = Complex.ZERO;
                } else {
                    cmp2[i][j] = Complex.ONE;
                }
            }
        }
        cmp2[0][0] = Complex.ONE.mul(-1);
        int nd = numofqubit<<1;

        Complex[][] inter1 = mmul(cmp1,cmp2);
        Complex[][] dif = mmul(inter1, cmp1);
        return dif;
    }


    static Complex[][] mmul(Complex[][] a, Complex[][]b) {
        int arow = a.length;
        int acol = a[0].length;
        int brow = b.length;
        int bcol = b[0].length;
        if (acol != brow) throw new RuntimeException("#cols a "+acol+" != #rows b "+brow);
        Complex[][] answ = new Complex[arow][bcol];
        for (int i = 0; i < arow; i++) {
            for (int j = 0; j < bcol; j++) {
                Complex el = Complex.ZERO;
                for (int k = 0; k < acol;k++) {
                    el = el.add(a[i][k].mul(b[k][j]));
                }
                answ[i][j] = el;
            }
        }

        return answ;
    }

    static Oracle createOracle(int numofqubit, int solution) {
        int N = 1<<numofqubit;
        System.err.println("numofqubit = "+numofqubit+"  N = "+N);
        Complex[][] mymatrix = new Complex[N][N];
        for (int i = 0; i < N;i++) {
            for (int j = 0 ; j < N; j++) {
                if (i != j) {
                    mymatrix[i][j] = Complex.ZERO;
                } else {
                    if (i == solution) {
                        mymatrix[i][j] = Complex.ONE.mul(-1);
                    } else {
                        mymatrix[i][j] = Complex.ONE;
                    }
                }
            }
        }
        Oracle answer = new Oracle(mymatrix);

        return answer;
    }
}