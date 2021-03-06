
package br.com.msfu.sonar;

import java.util.Objects;

/**
 *
 * @author rudieri
 */
public class ParametrosSonar {
    private String projeto;
    private String destinoHtml;
    private Integer nroTestes;
    private Perfil perfil;
    private Integer nucleos;

    public String getProjeto() {
        return projeto;
    }

    public void setProjeto(String projeto) {
        this.projeto = projeto;
    }

    public String getDestinoHtml() {
        return destinoHtml;
    }

    public void setDestinoHtml(String destinoHtml) {
        this.destinoHtml = destinoHtml;
    }

    public Integer getNroTestes() {
        return nroTestes;
    }

    public void setNroTestes(Integer nroTestes) {
        this.nroTestes = nroTestes;
    }
    public void setNucleos(Integer nucleos) {
        this.nucleos = nucleos;
    }
    
    public Integer getNucleos(){
        return nucleos;
    }

    public Perfil getPerfil() {
        return perfil;
    }


    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.projeto);
        hash = 31 * hash + Objects.hashCode(this.perfil);
        hash = 31 * hash + Objects.hashCode(this.nucleos);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParametrosSonar other = (ParametrosSonar) obj;
        if (!Objects.equals(this.projeto, other.projeto)) {
            return false;
        }
        if (!Objects.equals(this.perfil, other.perfil)) {
            return false;
        }
        if (!Objects.equals(this.nucleos, other.nucleos)) {
            return false;
        }
        return true;
    }

   
    
    
    
}
