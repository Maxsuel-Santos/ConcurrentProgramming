/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 21/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: Constantes.java
* Funcao...........: Centraliza todas as constantes do problema: a malha
*                    de ruas (6x6 vertices), os 8 percursos sorteados
*                    (cada um como uma sequencia de trechos RHxx/RVxx,
*                    JA' na ordem real de deslocamento) e as ZONAS
*                    CRITICAS (regioes formadas por um ou mais trechos
*                    CONSECUTIVOS, protegidas por um unico semaforo).
*
*                    Numeracao da malha (ver imagem backbone.png):
*                    - RH (Rua Horizontal): RH01..RH30. A rua RHxx liga o
*                      vertice (linha, coluna-1) ao vertice (linha, coluna)
*                      dentro da mesma linha de vertices. As ruas estao
*                      numeradas em blocos de 5 por linha, de cima (linha 0)
*                      para baixo (linha 5).
*                    - RV (Rua Vertical): RV01..RV30. A rua RVxx liga o
*                      vertice da linha mais baixa ao vertice da linha
*                      imediatamente acima, dentro da mesma coluna. As
*                      ruas estao numeradas em blocos de 5 por coluna,
*                      de baixo (linha 5) para cima (linha 0), e os
*                      blocos seguem da coluna 0 (esquerda) para a
*                      coluna 5 (direita).
*
*                    HISTORICO - MUDANCA DE GRANULARIDADE DAS REGIOES
*                    CRITICAS: a primeira versao usava 1 semaforo por
*                    TRECHO individual (RHxx/RVxx). Isso causava deadlock
*                    com 3+ carros se cruzando: cada thread fazia varias
*                    trocas rapidas de semaforo (release/acquire) dentro
*                    de uma mesma sequencia longa compartilhada, e o
*                    entrelacamento de 3 threads trocando semaforos finos
*                    nos mesmos pontos formava esperas circulares.
*
*                    SOLUCAO ATUAL: as regioes criticas seguem
*                    diretamente o arquivo
*                    regioes_criticas_transito_automato.txt: 57 regioes
*                    por PAR de carros, cada uma com seu proprio
*                    Semaphore(1). Cada carro adquire o semaforo da RC
*                    ao entrar no primeiro trecho daquela regiao, e
*                    libera ao concluir o ultimo trecho dela.
************************************************************************ */

package util;

import java.util.LinkedHashMap;
import java.util.Map;

public class Constantes {

    // -----------------------------------------------------------------
    // Tamanho da malha (em quadras). Uma malha 5x5 de quadras possui
    // 6x6 vertices (cruzamentos).
    // -----------------------------------------------------------------
    public static final int TAMANHO_MALHA = 5;
    public static final int N_VERTICES = TAMANHO_MALHA + 1; // 6

    // -----------------------------------------------------------------
    // Quantidade de carros/percursos do trabalho (fixo em 8, conforme
    // especificacao do problema).
    // -----------------------------------------------------------------
    public static final int N_CARROS = 8;

    // -----------------------------------------------------------------
    // Sentido de percurso de cada carro
    // -----------------------------------------------------------------
    public static final String SENTIDO_HORARIO = "SH";
    public static final String SENTIDO_ANTI_HORARIO = "SA";

    // -----------------------------------------------------------------
    // Percurso (nome da imagem do problema, sem prefixo/sufixo) e
    // sentido de cada um dos 8 carros sorteados para a matricula
    // 202511587.
    // -----------------------------------------------------------------
    public static final String[] CARRO_PERCURSO_NOME = {
        "P05", "P03", "P07", "P11", "P16", "P18", "P20", "P23"
    };

    public static final String[] CARRO_SENTIDO = {
        SENTIDO_ANTI_HORARIO, // Carro 1 - P05_SA
        SENTIDO_ANTI_HORARIO, // Carro 2 - P03_SA
        SENTIDO_HORARIO,      // Carro 3 - P07_SH
        SENTIDO_ANTI_HORARIO, // Carro 4 - P11_SA
        SENTIDO_ANTI_HORARIO, // Carro 5 - P16_SA
        SENTIDO_HORARIO,      // Carro 6 - P18_SH
        SENTIDO_HORARIO,      // Carro 7 - P20_SH
        SENTIDO_ANTI_HORARIO  // Carro 8 - P23_SA
    };

    // -----------------------------------------------------------------
    // Indice (dentro da lista CARRO_x_TRECHOS de cada carro) onde o
    // ciclo do carro comeca a ser percorrido. 0 = comeca no primeiro
    // trecho da lista (padrao). Usado quando o discente decide
    // posicionar o carro "nascendo" no meio do proprio percurso, em
    // vez de no primeiro trecho listado.
    //
    // Carro 2 (P03_SA): comeca no trecho RV18 (indice 16 da lista
    // CARRO_2_TRECHOS), conforme posicionamento definido para a tela.
    // Carro 3 (P07_SH): comeca no trecho RH12 (indice 10 da lista
    // CARRO_3_TRECHOS), conforme posicionamento definido para a tela.
    // -----------------------------------------------------------------
    public static final int[] CARRO_INDICE_CICLO_INICIAL = {
        0,  // Carro 1
        16, // Carro 2 - comeca em RV18
        10, // Carro 3 - comeca em RH12
        0,  // Carro 4
        0,  // Carro 5
        0,  // Carro 6
        0,  // Carro 7
        0   // Carro 8
    };

    // -----------------------------------------------------------------
    // Sequencia de trechos (arestas RHxx/RVxx) que compoem o ciclo de
    // cada percurso, JA' na ordem real de deslocamento do carro (o
    // sentido SA/SH informado em CARRO_SENTIDO e' apenas descritivo;
    // a ordem abaixo e' sempre percorrida de forma direta, sem
    // inversao - ver model.Percurso). Validado: todos os 8 ciclos
    // fecham corretamente e a orientacao geometrica de cada lista
    // bate com o sentido declarado.
    // -----------------------------------------------------------------
    public static final String[] CARRO_1_TRECHOS = {
        "RV05","RV04","RV03","RV02","RV01",
        "RH26","RH27","RH28","RH29","RH30",
        "RV26","RV27","RV28","RV29","RV30",
        "RH05","RH04","RH03","RH02","RH01"
    };

    public static final String[] CARRO_2_TRECHOS = {
        "RV05","RV04","RV03","RV02","RV01",
        "RH26","RH27","RV11","RH23","RV16",
        "RH29","RH30","RV26","RH25","RV22",
        "RH19","RV18","RH14","RH15","RV29",
        "RV30","RH05","RH04","RH03","RH02","RH01"
    };

    public static final String[] CARRO_3_TRECHOS = {
        "RH01","RH02","RH03","RH04","RH05",
        "RV30","RV29","RH15","RH14","RH13",
        "RH12","RH11","RV04","RV05"
    };

    public static final String[] CARRO_4_TRECHOS = {
        "RV05","RV04","RV03","RV02","RV01",
        "RH26","RH27","RV11","RV12","RV13",
        "RV14","RV15","RH02","RH01"
    };

    public static final String[] CARRO_5_TRECHOS = {
        "RV15","RV14","RV13","RH18","RH19",
        "RH20","RV28","RV29","RV30","RH05",
        "RH04","RH03"
    };

    public static final String[] CARRO_6_TRECHOS = {
        "RH13","RH14","RH15","RV28","RV27",
        "RV26","RH30","RH29","RH28","RV11",
        "RV12","RV13"
    };

    public static final String[] CARRO_7_TRECHOS = {
        "RH03","RV20","RH09","RV24","RH15",
        "RV28","RH20","RV22","RH24","RV16",
        "RH28","RV11","RH22","RV07","RH16",
        "RV03","RH11","RV09","RH07","RV15"
    };

    public static final String[] CARRO_8_TRECHOS = {
        "RV05","RV04","RH11","RV08","RH17",
        "RV12","RH23","RV16","RH29","RH30",
        "RV26","RV27","RH20","RV23","RH14",
        "RV19","RH08","RV15","RH02","RH01"
    };

    // Indexado por (numero do carro - 1)
    public static final String[][] CARRO_TRECHOS = {
        CARRO_1_TRECHOS, CARRO_2_TRECHOS, CARRO_3_TRECHOS, CARRO_4_TRECHOS,
        CARRO_5_TRECHOS, CARRO_6_TRECHOS, CARRO_7_TRECHOS, CARRO_8_TRECHOS
    };

    // ===================================================================
    // REGIOES CRITICAS (regioes protegidas por semaforo)
    // ===================================================================
    // Cada entrada abaixo corresponde a uma linha RC_xx do arquivo
    // regioes_criticas_transito_automato.txt.
    // RC_35 foi ajustada para RH02, RH01, RV05, RV04, RH11: no TXT ha'
    // "RH02, RH02", mas essa repeticao nao existe como sequencia em
    // nenhum dos dois percursos; a forma corrigida casa com os carros
    // 3 e 8 e preserva a regiao descrita.
    public static final String[] NOMES_REGIOES_CRITICAS = {
        "RC_01", "RC_02", "RC_03", "RC_04", "RC_05", "RC_06", "RC_07", "RC_08", "RC_09", "RC_10",
        "RC_11", "RC_12", "RC_13", "RC_14", "RC_15", "RC_16", "RC_17", "RC_18", "RC_19", "RC_20",
        "RC_21", "RC_22", "RC_23", "RC_24", "RC_25", "RC_26", "RC_27", "RC_28", "RC_29", "RC_30",
        "RC_31", "RC_32", "RC_33", "RC_34", "RC_35", "RC_36", "RC_37", "RC_38", "RC_39", "RC_40",
        "RC_41", "RC_42", "RC_43", "RC_44", "RC_45", "RC_46", "RC_47", "RC_48", "RC_49", "RC_50",
        "RC_51", "RC_52", "RC_53", "RC_54", "RC_55", "RC_56", "RC_57"
    };

    public static final int[][] CARROS_REGIOES_CRITICAS = {
        {1, 2}, {1, 2}, {1, 3}, {1, 4}, {1, 5}, {1, 6}, {1, 7}, {1, 7}, {1, 7}, {1, 7},
        {1, 8}, {1, 8}, {2, 3}, {2, 4}, {2, 5}, {2, 5}, {2, 6}, {2, 6}, {2, 6}, {2, 7},
        {2, 7}, {2, 7}, {2, 7}, {2, 7}, {2, 7}, {2, 8}, {2, 8}, {2, 8}, {3, 4}, {3, 5},
        {3, 6}, {3, 7}, {3, 7}, {3, 7}, {3, 8}, {3, 8}, {4, 5}, {4, 6}, {4, 7}, {4, 7},
        {4, 7}, {4, 8}, {4, 8}, {5, 6}, {5, 6}, {5, 7}, {5, 7}, {5, 8}, {5, 8}, {6, 7},
        {6, 7}, {6, 8}, {6, 8}, {6, 8}, {7, 8}, {7, 8}, {7, 8}
    };

    public static final String[][] TRECHOS_REGIOES_CRITICAS = {
        {"RV29","RV30","RH05","RH04","RH03","RH02","RH01","RV05","RV04","RV03","RV02","RV01","RH26","RH27"},
        {"RH29","RH30","RV26"},
        {"RV04","RV05","RH01","RH02","RH03","RH04","RH05","RV30","RV29"},
        {"RH02","RH01","RV05","RV04","RV03","RV02","RV01","RH26","RH27"},
        {"RV28","RV29","RV30","RH05","RH04","RH03"},
        {"RV28","RV27","RV26","RH30","RH29","RH28"},
        {"RH03"},
        {"RV28"},
        {"RH28"},
        {"RV03"},
        {"RH02","RH01","RV05","RV04"},
        {"RH29","RH30","RV26","RV27"},
        {"RV04","RV05","RH01","RH02","RH03","RH04","RH05","RV30","RV29","RH15","RH14"},
        {"RH02","RH01","RV05","RV04","RV03","RV02","RV01","RH26","RH27","RV11"},
        {"RH19"},
        {"RV29","RV30","RH05","RH04","RH03"},
        {"RH14","RH15"},
        {"RV26","RH30","RH29"},
        {"RV11"},
        {"RH03"},
        {"RH15"},
        {"RV22"},
        {"RV16"},
        {"RV11"},
        {"RV03"},
        {"RH02","RH01","RV05","RV04"},
        {"RH23","RV16","RH29","RH30","RV26"},
        {"RH14"},
        {"RH02","RH01","RV05","RV04"},
        {"RV29","RV30","RH05","RH04","RH03"},
        {"RH15","RH14","RH13"},
        {"RH03"},
        {"RH15"},
        {"RH11"},
        {"RH02","RH01","RV05","RV04","RH11"},
        {"RH14"},
        {"RV15","RV14","RV13"},
        {"RV11","RV12","RV13"},
        {"RV03"},
        {"RV11"},
        {"RV15"},
        {"RV15","RH02","RH01","RV05","RV04"},
        {"RV12"},
        {"RV13"},
        {"RV28"},
        {"RV15","RH03"},
        {"RV28","RH20"},
        {"RH20"},
        {"RV15"},
        {"RH15","RV28"},
        {"RH28","RV11"},
        {"RH14"},
        {"RV12"},
        {"RH29","RH30","RV26","RV27"},
        {"RH11"},
        {"RV16"},
        {"RH20"}
    };

    // -----------------------------------------------------------------
    // Mapa de coordenadas dos vertices de cada trecho, em (linha,coluna),
    // linha 0 = topo, linha 5 = base; coluna 0 = esquerda, coluna 5 =
    // direita. Usado pelo model.Grid para montar o grafo e calcular as
    // posicoes em pixel de cada cruzamento.
    // -----------------------------------------------------------------
    public static Map<String, int[][]> montarMapaArestas() {
        Map<String, int[][]> mapa = new LinkedHashMap<>();

        // Ruas horizontais: RH(linha*5 + coluna+1), liga (linha,coluna)-(linha,coluna+1)
        for (int linha = 0; linha < N_VERTICES; linha++) {
            for (int k = 1; k <= TAMANHO_MALHA; k++) {
                int num = linha * TAMANHO_MALHA + k;
                String nome = String.format("RH%02d", num);
                int colunaOrigem = k - 1;
                int colunaDestino = k;
                mapa.put(nome, new int[][] {
                    { linha, colunaOrigem },
                    { linha, colunaDestino }
                });
            }
        }

        // Ruas verticais: RV(coluna*5 + k), k=1 (base) .. 5 (topo)
        for (int coluna = 0; coluna < N_VERTICES; coluna++) {
            for (int k = 1; k <= TAMANHO_MALHA; k++) {
                int num = coluna * TAMANHO_MALHA + k;
                String nome = String.format("RV%02d", num);
                int linhaBaixo = N_VERTICES - k;
                int linhaCima = N_VERTICES - k - 1;
                mapa.put(nome, new int[][] {
                    { linhaBaixo, coluna },
                    { linhaCima, coluna }
                });
            }
        }

        return mapa;
    }

    // -----------------------------------------------------------------
    // Caminhos das imagens (carro1.png .. carro8.png, P03_SA.png, etc.)
    // -----------------------------------------------------------------
    public static final String CAMINHO_IMG = "/img/";

    public static String nomeImagemCarro(int numeroCarro) {
        return CAMINHO_IMG + "carro" + numeroCarro + ".png";
    }

    public static String nomeImagemPercurso(int numeroCarro) {
        int idx = numeroCarro - 1;
        return CAMINHO_IMG + CARRO_PERCURSO_NOME[idx] + "_" + CARRO_SENTIDO[idx] + ".png";
    }

    // -----------------------------------------------------------------
    // Parametros de simulacao
    // -----------------------------------------------------------------
    public static final long PASSO_BASE_MS = 600;   // tempo base de um "passo" do carro (na velocidade 1.0x)
    public static final double VELOCIDADE_MIN = 0.25;
    public static final double VELOCIDADE_MAX = 3.0;
    public static final double VELOCIDADE_PADRAO = 1.0;

    private Constantes() {
        // classe utilitaria: nao deve ser instanciada
    }
}
