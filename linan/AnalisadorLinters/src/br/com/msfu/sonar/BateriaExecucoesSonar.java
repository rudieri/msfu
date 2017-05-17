package br.com.msfu.sonar;

import br.com.msfu.gui.JAnalisadorLinter;
import br.com.msfu.utils.FormatadorCsv;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author rudieri
 */
public class BateriaExecucoesSonar implements FormatadorCsv {

    private final HashMap<ParametrosSonar, ArrayList<ResultadosSonar>> resultados = new HashMap<>();
    private final HashSet<ParametrosSonar> execucoesAgendadas = new HashSet<>();
    
    
    public void agendarExecucao(ParametrosSonar param){
        execucoesAgendadas.add(param);
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public HashSet<ParametrosSonar> getExecucoesAgendadas() {
        return execucoesAgendadas;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public HashMap<ParametrosSonar, ArrayList<ResultadosSonar>> getResultados() {
        return resultados;
    }
    
    public void iniciar() throws IOException, FileNotFoundException, SQLException, InterruptedException{
        for (ParametrosSonar param : execucoesAgendadas) {
            executar(param);
        }
    }
    
    private void executar(ParametrosSonar param) throws FileNotFoundException, IOException, SQLException, InterruptedException {
        File projeto = new File(param.getProjeto());
        Properties sonarProject = new Properties();
        try (FileInputStream in = new FileInputStream(new File(projeto, "sonar-project.properties"));) {
            sonarProject.load(in);
        }

        SonarUtils.definirPerfil(sonarProject.getProperty("sonar.projectKey"), param.getPerfil());

        for (int i = 0; i < param.getNroTestes(); i++) {
            String[] comando = montarComando(projeto, param.getDestinoHtml(), param.getPerfil(), i);

            long tempo = System.currentTimeMillis();
            ArrayList<ResultadosSonar> lista = resultados.get(param);
            if (lista == null) {
                lista = new ArrayList<>(param.getNroTestes());
                resultados.put(param, lista);
            }
            ResultadosSonar res = new ResultadosSonar();
            lista.add(res);
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.directory(projeto);
            pb.redirectErrorStream(true);
            Process processo = pb.start();
            try(InputStream in = processo.getInputStream()){
                byte[] buffer = new byte[1024];
                while (in.read(buffer) < 0);
            }
//            Process processo = Runtime.getRuntime().exec(comando, null, projeto);
//            Process processo = Runtime.getRuntime().exec(comando, null, projeto);
            
            
            res.setPid(getPid(processo));
            
            int retorno = processo.waitFor();
            if (retorno != 0) {
                InputStream err = processo.getErrorStream();
                StringBuilder erro = new StringBuilder();
                int n;
                byte[] buffer = new byte[4096];
                while ((n = err.read(buffer)) > 0) {
                    String s = new String(buffer, 0, n);
                    erro.append(s);
                }
                res.setErro("Erro ao executar: " + erro);
                System.err.println("Erro ao executar.\n" + erro.toString());
            } else {
                long difTempo = System.currentTimeMillis() - tempo;
                res.setTempo(difTempo);
                Logger.getLogger(JAnalisadorLinter.class.getName()).log(Level.INFO, "Execu\u00e7\u00e3o finalizada em {0}ms.", difTempo);
            }
        }
    }
    
    private int getPid(Process processo){
        try {
            Field[] fields = processo.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("pid")) {
                    field.setAccessible(true);
                    return field.getInt(processo);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(BateriaExecucoesSonar.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private String[] montarComando(File projeto, String destino, Perfil perfil, Integer nroTeste) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm");
        String strDate = sdf.format(new Date());

        String[] comando = new String[]{
            "sonarlint",
            "-u",
            "--html-report",
            destino + "_sonar_" + projeto.getName() + perfil.getKke() + "_" + strDate + "__" + nroTeste + ".html"
        };
        return comando;

    }

    @Override
    public String getTextoCsv() {
        StringBuilder sb = new StringBuilder(8192);
        sb.append("Nº;Projeto;Perfil;PID;Tempo\n");
        for (Map.Entry<ParametrosSonar, ArrayList<ResultadosSonar>> entrySet : resultados.entrySet()) {
            ParametrosSonar param = entrySet.getKey();
            ArrayList<ResultadosSonar> tempos = entrySet.getValue();
            for (int i = 0; i < tempos.size(); i++) {
                ResultadosSonar res = tempos.get(i);
                sb.append(i).append(';').append(param.getProjeto()).append(';');
                sb.append(param.getPerfil().getNome()).append(';').append(res.getPid()).append(';');
                if (res.getErro() != null) {
                    sb.append("-");
                } else {
                    sb.append(res.getTempo()).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
