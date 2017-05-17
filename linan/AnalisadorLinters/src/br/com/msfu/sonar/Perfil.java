
package br.com.msfu.sonar;

/**
 *
 * @author rudieri
 */
public class Perfil {
    private String nome;
    private String kke;

    public Perfil(String kke) {
        this.kke = kke;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getKke() {
        return kke;
    }

    public void setKke(String kke) {
        this.kke = kke;
    }

    @Override
    public String toString() {
        return nome;
    }
    
}
