/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 10/07/2026
* Nome.............: Grid.java
* Funcao...........: Monta a malha completa de vertices (cruzamentos) e
*                    arestas (trechos RHxx/RVxx) da quadra 5x5, a partir
*                    do mapa de coordenadas definido em Constantes.
*
*                    Cada Aresta criada aqui eh apenas geometria (nome +
*                    extremos) - a informacao de ZONA CRITICA (semaforo)
*                    fica em model.Percurso, pois um mesmo trecho pode
*                    ser o INICIO da zona para um carro e o MEIO da zona
*                    para outro, dependendo de onde cada percurso entra
*                    naquela regiao.
*
*                    Tambem calcula a posicao em pixel de cada vertice,
*                    para que os Carros (e o Controller) saibam onde
*                    desenhar cada cruzamento/segmento na tela.
************************************************************************ */

package model;

import java.util.LinkedHashMap;
import java.util.Map;

import util.Constantes;

/* ***************************************************************
* Classe: Grid
* Funcao: Constroi e disponibiliza a malha de vertices e arestas.
*************************************************************** */
public class Grid {

    private final Map<String, Vertice> vertices = new LinkedHashMap<>();
    private final Map<String, Aresta> arestas = new LinkedHashMap<>();

    // Origem (canto superior-esquerdo) e tamanho de cada quadra, em
    // pixels, usados para converter (linha,coluna) em (x,y) na tela.
    private final double origemX;
    private final double origemY;
    private final double tamanhoQuadra;

    /* ***************************************************************
    * Metodo: Grid
    * Funcao: Inicializa uma nova instancia de Grid.
    * Parametros: origemX parametro origemX; origemY parametro origemY; tamanhoQuadra parametro tamanhoQuadra
    * Retorno: sem retorno
    *************************************************************** */
    public Grid(double origemX, double origemY, double tamanhoQuadra) {
        this.origemX = origemX;
        this.origemY = origemY;
        this.tamanhoQuadra = tamanhoQuadra;

        criarVertices();
        criarArestas();
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
    *         Aresta (apenas geometria) para cada um.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void criarArestas() {
        Map<String, int[][]> mapa = Constantes.montarMapaArestas();

        for (Map.Entry<String, int[][]> entrada : mapa.entrySet()) {
            String nome = entrada.getKey();
            int[][] pontos = entrada.getValue();

            Vertice origem = getVertice(pontos[0][0], pontos[0][1]);
            Vertice destino = getVertice(pontos[1][0], pontos[1][1]);

            arestas.put(nome, new Aresta(nome, origem, destino));
        }
    }

    /* ***************************************************************
    * Metodo: getVertice
    * Funcao: Retorna vertice.
    * Parametros: linha parametro linha; coluna parametro coluna
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Vertice getVertice(int linha, int coluna) {
        return vertices.get(Vertice.chave(linha, coluna));
    }

    /* ***************************************************************
    * Metodo: getAresta
    * Funcao: Retorna aresta.
    * Parametros: nome parametro nome
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Aresta getAresta(String nome) {
        Aresta a = arestas.get(nome);
        if (a == null) {
            throw new IllegalArgumentException("Aresta desconhecida: " + nome);
        }
        return a;
    }

    /* ***************************************************************
    * Metodo: getVertices
    * Funcao: Retorna vertices.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Map<String, Vertice> getVertices() {
        return vertices;
    }

    /* ***************************************************************
    * Metodo: getArestas
    * Funcao: Retorna arestas.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Map<String, Aresta> getArestas() {
        return arestas;
    }
}
