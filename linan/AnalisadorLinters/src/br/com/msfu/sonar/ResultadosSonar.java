package br.com.msfu.sonar;

import java.util.ArrayList;

/**
 *
 * @author rudieri
 */
public class ResultadosSonar {

    private int pid;
    private long tempo;
    private final ArrayList<String> usoDeCpu = new ArrayList<>();
    private String erro;

    public void setPid(int pid) {
        this.pid = pid;
    }

    
    public void setTempo(long tempo) {
        this.tempo= tempo;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public ArrayList<String> getUsoDeCpu() {
        return usoDeCpu;
    }

    public long getTempo() {
        return tempo;
    }

    public int getPid() {
        return pid;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }
    public String getErro(){
        return erro;
    }
    
    
}
