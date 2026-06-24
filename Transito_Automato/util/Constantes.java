/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 21/06/2026
* Ultima alteracao.: 24/06/2026
* Nome.............: Constantes.java
* Funcao...........: Centraliza todas as constantes do problema: a malha
*                    de ruas (6x6 vertices), os 8 percursos sorteados
*                    (cada um como uma sequencia de trechos RHxx/RVxx) e
*                    a lista dos trechos compartilhados entre carros, que
*                    precisarao de semaforo (regiao critica).
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
    // Sequencia de trechos (arestas RHxx/RVxx) que compoem o ciclo de
    // cada percurso. A ordem abaixo percorre o ciclo sempre no MESMO
    // sentido de referencia (sentido horario na tela); o sentido real
    // de cada carro (SA/SH) e aplicado depois, em tempo de execucao,
    // percorrendo esta mesma lista de forma direta (SH) ou invertida
    // (SA). Ver model.Percurso.
    // -----------------------------------------------------------------
    public static final String[] CARRO_1_TRECHOS = {
        "RH01","RH02","RH03","RH04","RH05",
        "RV30","RV29","RV28","RV27","RV26",
        "RH30","RH29","RH28","RH27","RH26",
        "RV01","RV02","RV03","RV04","RV05"
    };

    public static final String[] CARRO_2_TRECHOS = {
        "RH01","RH02","RH03","RH04","RH05",
        "RV30","RV29","RH15","RH14","RV18",
        "RH19","RV22","RH25","RV26","RH30",
        "RH29","RV16","RH23","RV11","RH27",
        "RH26","RV01","RV02","RV03","RV04","RV05"
    };

    public static final String[] CARRO_3_TRECHOS = {
        "RH01","RH02","RH03","RH04","RH05",
        "RV30","RV29","RH15","RH14","RH13",
        "RH12","RH11","RV04","RV05"
    };

    public static final String[] CARRO_4_TRECHOS = {
        "RH01","RH02","RV15","RV14","RV13",
        "RV12","RV11","RH27","RH26","RV01",
        "RV02","RV03","RV04","RV05"
    };

    public static final String[] CARRO_5_TRECHOS = {
        "RH03","RH04","RH05","RV30","RV29",
        "RV28","RH20","RH19","RH18","RV13",
        "RV14","RV15"
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
        "RH01","RH02","RV15","RH08","RV19",
        "RH14","RV23","RH20","RV27","RV26",
        "RH30","RH29","RV16","RH23","RV12",
        "RH17","RV08","RH11","RV04","RV05"
    };

    // Indexado por (numero do carro - 1)
    public static final String[][] CARRO_TRECHOS = {
        CARRO_1_TRECHOS, CARRO_2_TRECHOS, CARRO_3_TRECHOS, CARRO_4_TRECHOS,
        CARRO_5_TRECHOS, CARRO_6_TRECHOS, CARRO_7_TRECHOS, CARRO_8_TRECHOS
    };

    // -----------------------------------------------------------------
    // Trechos compartilhados por 2 ou mais carros (= precisam de
    // semaforo). Calculado a partir da intersecao dos 8 percursos
    // acima e conferido manualmente com a tabela de pares fornecida
    // pelo aluno (28 combinacoes, todas batendo).
    // Total: 34 trechos compartilhados, de 52 trechos distintos usados.
    // -----------------------------------------------------------------
    public static final String[] TRECHOS_COMPARTILHADOS = {
        "RH01","RH02","RH03","RH04","RH05",
        "RH11","RH13","RH14","RH15","RH19",
        "RH20","RH23","RH26","RH27","RH28",
        "RH29","RH30",
        "RV01","RV02","RV03","RV04","RV05",
        "RV11","RV12","RV13","RV14","RV15",
        "RV16","RV22","RV26","RV27","RV28",
        "RV29","RV30"
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
