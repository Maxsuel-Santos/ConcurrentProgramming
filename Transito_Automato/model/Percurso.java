/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: Percurso.java
* Funcao...........: Representa o ciclo fechado de um carro: a sequencia
*                    ORDENADA de Arestas que ele percorre repetidamente,
*                    junto com as REGIOES CRITICAS (se houver) de cada
*                    trecho
*                    do ciclo.
*
*                    Constantes.CARRO_x_TRECHOS guarda cada percurso JA'
*                    na ordem real de deslocamento (sentido SA/SH
*                    validado geometricamente) - nao ha' mais inversao
*                    de ordem por sentido nesta versao.
*
*                    Constantes.TRECHOS_REGIOES_CRITICAS lista as 57
*                    RCs do arquivo original. Este Percurso encaixa cada
*                    RC no ciclo do carro correspondente, inclusive
*                    quando a ordem aparece invertida ou atravessa a
*                    virada do ciclo, e calcula em quais indices a
*                    ThreadCarro deve adquirir/liberar semaforos.
************************************************************************ */

package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import util.Constantes;

public class Percurso {

    private final String nomePercurso; // ex: "P05"
    private final String sentido;      // SENTIDO_HORARIO ou SENTIDO_ANTI_HORARIO
    private final List<Aresta> arestasOrdenadas = new ArrayList<>();
    private final List<Vertice> verticesOrdenados = new ArrayList<>();
    private final List<Set<String>> regioesPorIndice = new ArrayList<>(); // paralela a arestasOrdenadas
    private final List<List<String>> regioesEntradaPorIndice = new ArrayList<>();
    private final List<List<String>> regioesSaidaPorIndice = new ArrayList<>();

    /* ***************************************************************
    * Construtor: Percurso
    * Funcao: Resolve a lista de nomes de trechos (ja' na ordem real do
    *         ciclo) para Arestas/Vertices reais do Grid, e associa a
    *         regioes criticas que existem no percurso deste carro.
    * Parametros: @param grid malha com as Arestas/Vertices reais
    *             @param nomePercurso nome do percurso (ex: "P05")
    *             @param sentido SA ou SH (apenas descritivo aqui)
    *             @param trechos nomes dos trechos, na ordem real do ciclo
    *             @param numeroCarro numero do carro (1..8)
    *************************************************************** */
    public Percurso(Grid grid, String nomePercurso, String sentido,
                     String[] trechos, int numeroCarro) {
        this.nomePercurso = nomePercurso;
        this.sentido = sentido;

        montarCiclo(grid, trechos);
        marcarRegioesCriticas(trechos, numeroCarro);
        calcularEntradasESaidas();
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
    * Metodo: montarCiclo
    * Funcao: Percorre a lista de nomes de trechos, partindo do vertice
    *         inicial correto (ver determinarVerticeInicial), resolve a
    *         sequencia real de Arestas/Vertices visitados.
    * Parametros: @param grid malha com as Arestas/Vertices reais
    *             @param trechos nomes dos trechos, na ordem de percurso
    * Retorno: sem retorno
    *************************************************************** */
    private void montarCiclo(Grid grid, String[] trechos) {
        Vertice atual = determinarVerticeInicial(grid, trechos);
        verticesOrdenados.add(atual);

        for (int i = 0; i < trechos.length; i++) {
            Aresta aresta = grid.getAresta(trechos[i]);

            Vertice proximo = aresta.outroExtremo(atual);

            arestasOrdenadas.add(aresta);
            verticesOrdenados.add(proximo);
            regioesPorIndice.add(new LinkedHashSet<>());
            regioesEntradaPorIndice.add(new ArrayList<>());
            regioesSaidaPorIndice.add(new ArrayList<>());

            atual = proximo;
        }
    }

    private void marcarRegioesCriticas(String[] trechos, int numeroCarro) {
        for (int i = 0; i < Constantes.NOMES_REGIOES_CRITICAS.length; i++) {
            int carroA = Constantes.CARROS_REGIOES_CRITICAS[i][0];
            int carroB = Constantes.CARROS_REGIOES_CRITICAS[i][1];

            if (numeroCarro != carroA && numeroCarro != carroB) {
                continue;
            }

            String nomeRegiao = Constantes.NOMES_REGIOES_CRITICAS[i];
            String[] trechosRegiao = Constantes.TRECHOS_REGIOES_CRITICAS[i];
            int[] indices = localizarRegiaoNoCiclo(trechos, trechosRegiao);

            if (indices == null) {
                throw new IllegalStateException(
                    "A regiao critica " + nomeRegiao + " nao encaixa no percurso do carro "
                    + numeroCarro + " (" + nomePercurso + ")"
                );
            }

            for (int indice : indices) {
                regioesPorIndice.get(indice).add(nomeRegiao);
            }
        }
    }

    private int[] localizarRegiaoNoCiclo(String[] trechos, String[] trechosRegiao) {
        int[] indices = localizarSequenciaNoCiclo(trechos, trechosRegiao);
        if (indices != null) {
            return indices;
        }

        String[] invertida = new String[trechosRegiao.length];
        for (int i = 0; i < trechosRegiao.length; i++) {
            invertida[i] = trechosRegiao[trechosRegiao.length - 1 - i];
        }
        return localizarSequenciaNoCiclo(trechos, invertida);
    }

    private int[] localizarSequenciaNoCiclo(String[] trechos, String[] sequencia) {
        int n = trechos.length;
        for (int inicio = 0; inicio < n; inicio++) {
            boolean encontrou = true;
            for (int deslocamento = 0; deslocamento < sequencia.length; deslocamento++) {
                if (!trechos[(inicio + deslocamento) % n].equals(sequencia[deslocamento])) {
                    encontrou = false;
                    break;
                }
            }

            if (encontrou) {
                int[] indices = new int[sequencia.length];
                for (int deslocamento = 0; deslocamento < sequencia.length; deslocamento++) {
                    indices[deslocamento] = (inicio + deslocamento) % n;
                }
                return indices;
            }
        }
        return null;
    }

    private void calcularEntradasESaidas() {
        for (String nomeRegiao : Constantes.NOMES_REGIOES_CRITICAS) {
            for (int indice = 0; indice < regioesPorIndice.size(); indice++) {
                if (!regioesPorIndice.get(indice).contains(nomeRegiao)) {
                    continue;
                }

                int anterior = ((indice - 1) + regioesPorIndice.size()) % regioesPorIndice.size();
                int proximo = (indice + 1) % regioesPorIndice.size();

                if (!regioesPorIndice.get(anterior).contains(nomeRegiao)) {
                    regioesEntradaPorIndice.get(indice).add(nomeRegiao);
                }
                if (!regioesPorIndice.get(proximo).contains(nomeRegiao)) {
                    regioesSaidaPorIndice.get(indice).add(nomeRegiao);
                }
            }
        }

        for (int i = 0; i < regioesEntradaPorIndice.size(); i++) {
            Collections.sort(regioesEntradaPorIndice.get(i));
            Collections.sort(regioesSaidaPorIndice.get(i));
        }
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

    /* ***************************************************************
    * Metodo: getRegioesEntrada
    * Funcao: Devolve as regioes criticas que comecam no trecho de
    *         indice informado.
    * Parametros: @param indice posicao desejada no ciclo
    * Retorno: @return lista de nomes de regioes criticas
    *************************************************************** */
    public List<String> getRegioesEntrada(int indice) {
        int i = indice % regioesEntradaPorIndice.size();
        return regioesEntradaPorIndice.get(i);
    }

    public List<String> getRegioesDoTrecho(int indice) {
        int i = indice % regioesPorIndice.size();
        List<String> regioes = new ArrayList<>(regioesPorIndice.get(i));
        Collections.sort(regioes);
        return regioes;
    }

    /* ***************************************************************
    * Metodo: getRegioesSaida
    * Funcao: Devolve as regioes criticas que terminam no trecho de
    *         indice informado.
    * Parametros: @param indice posicao desejada no ciclo
    * Retorno: @return lista de nomes de regioes criticas
    *************************************************************** */
    public List<String> getRegioesSaida(int indice) {
        int i = indice % regioesSaidaPorIndice.size();
        return regioesSaidaPorIndice.get(i);
    }

    public List<Aresta> getArestasOrdenadas() {
        return arestasOrdenadas;
    }

    public List<Vertice> getVerticesOrdenados() {
        return verticesOrdenados;
    }
}
