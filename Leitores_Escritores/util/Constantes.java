/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 07/05/2026
* Ultima alteracao.: 08/05/2026
* Nome.............: Constantes.java
* Funcao...........: Constantes globais da aplicacao Leitores/Escritores
************************************************************************ */
package util;

/* ***************************************************************
* Classe: Constantes
* Funcao: Define as constantes utilizadas em toda a aplicacao.
*************************************************************** */
public class Constantes {

    public static final int NUM_LEITORES = 5;
    public static final int NUM_ESCRITORES = 5;

    public static final long VEL_PADRAO_LEITOR = 1500;
    public static final long VEL_PADRAO_ESCRITOR = 2000;
    public static final long VEL_MIN = 200;
    public static final long VEL_MAX = 4000;

    public static final String IMG_EDITOR_OCIOSO = "/img/editor_ocioso.png";
    public static final String IMG_EDITOR_LENDO = "/img/editor_lendo.png";
    public static final String IMG_EDITOR_AGUARDANDO = "/img/editor_aguardando.png";
    public static final String IMG_EDITOR_PAUSADO = "/img/editor_pausado.png";

    public static final String IMG_REPORTER_OCIOSO = "/img/reporter_ocioso.png";
    public static final String IMG_REPORTER_ESCREVENDO = "/img/reporter_escrevendo.png";
    public static final String IMG_REPORTER_AGUARDANDO = "/img/reporter_aguardando.png";
    public static final String IMG_REPORTER_PAUSADO = "/img/reporter_pausado.png";

    public static final String IMG_JORNAL_FECHADO = "/img/jornal_fechado.png";
    public static final String IMG_JORNAL_ABERTO = "/img/jornal_aberto.png";

    public static final String[] PAUTAS = {
        "Exclusivo: Descoberta cientifica revoluciona a medicina",
        "Politica: Novo projeto de lei aprovado por unanimidade",
        "Economia: Mercado registra alta historica na bolsa",
        "Esportes: Time local conquista campeonato estadual",
        "Cultura: Festival de musica atrai milhares ao centro",
        "Tecnologia: Startup brasileira capta investimento recorde",
        "Internacional: Cupula mundial discute mudancas climaticas",
        "Cidade: Obras de reforma transformam parque urbano",
        "Educacao: Nova politica amplia acesso a universidades",
        "Saude: Campanha de vacinacao supera meta nacional"
    };

    private Constantes() {}
    
} // Fim da classe Constantes
