package br.com.msfu.sonar;

import br.com.msfu.h2.H2BD;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author rudieri
 */
public class SonarUtils {

    public static ArrayList<Perfil> listarPerfis() throws SQLException {
        try (Connection conn = H2BD.getConnection()) {
            Statement st = conn.createStatement();

            ArrayList<Perfil> lista = new ArrayList<>();
            try (ResultSet rs = st.executeQuery("select kee, name from RULES_PROFILES where language = 'java'")) {
                while (rs.next()) {
                    Perfil perfil = new Perfil(rs.getString("kee"));
                    perfil.setNome(rs.getString("name"));
                    lista.add(perfil);
                }
            }
            return lista;
        } 
    }
    
    public static void definirPerfil(String chaveProjeto, Perfil perfil) throws SQLException{
        try (Connection conn = H2BD.getConnection()) {
            Statement st = conn.createStatement();
            String projectUUID;
            try (ResultSet rs = st.executeQuery("select uuid from PROJECTS where kee = '" + chaveProjeto + "'")) {
                if (rs.next()) {
                    projectUUID = rs.getString("uuid");
                } else {
                    throw new IllegalStateException("Erro ao atualizar perfil, projeto não localizado.");
                }
            }
            
            try (ResultSet rs = st.executeQuery("select id from PROJECT_QPROFILES where project_uuid = '" + projectUUID + "'")) {
                if (rs.next()) {
                    int r = st.executeUpdate("update PROJECT_QPROFILES set profile_key = '" + perfil.getKke() + "'" 
                            + " where id = " + rs.getString("id"));
                    if (r == 0) {
                        throw new IllegalStateException("Erro ao atualizar perfil, nenhuma linha afetada.");
                    }
                } else {
                    st.executeUpdate("insert into PROJECT_QPROFILES values (next value for public.SYSTEM_SEQUENCE_BD0326A0_F944_4325_8A10_3BA3299BC852, '" + projectUUID + "', " + "'" + perfil.getKke() + "')");
                }
            }
        } 
    }

}
