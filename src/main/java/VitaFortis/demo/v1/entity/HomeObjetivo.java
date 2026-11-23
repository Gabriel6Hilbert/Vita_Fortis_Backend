package VitaFortis.demo.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
@Table(name="home_objetivo", uniqueConstraints=@UniqueConstraint(name="uk_home_objetivo_chave", columnNames="chave"))
public class HomeObjetivo {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="home_objetivo_id") private Long id;
    @Column(nullable=false,length=40) private String chave;
    @Column(nullable=false,length=80) private String titulo;
    @Column(name="imagem_url",length=500) private String imagemUrl;
    @Column(name="texto_alternativo",nullable=false,length=180) private String textoAlternativo;
    @Column(nullable=false) private int ordem;
    @Column(nullable=false) private boolean ativo=true;
}
