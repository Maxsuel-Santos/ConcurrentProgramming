/* ***************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 25/06/2026
* Ultima alteracao.: 12/07/2026
* Nome.............: Constantes.java
* Funcao...........: Centraliza as constantes e os mapas usados pela simulacao.
************************************************************************ */
package util;

import java.util.LinkedHashMap;
import java.util.Map;

/* ***************************************************************
* Classe: Constantes
* Funcao: Centraliza as constantes e os mapas usados pela simulacao.
*************************************************************** */
public final class Constantes {

    public static final int TAMANHO_MALHA = 5;
    public static final int N_VERTICES = 6;
    public static final int N_CARROS = 8;

    public static final String SENTIDO_HORARIO = "SH";
    public static final String SENTIDO_ANTI_HORARIO = "SA";

    public static final String[] NOMES_PERCURSOS = {
        "P05", "P03", "P07", "P11", "P16", "P18", "P20", "P23"
    };

    public static final String[] SENTIDOS = {
        SENTIDO_ANTI_HORARIO,
        SENTIDO_ANTI_HORARIO,
        SENTIDO_HORARIO,
        SENTIDO_ANTI_HORARIO,
        SENTIDO_ANTI_HORARIO,
        SENTIDO_HORARIO,
        SENTIDO_HORARIO,
        SENTIDO_ANTI_HORARIO
    };

    // Todos os carros nascem no meio de trechos diferentes
    public static final int[] INDICES_INICIAIS = {
        6,  // C1 em RH27
        13, // C2 em RH25
        10, // C3 em RH12
        3,  // C4 em RV02
        3,  // C5 em RH18
        9,  // C6 em RV11
        1,  // C7 em RV20
        3   // C8 em RV08
    };

    public static final String[] CARRO_1_TRECHOS = {
        "RV05", "RV04", "RV03", "RV02", "RV01",
        "RH26", "RH27", "RH28", "RH29", "RH30",
        "RV26", "RV27", "RV28", "RV29", "RV30",
        "RH05", "RH04", "RH03", "RH02", "RH01"
    };

    public static final String[] CARRO_2_TRECHOS = {
        "RV05", "RV04", "RV03", "RV02", "RV01",
        "RH26", "RH27", "RV11", "RH23", "RV16",
        "RH29", "RH30", "RV26", "RH25", "RV22",
        "RH19", "RV18", "RH14", "RH15", "RV29",
        "RV30", "RH05", "RH04", "RH03", "RH02", "RH01"
    };

    public static final String[] CARRO_3_TRECHOS = {
        "RH01", "RH02", "RH03", "RH04", "RH05",
        "RV30", "RV29", "RH15", "RH14", "RH13",
        "RH12", "RH11", "RV04", "RV05"
    };

    public static final String[] CARRO_4_TRECHOS = {
        "RV05", "RV04", "RV03", "RV02", "RV01",
        "RH26", "RH27", "RV11", "RV12", "RV13",
        "RV14", "RV15", "RH02", "RH01"
    };

    public static final String[] CARRO_5_TRECHOS = {
        "RV15", "RV14", "RV13", "RH18", "RH19", "RH20",
        "RV28", "RV29", "RV30", "RH05", "RH04", "RH03"
    };

    public static final String[] CARRO_6_TRECHOS = {
        "RH13", "RH14", "RH15", "RV28", "RV27", "RV26",
        "RH30", "RH29", "RH28", "RV11", "RV12", "RV13"
    };


    public static final String[] CARRO_7_TRECHOS = {
        "RH03", "RV20", "RH09", "RV24", "RH15",
        "RV28", "RH20", "RV22", "RH24", "RV16",
        "RH28", "RV11", "RH22", "RV07", "RH16",
        "RV03", "RH11", "RV09", "RH07", "RV15"
    };


    public static final String[] CARRO_8_TRECHOS = {
        "RV05", "RV04", "RH11", "RV08", "RH17",
        "RV12", "RH23", "RV16", "RH29", "RH30",
        "RV26", "RV27", "RH20", "RV23", "RH14",
        "RV19", "RH08", "RV15", "RH02", "RH01"
    };

    public static final String[][] TRECHOS_DOS_CARROS = {
        CARRO_1_TRECHOS,
        CARRO_2_TRECHOS,
        CARRO_3_TRECHOS,
        CARRO_4_TRECHOS,
        CARRO_5_TRECHOS,
        CARRO_6_TRECHOS,
        CARRO_7_TRECHOS,
        CARRO_8_TRECHOS
    };

    public static final long PASSO_BASE_MS = 650L;
    public static final double VELOCIDADE_MIN = 0.25;
    public static final double VELOCIDADE_MAX = 3.0;
    public static final double VELOCIDADE_PADRAO = 1.0;

    // Posicionamento da malha logica sobre a imagem de fundo.
    // TAMANHO_QUADRA_PX altera a distancia entre os cruzamentos.
    public static final double ORIGEM_GRID_X = 10.0;
    public static final double ORIGEM_GRID_Y = 10.0;
    public static final double TAMANHO_QUADRA_PX = 140.0;

    
    // Quanto o ponto de parada fica a frente do meio do trecho, sempre no sentido em que o carro esta andando.
    public static final double AVANCO_PONTO_PARADA_PX = 10.0;

    public static final double[] AJUSTE_VISUAL_CARRO_X = {
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
    };
    public static final double[] AJUSTE_VISUAL_CARRO_Y = {
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
    };

    public static final double LARGURA_CARRO_PX = 40.0;
    public static final double ALTURA_CARRO_PX = 40.0;

    public static final String CAMINHO_IMG = "/img/";

    /* ***************************************************************
    * Metodo: montarMapaArestas
    * Funcao: Monta o mapa completo das arestas da malha.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public static Map<String, int[][]> montarMapaArestas() {
        Map<String, int[][]> mapa = new LinkedHashMap<>();

        for (int linha = 0; linha < N_VERTICES; linha++) {
            for (int k = 1; k <= TAMANHO_MALHA; k++) {
                int numero = linha * TAMANHO_MALHA + k;
                String nome = String.format("RH%02d", numero);
                mapa.put(nome, new int[][] {
                    {linha, k - 1},
                    {linha, k}
                });
            }
        }

        for (int coluna = 0; coluna < N_VERTICES; coluna++) {
            for (int k = 1; k <= TAMANHO_MALHA; k++) {
                int numero = coluna * TAMANHO_MALHA + k;
                String nome = String.format("RV%02d", numero);
                int linhaBaixo = N_VERTICES - k;
                int linhaCima = N_VERTICES - k - 1;
                mapa.put(nome, new int[][] {
                    {linhaBaixo, coluna},
                    {linhaCima, coluna}
                });
            }
        }

        return mapa;
    }

    /* ***************************************************************
    * Metodo: Constantes
    * Funcao: Inicializa uma nova instancia de Constantes.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private Constantes() {
    }
}
