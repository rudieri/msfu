package br.com.msfu.sonar;

import java.util.ArrayList;

/**
 *
 * @author rudieri
 */
public class ExecucaoParalelaSonar {
    private final ArrayList<ParametrosSonar> parametros = new ArrayList<>();
    public void adicionarParametro(ParametrosSonar parametrosSonar){
        parametros.add(parametrosSonar);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public ArrayList<ParametrosSonar> getParametros() {
        return parametros;
    }
    
}
