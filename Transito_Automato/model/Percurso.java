/* ***************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 24/06/2026
* Ultima alteracao.: 10/07/2026
* Nome.............: Percurso.java
* Funcao...........: Representa um percurso circular formado por trechos da malha.
************************************************************************ */
package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* ***************************************************************
* Classe: Percurso
* Funcao: Representa um percurso circular formado por trechos da malha.
*************************************************************** */
public class Percurso {

    private final String nome;
    private final String sentido;
    private final List<Aresta> arestas = new ArrayList<>();
    private final List<Vertice> vertices = new ArrayList<>();

    /* ***************************************************************
    * Metodo: Percurso
    * Funcao: Inicializa uma nova instancia de Percurso.
    * Parametros: grid parametro grid; nome parametro nome; sentido parametro sentido; nomesTrechos parametro nomesTrechos
    * Retorno: sem retorno
    *************************************************************** */
    public Percurso(Grid grid, String nome, String sentido, String[] nomesTrechos) {
        this.nome = nome;
        this.sentido = sentido;
        montarCiclo(grid, nomesTrechos);
    }

    /* ***************************************************************
    * Metodo: montarCiclo
    * Funcao: Monta ciclo.
    * Parametros: grid parametro grid; nomesTrechos parametro nomesTrechos
    * Retorno: sem retorno
    *************************************************************** */
    private void montarCiclo(Grid grid, String[] nomesTrechos) {
        if (nomesTrechos == null || nomesTrechos.length < 2) {
            throw new IllegalArgumentException("O percurso precisa de pelo menos dois trechos.");
        }

        Aresta primeira = grid.getAresta(nomesTrechos[0]);
        Aresta segunda = grid.getAresta(nomesTrechos[1]);
        Vertice atual = descobrirInicio(primeira, segunda);
        Vertice inicio = atual;
        vertices.add(atual);

        for (String nomeTrecho : nomesTrechos) {
            Aresta aresta = grid.getAresta(nomeTrecho);
            Vertice proximo = aresta.outroExtremo(atual);
            arestas.add(aresta);
            vertices.add(proximo);
            atual = proximo;
        }

        if (!atual.equals(inicio)) {
            throw new IllegalArgumentException("O percurso " + nome + " nao fecha um ciclo.");
        }
    }

    /* ***************************************************************
    * Metodo: descobrirInicio
    * Funcao: Executa a operacao descobrir inicio.
    * Parametros: primeira parametro primeira; segunda parametro segunda
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    private Vertice descobrirInicio(Aresta primeira, Aresta segunda) {
        Vertice a = primeira.getOrigem();
        Vertice b = primeira.getDestino();
        boolean aPertenceASegunda = a.equals(segunda.getOrigem()) || a.equals(segunda.getDestino());
        boolean bPertenceASegunda = b.equals(segunda.getOrigem()) || b.equals(segunda.getDestino());

        if (aPertenceASegunda && !bPertenceASegunda) {
            return b;
        }
        if (bPertenceASegunda && !aPertenceASegunda) {
            return a;
        }
        throw new IllegalArgumentException("Os dois primeiros trechos nao formam uma sequencia valida.");
    }

    /* ***************************************************************
    * Metodo: getNome
    * Funcao: Retorna nome.
    * Parametros: nenhum
    * Retorno: texto resultante
    *************************************************************** */
    public String getNome() {
        return nome;
    }

    /* ***************************************************************
    * Metodo: getSentido
    * Funcao: Retorna sentido.
    * Parametros: nenhum
    * Retorno: texto resultante
    *************************************************************** */
    public String getSentido() {
        return sentido;
    }

    /* ***************************************************************
    * Metodo: getQuantidadeTrechos
    * Funcao: Retorna quantidade trechos.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public int getQuantidadeTrechos() {
        return arestas.size();
    }

    /* ***************************************************************
    * Metodo: getAresta
    * Funcao: Retorna aresta.
    * Parametros: indice parametro indice
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Aresta getAresta(int indice) {
        return arestas.get(normalizar(indice, arestas.size()));
    }

    /* ***************************************************************
    * Metodo: getVertice
    * Funcao: Retorna vertice.
    * Parametros: indice parametro indice
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public Vertice getVertice(int indice) {
        int quantidade = arestas.size();
        return vertices.get(normalizar(indice, quantidade));
    }

    /* ***************************************************************
    * Metodo: getArestas
    * Funcao: Retorna arestas.
    * Parametros: nenhum
    * Retorno: objeto ou colecao resultante
    *************************************************************** */
    public List<Aresta> getArestas() {
        return Collections.unmodifiableList(arestas);
    }

    /* ***************************************************************
    * Metodo: normalizar
    * Funcao: Normaliza .
    * Parametros: indice parametro indice; tamanho parametro tamanho
    * Retorno: valor calculado
    *************************************************************** */
    private int normalizar(int indice, int tamanho) {
        int resultado = indice % tamanho;
        return resultado < 0 ? resultado + tamanho : resultado;
    }
}
