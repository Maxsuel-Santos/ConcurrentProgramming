/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: Aresta.java
* Funcao...........: Representa um trecho de rua (RHxx ou RVxx) que liga
*                    dois vertices da malha. Apenas a GEOMETRIA do
*                    trecho fica aqui (nome e extremos); a informacao de
*                    qual ZONA CRITICA (se houver) o trecho pertence, e
*                    os pontos de entrada/saida da zona dentro do ciclo
*                    de cada carro, ficam em model.Percurso - ja' que um
*                    mesmo trecho pode ser o INICIO da zona para um
*                    carro e o MEIO da zona para outro, dependendo de
*                    onde cada percurso entra naquela regiao.
************************************************************************ */

package model;

public class Aresta {

    private final String nome;     // ex: "RH01", "RV30"
    private final Vertice origem;
    private final Vertice destino;

    public Aresta(String nome, Vertice origem, Vertice destino) {
        this.nome = nome;
        this.origem = origem;
        this.destino = destino;
    }

    public String getNome() {
        return nome;
    }

    public Vertice getOrigem() {
        return origem;
    }

    public Vertice getDestino() {
        return destino;
    }

    /* ***************************************************************
    * Metodo: outroExtremo
    * Funcao: Dado um vertice de entrada nesta aresta, devolve o outro
    *         extremo (a aresta nao tem direcao fixa: pode ser
    *         percorrida de origem->destino ou de destino->origem,
    *         dependendo do sentido do carro).
    * Parametros: @param de vertice de partida
    * Retorno: @return Vertice extremo oposto a "de"
    *************************************************************** */
    public Vertice outroExtremo(Vertice de) {
        if (de.equals(origem)) {
            return destino;
        }
        if (de.equals(destino)) {
            return origem;
        }
        throw new IllegalArgumentException(
            "Vertice " + de + " nao pertence a aresta " + nome
        );
    }

    @Override
    public String toString() {
        return nome + "[" + origem + " - " + destino + "]";
    }
}
