package br.com.msfu.main;

import br.com.msfu.gui.JAgendadorTestes;
import br.com.msfu.sonar.BateriaExecucoesSonar;
import br.com.msfu.sonar.ExecucaoParalelaSonar;
import br.com.msfu.sonar.ParametrosSonar;
import br.com.msfu.sonar.Perfil;
import br.com.msfu.sonar.SonarUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rudieri
 */
public class AnalizadorTestes {
    private static Properties opcoes;

    @SuppressWarnings("AssignmentToForLoopParameter")
    public static void main(String[] args) {
        opcoes = new Properties();
        try {
            opcoes.load(new FileInputStream("opcoes.properties"));
            
        } catch (IOException ex) {
            Logger.getLogger(AnalizadorTestes.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (args.length > 0) {
            ArrayList<Object> tarefas = new ArrayList<>();
            String arquivoTeste = args[0];
            try (FileInputStream in = new FileInputStream(new File(arquivoTeste))) {
                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[4096];
                int n;
                while ((n = in.read(buffer)) > 0) {
                    sb.append(new String(buffer, 0, n));
                }
                String[] linhas = sb.toString().split("\n");
                // ignoramos o cabeçalho
                for (int i = 1; i < linhas.length; i++) {
                    String linha = linhas[i].trim();
                    if (linha.isEmpty()) {
                        continue;
                    }
                    if (linha.contains("paralelo{")) {
                        ExecucaoParalelaSonar execucaoParalelaSonar = new ExecucaoParalelaSonar();
                        for (int j = i + 1; j < linhas.length; j++) {
                            String linhaAux = linhas[j].trim();
                            if (linhaAux.equals("}")) {
                                i = j;
                                tarefas.add(execucaoParalelaSonar);
                                break;
                            }
                            execucaoParalelaSonar.adicionarParametro(criarParametros(linhaAux));
                        }
                    } else {
                        tarefas.add(criarParametros(linha));
                    }

                }
                for (int i = 0; i < tarefas.size(); i++) {
                    Object obj = tarefas.get(i);
                    BateriaExecucoesSonar bateriaExecucoesSonar = new BateriaExecucoesSonar();
                    if (obj instanceof ExecucaoParalelaSonar) {
                        ExecucaoParalelaSonar aux = (ExecucaoParalelaSonar) obj;
                        for (int j = 0; j < aux.getParametros().size(); j++) {
                            ParametrosSonar param = aux.getParametros().get(j);
                            bateriaExecucoesSonar.agendarExecucao(param);
                        }
                        bateriaExecucoesSonar.iniciar(true);
                        imprimirResultados(bateriaExecucoesSonar.getTextoCsv());

                    } else {
                        ParametrosSonar aux = (ParametrosSonar) obj;
                        bateriaExecucoesSonar.agendarExecucao(aux);
//                        if (i == tarefas.size() - 1 || tarefas.get(i + 1) instanceof ExecucaoParalelaSonar) {
                            bateriaExecucoesSonar.iniciar(true);
                            imprimirResultados(bateriaExecucoesSonar.getTextoCsv());
//                        }
                    }

                }
            } catch (IOException | SQLException | InterruptedException ex) {
                Logger.getLogger(AnalizadorTestes.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            JAgendadorTestes jAgendadorTestes = new JAgendadorTestes();
            jAgendadorTestes.setVisible(true);
        }
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public static Properties getOpcoes() {
        return opcoes;
    }

    private static void imprimirResultados(String textoCsv) {
        System.out.println("--------------------------------");
        System.out.println("-------RESULTADOS PARCIAIS------");
        System.out.println(textoCsv);
        System.out.println("--------------------------------");
        System.out.println("--------------------------------");
    }

    private static ParametrosSonar criarParametros(String linha) throws SQLException {
        String[] colunas = linha.split(";");
        ParametrosSonar param = new ParametrosSonar();
        param.setProjeto(colunas[0]);
        ArrayList<Perfil> listarPerfis = SonarUtils.listarPerfis(colunas[1]);
        if (listarPerfis.isEmpty()) {
            throw new IllegalStateException("Perfil " + colunas[1] + " não encontrado.");
        }
        param.setPerfil(listarPerfis.get(0));
        param.setNroTestes(Integer.valueOf(colunas[2]));
        param.setNucleos(Integer.valueOf(colunas[3]));
        param.setDestinoHtml(colunas[4]);
        return param;
    }
}
