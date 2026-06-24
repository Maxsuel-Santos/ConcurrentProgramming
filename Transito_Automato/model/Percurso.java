/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 24/06/2026
* Nome.............: Percurso.java
* Funcao...........: Representa o ciclo fechado de um carro: a sequencia
*                    ORDENADA de Arestas que ele percorre repetidamente.
*
*                    Constantes.CARRO_x_TRECHOS pode vir em dois formatos:
*                    - JA' na ordem real de deslocamento (ordemJaResolvida
*                      = true): a lista e' usada exatamente como esta',
*                      sem nenhuma inversao. E' o caso do Carro 1.
*                    - Na ordem de referencia / sentido horario na tela
*                      (ordemJaResolvida = false): se o sentido do carro
*                      for Anti-Horario (SA), a ordem e' invertida antes
*                      de uso. Usado pelos carros que ainda nao tiveram
*                      sua lista de trechos revalidada no novo formato.
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

    /* ***************************************************************
    * Construtor: Percurso (ordem ja' resolvida)
    * Funcao: Usa a lista de trechos exatamente como informada, sem
    *         qualquer inversao - para percursos cuja ordem real de
    *         movimento ja' foi validada (ex: Carro 1).
    *************************************************************** */
    public Percurso(Grid grid, String nomePercurso, String sentido, String[] trechos) {
        this(grid, nomePercurso, sentido, trechos, true);
    }

    /* ***************************************************************
    * Construtor: Percurso (formato legado)
    * Funcao: Mantido por compatibilidade com os percursos (Carros
    *         2 a 8) cuja lista de trechos ainda esta' na ordem de
    *         referencia (sentido horario) e depende da inversao por
    *         sentido. Sera' descontinuado quando todos os percursos
    *         migrarem para o formato de ordem ja' resolvida.
    * Parametros: @param ordemJaResolvida false = aplica a inversao
    *             legada quando o sentido for Anti-Horario
    *************************************************************** */
    public Percurso(Grid grid, String nomePercurso, String sentido, String[] trechos,
                     boolean ordemJaResolvida) {
        this.nomePercurso = nomePercurso;
        this.sentido = sentido;

        montarCicloNaOrdemBase(grid, trechos);

        if (!ordemJaResolvida && Constantes.SENTIDO_ANTI_HORARIO.equals(sentido)) {
            inverterCiclo();
        }
    }

    /* ***************************************************************
    * Metodo: determinarVerticeInicial
    * Funcao: Identifica o vertice de partida correto do ciclo: o
    *         extremo da primeira aresta que NAO e' compartilhado com a
    *         segunda aresta da lista. Como uma Aresta nao tem direcao
    *         fixa (origem/destino sao apenas rotulos), nao basta usar
    *         getOrigem() - isso falharia sempre que a "origem" rotulada
    *         coincidir com o vertice de CHEGADA do trecho.
    * Parametros: @param grid malha com as Arestas reais
    *             @param trechos nomes dos trechos, na ordem do ciclo
    * Retorno: @return Vertice de partida do ciclo
    *************************************************************** */
    private Vertice determinarVerticeInicial(Grid grid, String[] trechos) {
        Aresta primeira = grid.getAresta(trechos[0]);

        if (trechos.length == 1) {
            // ciclo degenerado de 1 trecho (nao deveria ocorrer na pratica)
            return primeira.getOrigem();
        }

        Aresta segunda = grid.getAresta(trechos[1]);

        Vertice a = primeira.getOrigem();
        Vertice b = primeira.getDestino();

        boolean aPertenceSegunda = a.equals(segunda.getOrigem()) || a.equals(segunda.getDestino());
        boolean bPertenceSegunda = b.equals(segunda.getOrigem()) || b.equals(segunda.getDestino());

        if (aPertenceSegunda && !bPertenceSegunda) {
            return b;
        }
        if (bPertenceSegunda && !aPertenceSegunda) {
            return a;
        }

        // ambiguo (ambos ou nenhum extremo coincide) - cai para a origem
        // rotulada como padrao
        return a;
    }

    /* ***************************************************************
    * Metodo: montarCicloNaOrdemBase
    * Funcao: Percorre a lista de nomes de trechos, partindo do vertice
    *         inicial correto (ver determinarVerticeInicial), e resolve
    *         a sequencia real de Arestas/Vertices visitados.
    * Parametros: @param grid malha com as Arestas/Vertices reais
    *             @param trechos nomes dos trechos, na ordem de percurso
    * Retorno: sem retorno
    *************************************************************** */
    private void montarCicloNaOrdemBase(Grid grid, String[] trechos) {
        Vertice atual = determinarVerticeInicial(grid, trechos);
        verticesOrdenados.add(atual);

        for (String nomeTrecho : trechos) {
            Aresta aresta = grid.getAresta(nomeTrecho);

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
