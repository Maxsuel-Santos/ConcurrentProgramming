/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 23/06/2026
* Nome.............: Percurso.java
* Funcao...........: Representa o ciclo fechado de um carro: a sequencia
*                    ORDENADA de Arestas que ele percorre repetidamente.
*
*                    Constantes.CARRO_x_TRECHOS guarda cada percurso na
*                    ordem que percorre o ciclo sempre no mesmo sentido
*                    de referencia (horario na tela). Aqui resolvemos
*                    essa lista de nomes para Arestas reais do Grid e,
*                    se o sentido do carro for Anti-Horario (SA),
*                    invertemos a ordem - o carro acaba andando, de
*                    fato, no sentido contrario ao da lista original,
*                    sem que seja necessario re-listar os trechos.
************************************************************************ */

package model;

import java.util.ArrayList;
import java.util.List;

import util.Constantes;

public class Percurso {

    private final String nomePercurso; // ex: "P05"
    private final String sentido;      // SENTIDO_HORARIO ou SENTIDO_ANTI_HORARIO
    private final List<Aresta> arestasOrdenadas = new ArrayList<>();
    private final List<Vertice> verticesOrdenados = new ArrayList<>();

    public Percurso(Grid grid, String nomePercurso, String sentido, String[] trechos) {
        this.nomePercurso = nomePercurso;
        this.sentido = sentido;

        montarCicloNaOrdemBase(grid, trechos);

        if (Constantes.SENTIDO_ANTI_HORARIO.equals(sentido)) {
            inverterCiclo();
        }
    }

    /* ***************************************************************
    * Metodo: montarCicloNaOrdemBase
    * Funcao: Percorre a lista de nomes de trechos (na ordem de
    *         referencia, sentido horario) e resolve a sequencia real
    *         de vertices visitados, validando que cada trecho conecta
    *         corretamente ao vertice atual.
    * Parametros: @param grid malha com as Arestas/Vertices reais
    *             @param trechos nomes dos trechos, na ordem horaria
    * Retorno: sem retorno
    *************************************************************** */
    private void montarCicloNaOrdemBase(Grid grid, String[] trechos) {
        Vertice atual = null;

        for (String nomeTrecho : trechos) {
            Aresta aresta = grid.getAresta(nomeTrecho);

            if (atual == null) {
                // primeiro trecho do ciclo: define o vertice inicial
                atual = aresta.getOrigem();
                verticesOrdenados.add(atual);
            }

            Vertice proximo = aresta.outroExtremo(atual);

            arestasOrdenadas.add(aresta);
            verticesOrdenados.add(proximo);

            atual = proximo;
        }
    }

    /* ***************************************************************
    * Metodo: inverterCiclo
    * Funcao: Inverte a ordem das arestas e vertices do ciclo, de modo
    *         que o carro passe a percorrer o mesmo trajeto no sentido
    *         contrario (Anti-Horario).
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    private void inverterCiclo() {
        java.util.Collections.reverse(arestasOrdenadas);
        java.util.Collections.reverse(verticesOrdenados);
    }

    public String getNomePercurso() {
        return nomePercurso;
    }

    public String getSentido() {
        return sentido;
    }

    public int getQuantidadeTrechos() {
        return arestasOrdenadas.size();
    }

    /* ***************************************************************
    * Metodo: getAresta
    * Funcao: Devolve a aresta do indice informado dentro do ciclo,
    *         dando a volta automaticamente quando o indice ultrapassa
    *         o tamanho do percurso (ciclo infinito).
    * Parametros: @param indice posicao desejada no ciclo
    * Retorno: @return Aresta correspondente
    *************************************************************** */
    public Aresta getAresta(int indice) {
        int i = indice % arestasOrdenadas.size();
        return arestasOrdenadas.get(i);
    }

    /* ***************************************************************
    * Metodo: getVertice
    * Funcao: Devolve o vertice (cruzamento) do indice informado dentro
    *         do ciclo. Como o ciclo e' fechado, o vertice de indice 0
    *         e' o mesmo de indice getQuantidadeTrechos().
    * Parametros: @param indice posicao desejada no ciclo
    * Retorno: @return Vertice correspondente
    *************************************************************** */
    public Vertice getVertice(int indice) {
        int i = indice % arestasOrdenadas.size();
        return verticesOrdenados.get(i);
    }

    public List<Aresta> getArestasOrdenadas() {
        return arestasOrdenadas;
    }

    public List<Vertice> getVerticesOrdenados() {
        return verticesOrdenados;
    }
}
