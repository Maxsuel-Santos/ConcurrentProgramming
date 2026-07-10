/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 10/07/2026
* Nome.............: Vertice.java
* Funcao...........: Representa um cruzamento (no) da malha de ruas.
*                    Guarda apenas sua posicao logica (linha, coluna) na
*                    grade 6x6 e a posicao em pixel correspondente na
*                    tela, usada para posicionar/animar os carros.
************************************************************************ */

package model;

/* ***************************************************************
* Classe: Vertice
* Funcao: Representa um cruzamento e suas coordenadas logicas e visuais.
*************************************************************** */
public class Vertice {

    private final int linha;
    private final int coluna;
    private double x; // posicao em pixel (eixo horizontal) na tela
    private double y; // posicao em pixel (eixo vertical) na tela

    /* ***************************************************************
    * Metodo: Vertice
    * Funcao: Inicializa uma nova instancia de Vertice.
    * Parametros: linha parametro linha; coluna parametro coluna
    * Retorno: sem retorno
    *************************************************************** */
    public Vertice(int linha, int coluna) {
        this.linha = linha;
        this.coluna = coluna;
    }

    /* ***************************************************************
    * Metodo: getLinha
    * Funcao: Retorna linha.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public int getLinha() {
        return linha;
    }

    /* ***************************************************************
    * Metodo: getColuna
    * Funcao: Retorna coluna.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public int getColuna() {
        return coluna;
    }

    /* ***************************************************************
    * Metodo: getX
    * Funcao: Retorna x.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public double getX() {
        return x;
    }

    /* ***************************************************************
    * Metodo: setX
    * Funcao: Define x.
    * Parametros: x parametro x
    * Retorno: sem retorno
    *************************************************************** */
    public void setX(double x) {
        this.x = x;
    }

    /* ***************************************************************
    * Metodo: getY
    * Funcao: Retorna y.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
    public double getY() {
        return y;
    }

    /* ***************************************************************
    * Metodo: setY
    * Funcao: Define y.
    * Parametros: y parametro y
    * Retorno: sem retorno
    *************************************************************** */
    public void setY(double y) {
        this.y = y;
    }

    /* ***************************************************************
    * Metodo: chave
    * Funcao: Gera uma chave textual unica "linha,coluna" para uso em
    *         mapas (HashMap) dentro do Grid.
    * Parametros: nenhum
    * Retorno: @return String chave do vertice
    *************************************************************** */
    public static String chave(int linha, int coluna) {
        return linha + "," + coluna;
    }

    public String chave() {
        return chave(linha, coluna);
    }

    /* ***************************************************************
    * Metodo: equals
    * Funcao: Compara este objeto com outro objeto.
    * Parametros: obj parametro obj
    * Retorno: verdadeiro quando a condicao for atendida
    *************************************************************** */
   @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vertice)) {
            return false;
        }
        Vertice outro = (Vertice) obj;
        return this.linha == outro.linha && this.coluna == outro.coluna;
    }

    /* ***************************************************************
    * Metodo: hashCode
    * Funcao: Calcula o codigo de dispersao do objeto.
    * Parametros: nenhum
    * Retorno: valor calculado
    *************************************************************** */
   @Override
    public int hashCode() {
        return chave().hashCode();
    }

    /* ***************************************************************
    * Metodo: toString
    * Funcao: Gera a representacao textual do objeto.
    * Parametros: nenhum
    * Retorno: texto resultante
    *************************************************************** */
   @Override
    public String toString() {
        return "Vertice(" + linha + "," + coluna + ")";
    }
}
