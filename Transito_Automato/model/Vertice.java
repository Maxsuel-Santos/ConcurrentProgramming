/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 23/06/2026
* Ultima alteracao.: 23/06/2026
* Nome.............: Vertice.java
* Funcao...........: Representa um cruzamento (no) da malha de ruas.
*                    Guarda apenas sua posicao logica (linha, coluna) na
*                    grade 6x6 e a posicao em pixel correspondente na
*                    tela, usada para posicionar/animar os carros.
************************************************************************ */

package model;

public class Vertice {

    private final int linha;
    private final int coluna;
    private double x; // posicao em pixel (eixo horizontal) na tela
    private double y; // posicao em pixel (eixo vertical) na tela

    public Vertice(int linha, int coluna) {
        this.linha = linha;
        this.coluna = coluna;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

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

    @Override
    public int hashCode() {
        return chave().hashCode();
    }

    @Override
    public String toString() {
        return "Vertice(" + linha + "," + coluna + ")";
    }
}
