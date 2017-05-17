/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.msfu.gui;

import br.com.msfu.constantes.Analisadores;
import br.com.msfu.sonar.ParametrosSonar;
import br.com.msfu.sonar.Perfil;
import br.com.msfu.sonar.SonarUtils;
import br.com.msfu.utils.textfield.CrepzBuscador;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author rudieri
 */
public class JAnalisadorLinter extends javax.swing.JDialog {

       private static final ArrayList<String> LISTA_VAZIA = new ArrayList<>(0);
    private ParametrosSonar parametros;

    /**
     * Creates new form JAnalizadorLinters
     */
    public JAnalisadorLinter(Window parent, ModalityType modal, boolean modoAgendamento) {
        super(parent, modal);
        init(modoAgendamento);
    }

    private void init(boolean modoAgendamento) {
        initComponents();
        jButton_Retornar.setVisible(modoAgendamento);
        jButton_Excutar.setVisible(!modoAgendamento);
        jComboBox1.removeAllItems();
        Analisadores[] analisadores = Analisadores.values();
        for (int i = 0; i < analisadores.length; i++) {
            Analisadores value = analisadores[i];
            jComboBox1.addItem(value);
        }
        carregarPerfil();

        jCrepzTextField1.setCrepzBuscador(new CrepzBuscador() {

            @Override
            public ArrayList<String> pesquisar(Object source, final String texto) {
                final int idx = texto.lastIndexOf("/");
                if (idx == -1 || idx == texto.length()) {
                    return LISTA_VAZIA;
                } else {
                    String path = texto.substring(0, idx + 1);
                    File file = new File(path);
                    if (file.exists()) {
                        File[] files = file.listFiles(new FilenameFilter() {

                            @Override
                            public boolean accept(File file, String string) {
                                String nome = texto.substring(idx + 1, texto.length());
                                return string.startsWith(nome);
                            }
                        });
                        ArrayList<String> lista = new ArrayList<>(files.length);
                        for (File f : files) {
                            lista.add(f.getAbsolutePath());
                        }
                        return lista;
                    } else {
                        return LISTA_VAZIA;
                    }
                }
            }
        });

    }

    private void executarAnalise() {
        try {
            File projeto = new File(jCrepzTextField1.getText());
            Properties sonarProject = new Properties();
            try(FileInputStream in = new FileInputStream(new File(projeto, "sonar-project.properties"));){
                sonarProject.load(in);
            }
            
            SonarUtils.definirPerfil(sonarProject.getProperty("sonar.projectKey"), (Perfil) jComboBox_Perfil.getSelectedItem());
            
            
            String[] comando = montarComando((Analisadores) jComboBox1.getSelectedItem(), projeto);
            long tempo = System.currentTimeMillis();
            Process processo = Runtime.getRuntime().exec(comando, null, projeto);
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
                
                JOptionPane.showMessageDialog(this, "Erro ao executar.\n"+erro.toString());
            } else {
                long difTempo = System.currentTimeMillis() - tempo;
                JOptionPane.showMessageDialog(this, "Execução finalizada em " + difTempo + "ms.");
                Logger.getLogger(JAnalisadorLinter.class.getName()).log(Level.INFO, "Execu\u00e7\u00e3o finalizada em {0}ms.", difTempo);
            }
        } catch (IOException | InterruptedException | SQLException ex) {
            Logger.getLogger(JAnalisadorLinter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void carregarPerfil() {
        try {
            jComboBox_Perfil.removeAllItems();
            ArrayList<Perfil> lista = SonarUtils.listarPerfis();
            for (Perfil perfil : lista) {
                jComboBox_Perfil.addItem(perfil);
            }
        } catch (SQLException ex) {
            Logger.getLogger(JAnalisadorLinter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String[] montarComando(Analisadores analizador, File projeto) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmm");
        String strDate = sdf.format(new Date());
        switch (analizador) {
            case SONARLINT:

                String[] comando = new String[]{
                    "sonarlint",
                    "-u",
                    "--html-report",
                    jTextField_DestinoRelatorio.getText() + "_sonar_" + projeto.getName() + "_" + strDate + ".html"
                };
                return comando;
            default:
                throw new AssertionError();
        }
    }
    
    private void retonarParametros(){
        parametros = new ParametrosSonar();
        parametros.setProjeto(jCrepzTextField1.getText());
        parametros.setNroTestes(Integer.valueOf(jTextField1.getText()));
        parametros.setPerfil((Perfil) jComboBox_Perfil.getSelectedItem());
        parametros.setDestinoHtml(jTextField_DestinoRelatorio.getText());
        dispose();
    }

    public ParametrosSonar getParametros() {
        return parametros;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<Analisadores>();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jCrepzTextField1 = new br.com.msfu.utils.textfield.JCrepzTextField();
        jPanel7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextField_DestinoRelatorio = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jComboBox_Perfil = new javax.swing.JComboBox<Perfil>();
        jPanel6 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jButton_Excutar = new javax.swing.JButton();
        jButton_Retornar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 3));

        jLabel1.setText("Analisador:");
        jLabel1.setPreferredSize(new java.awt.Dimension(95, 17));
        jPanel4.add(jLabel1);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SonarLint" }));
        jComboBox1.setPreferredSize(new java.awt.Dimension(150, 24));
        jPanel4.add(jComboBox1);

        jPanel2.add(jPanel4);

        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 3));

        jLabel2.setText("Projeto:");
        jLabel2.setPreferredSize(new java.awt.Dimension(95, 17));
        jPanel5.add(jLabel2);

        jCrepzTextField1.setPreferredSize(new java.awt.Dimension(500, 29));
        jPanel5.add(jCrepzTextField1);

        jPanel2.add(jPanel5);

        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 3));

        jLabel3.setText("Nº Testes:");
        jLabel3.setPreferredSize(new java.awt.Dimension(95, 17));
        jPanel7.add(jLabel3);

        jTextField1.setText("10");
        jTextField1.setPreferredSize(new java.awt.Dimension(70, 29));
        jPanel7.add(jTextField1);

        jPanel2.add(jPanel7);

        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 3));

        jLabel4.setText("Destino HTML:");
        jLabel4.setPreferredSize(new java.awt.Dimension(95, 17));
        jPanel8.add(jLabel4);

        jTextField_DestinoRelatorio.setText("/home/rudieri/ufsm/msfu/");
        jTextField_DestinoRelatorio.setPreferredSize(new java.awt.Dimension(500, 29));
        jPanel8.add(jTextField_DestinoRelatorio);

        jPanel2.add(jPanel8);

        jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 3));

        jLabel5.setText("Perfil:");
        jLabel5.setPreferredSize(new java.awt.Dimension(95, 17));
        jPanel10.add(jLabel5);

        jComboBox_Perfil.setPreferredSize(new java.awt.Dimension(150, 24));
        jPanel10.add(jComboBox_Perfil);

        jPanel2.add(jPanel10);

        jPanel6.setLayout(new java.awt.BorderLayout());
        jPanel2.add(jPanel6);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

        jButton_Excutar.setText("Executar");
        jButton_Excutar.setPreferredSize(new java.awt.Dimension(90, 29));
        jButton_Excutar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ExcutarActionPerformed(evt);
            }
        });
        jPanel9.add(jButton_Excutar);

        jButton_Retornar.setText("Retornar");
        jButton_Retornar.setPreferredSize(new java.awt.Dimension(90, 29));
        jButton_Retornar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_RetornarActionPerformed(evt);
            }
        });
        jPanel9.add(jButton_Retornar);

        jPanel3.add(jPanel9);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        setSize(new java.awt.Dimension(773, 366));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_ExcutarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ExcutarActionPerformed
        executarAnalise();
    }//GEN-LAST:event_jButton_ExcutarActionPerformed

    private void jButton_RetornarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_RetornarActionPerformed
        retonarParametros();
    }//GEN-LAST:event_jButton_RetornarActionPerformed

   /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JAnalisadorLinter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JAnalisadorLinter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JAnalisadorLinter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JAnalisadorLinter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JAnalisadorLinter(null, ModalityType.APPLICATION_MODAL, false).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Excutar;
    private javax.swing.JButton jButton_Retornar;
    private javax.swing.JComboBox<Analisadores> jComboBox1;
    private javax.swing.JComboBox<Perfil> jComboBox_Perfil;
    private br.com.msfu.utils.textfield.JCrepzTextField jCrepzTextField1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField_DestinoRelatorio;
    // End of variables declaration//GEN-END:variables
}
