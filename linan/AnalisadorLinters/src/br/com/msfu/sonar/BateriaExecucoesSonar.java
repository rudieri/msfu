package br.com.msfu.sonar;

import br.com.msfu.gui.JAnalisadorLinter;
import br.com.msfu.main.AnalizadorTestes;
import br.com.msfu.utils.FormatadorCsv;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
    private final List<Integer> nucleosUsados = Collections.synchronizedList(new ArrayList<Integer>());

    public void agendarExecucao(ParametrosSonar param) {
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

    public void iniciar(boolean paralelizar) throws IOException, FileNotFoundException, SQLException, InterruptedException {
        resultados.clear();
        nucleosUsados.clear();
        if (paralelizar) {
            iniciarParalelo();
        } else {
            iniciarSequencial();
        }
    }

    private void iniciarSequencial() throws IOException, FileNotFoundException, SQLException, InterruptedException {
        for (ParametrosSonar param : execucoesAgendadas) {
            executar(param);
            System.out.println("--------------------------------");
            System.out.println("------------PREVIA--------------");
            System.out.println(getTextoCsv());
            System.out.println("--------------------------------");
            System.out.println("--------------------------------");
        }
    }

    private void iniciarParalelo() throws IOException, FileNotFoundException, SQLException, InterruptedException {
        final Iterator<ParametrosSonar> iterator = execucoesAgendadas.iterator();
        ParametrosSonar inicial = iterator.next();
        
        
        final List<Thread> lista = Collections.synchronizedList(new ArrayList<Thread>());
        ExecucaoCallback callback = new ExecucaoCallback() {
            
            @Override
            public void perfilAplicado() {
                if (iterator.hasNext()) {
                    ParametrosSonar next = iterator.next();
                    Thread thread = executarParalelo(next, this);
                    lista.add(thread);
                }
            }
        };
        executar(inicial, callback);
        while (!lista.isEmpty() || iterator.hasNext()) {
            for (int i = lista.size() - 1; i >= 0; i--) {
                Thread thread = lista.get(i);
                if (!thread.isAlive()) {
                    lista.remove(i);
                }
            }
        }
//        for (ParametrosSonar param : execucoesAgendadas) {
//            executar(param);
//        System.out.println("--------------------------------");
//        System.out.println("------------PREVIA--------------");
//        System.out.println(getTextoCsv());
//        System.out.println("--------------------------------");
//        System.out.println("--------------------------------");
//        }
    }

    private void executar(ParametrosSonar param) throws FileNotFoundException, IOException, SQLException, InterruptedException {
        executar(param, null);
    }

    private Thread executarParalelo(final ParametrosSonar param, final ExecucaoCallback callback) {
        Thread thread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                try {
                    executar(param, callback);
                } catch (IOException ex) {
                    Logger.getLogger(BateriaExecucoesSonar.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Logger.getLogger(BateriaExecucoesSonar.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BateriaExecucoesSonar.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, "Execuçao em paralelo: " + param.getProjeto());
        thread.start();
        return thread;
    }
    private void executar(ParametrosSonar param, ExecucaoCallback callback) throws FileNotFoundException, IOException, SQLException, InterruptedException {
        File projeto = new File(param.getProjeto());
        Properties sonarProject = new Properties();
        try (FileInputStream in = new FileInputStream(new File(projeto, "sonar-project.properties"));) {
            sonarProject.load(in);
        }

        SonarUtils.definirPerfil(sonarProject.getProperty("sonar.projectKey"), param.getPerfil());

        for (int i = 0; i < param.getNroTestes(); i++) {
            String[] comando = montarComando(projeto, param.getDestinoHtml(), param.getPerfil(), param.getNucleos(), i);

            long tempo = System.currentTimeMillis();
            ArrayList<ResultadosSonar> lista = resultados.get(param);
            if (lista == null) {
                lista = new ArrayList<>(param.getNroTestes());
                resultados.put(param, lista);
            }
            ResultadosSonar res = new ResultadosSonar();
            lista.add(res);
            imprimirComando(comando);
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.directory(projeto);
            pb.redirectErrorStream(true);
            Process processo = pb.start();
            try (InputStreamReader reader = new InputStreamReader(processo.getInputStream());
                    BufferedReader br = new BufferedReader(reader);) {
                String linha;
                while ((linha = br.readLine()) != null) {
                    if (callback != null && i == 0) {
                        if (linha.contains("Binding updated")) {
                            callback.perfilAplicado();
                        }
                    }
                }
            }
//            try (InputStream in = processo.getInputStream()) {
//                byte[] buffer = new byte[1024];
//                while (in.read(buffer) < 0);
//            }
//            Process processo = Runtime.getRuntime().exec(comando, null, projeto);
//            Process processo = Runtime.getRuntime().exec(comando, null, projeto);

            res.setPid(getPid(processo));

            int retorno = processo.waitFor();
            
            for (int j = 0; j < comando.length; j++) {
                String arg = comando[j];
                if (arg.equals("-c")) {
                    String[] cores = comando[j + 1].split(",");
                    for (String c : cores) {
                        nucleosUsados.remove(nucleosUsados.indexOf(Integer.valueOf(c)));
                    }
                }
            }
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

    private int getPid(Process processo) {
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

    private String[] montarComando(File projeto, String destino, Perfil perfil, Integer nucleos, Integer nroTeste) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm");
        String strDate = sdf.format(new Date());
        StringBuilder cores= null;
        String[] comando;
        if (nucleos > 0) {
            cores = new StringBuilder(nucleos * 2);
            int processors = Runtime.getRuntime().availableProcessors();
            int count = 0;
            for (int i = 0; i < processors; i++) {
                if (!nucleosUsados.contains(i)) {
                    count++;
                    cores.append(i).append(',');
                    nucleosUsados.add(i);
                    if (count == nucleos) {
                        break;
                    }
                }
            }
            cores.delete(cores.length() - 1, cores.length());
            comando = new String[]{
                "taskset",
                "-c",
                cores.toString(),
                AnalizadorTestes.getOpcoes().getProperty("sonar_cmd", "sonarlint"),
                "-u",
                "--html-report",
                destino + "_sonar_" + projeto.getName() + perfil.getKke() + "_" + strDate + "__" + nroTeste + ".html"
            };
        } else {
            comando = new String[]{
                AnalizadorTestes.getOpcoes().getProperty("sonar_cmd", "sonarlint"),
                "-u",
                "--html-report",
                destino + "_sonar_" + projeto.getName() + perfil.getKke() + "_" + strDate + "__" + nroTeste + ".html"
            };
        }
        return comando;

    }

    @Override
    public String getTextoCsv() {
        StringBuilder sb = new StringBuilder(8192);
        sb.append("Nº;Projeto;Perfil;Núcleos;PID;Tempo\n");
        for (Map.Entry<ParametrosSonar, ArrayList<ResultadosSonar>> entrySet : resultados.entrySet()) {
            ParametrosSonar param = entrySet.getKey();
            ArrayList<ResultadosSonar> tempos = entrySet.getValue();
            for (int i = 0; i < tempos.size(); i++) {
                ResultadosSonar res = tempos.get(i);
                sb.append(i).append(';').append(param.getProjeto()).append(';');
                sb.append(param.getPerfil().getNome()).append(';').append(param.getNucleos()).append(';');
                sb.append(res.getPid()).append(';');
                if (res.getErro() != null) {
                    sb.append("-");
                } else {
                    sb.append(res.getTempo()).append("\n");
                }
            }
        }
        return sb.toString();
    }
    
    private void imprimirComando(String[] comando){
        StringBuilder cmdTxt = new StringBuilder();
        for (String arg : comando) {
            cmdTxt.append(arg).append(' ');
        }
        System.out.println("Comando: " + cmdTxt);

    }

    private interface ExecucaoCallback {
        
        public void perfilAplicado();
    }
}
