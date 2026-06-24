/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 23/06/2026
* Nome.............: Grid.java
* Funcao...........: Monta a malha completa de vertices (cruzamentos) e
*                    arestas (trechos RHxx/RVxx) da quadra 5x5, a partir
*                    do mapa de coordenadas definido em Constantes. Cada
*                    Aresta criada aqui ja vem associada ao seu Semaphore
*                    (quando o trecho for regiao critica), por meio do
*                    GerenciadorSemaforos recebido no construtor.
*
*                    Tambem calcula a posicao em pixel de cada vertice,
*                    para que os Carros (e o Controller) saibam onde
*                    desenhar cada cruzamento/segmento na tela.
************************************************************************ */

package model;

import java.util.LinkedHashMap;
import java.util.Map;

import util.Constantes;
import util.GerenciadorSemaforos;

public class Grid {

    private final Map<String, Vertice> vertices = new LinkedHashMap<>();
    private final Map<String, Aresta> arestas = new LinkedHashMap<>();

    // Origem (canto superior-esquerdo) e tamanho de cada quadra, em
    // pixels, usados para converter (linha,coluna) em (x,y) na tela.
    private final double origemX;
    private final double origemY;
    private final double tamanhoQuadra;

    public Grid(GerenciadorSemaforos gerenciadorSemaforos,
                double origemX, double origemY, double tamanhoQuadra) {

        this.origemX = origemX;
        this.origemY = origemY;
        this.tamanhoQuadra = tamanhoQuadra;

        criarVertices();
        criarArestas(gerenciadorSemaforos);
    }

    /* ***************************************************************
    * Metodo: criarVertices
    * Funcao: Instancia os (N_VERTICES x N_VERTICES) vertices da malha
    *         e calcula a posicao em pixel de cada um.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void criarVertices() {
        for (int linha = 0; linha < Constantes.N_VERTICES; linha++) {
            for (int coluna = 0; coluna < Constantes.N_VERTICES; coluna++) {
                Vertice v = new Vertice(linha, coluna);
                v.setX(origemX + coluna * tamanhoQuadra);
                v.setY(origemY + linha * tamanhoQuadra);
                vertices.put(v.chave(), v);
            }
        }
    }

    /* ***************************************************************
    * Metodo: criarArestas
    * Funcao: Le o mapa de trechos definido em Constantes e cria uma
    *         Aresta para cada um, associando o Semaphore correto
    *         quando o trecho for regiao critica.
    * Parametros: @param gerenciadorSemaforos fonte dos semaforos
    * Retorno: sem retorno
    *************************************************************** */
    private void criarArestas(GerenciadorSemaforos gerenciadorSemaforos) {
        Map<String, int[][]> mapa = Constantes.montarMapaArestas();

        for (Map.Entry<String, int[][]> entrada : mapa.entrySet()) {
            String nome = entrada.getKey();
            int[][] pontos = entrada.getValue();

            Vertice origem = getVertice(pontos[0][0], pontos[0][1]);
            Vertice destino = getVertice(pontos[1][0], pontos[1][1]);

            Aresta aresta = new Aresta(
                nome,
                origem,
                destino,
                gerenciadorSemaforos.getSemaforo(nome) // null se nao for regiao critica
            );

            arestas.put(nome, aresta);
        }
    }

    public Vertice getVertice(int linha, int coluna) {
        return vertices.get(Vertice.chave(linha, coluna));
    }

    public Aresta getAresta(String nome) {
        Aresta a = arestas.get(nome);
        if (a == null) {
            throw new IllegalArgumentException("Aresta desconhecida: " + nome);
        }
        return a;
    }

    public Map<String, Vertice> getVertices() {
        return vertices;
    }

    public Map<String, Aresta> getArestas() {
        return arestas;
    }
}
