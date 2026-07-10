package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Representa um percurso circular formado por trechos RHxx e RVxx. */
public class Percurso {

    private final String nome;
    private final String sentido;
    private final List<Aresta> arestas = new ArrayList<>();
    private final List<Vertice> vertices = new ArrayList<>();

    public Percurso(Grid grid, String nome, String sentido, String[] nomesTrechos) {
        this.nome = nome;
        this.sentido = sentido;
        montarCiclo(grid, nomesTrechos);
    }

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

    public String getNome() {
        return nome;
    }

    public String getSentido() {
        return sentido;
    }

    public int getQuantidadeTrechos() {
        return arestas.size();
    }

    public Aresta getAresta(int indice) {
        return arestas.get(normalizar(indice, arestas.size()));
    }

    public Vertice getVertice(int indice) {
        int quantidade = arestas.size();
        return vertices.get(normalizar(indice, quantidade));
    }

    public List<Aresta> getArestas() {
        return Collections.unmodifiableList(arestas);
    }

    private int normalizar(int indice, int tamanho) {
        int resultado = indice % tamanho;
        return resultado < 0 ? resultado + tamanho : resultado;
    }
}
