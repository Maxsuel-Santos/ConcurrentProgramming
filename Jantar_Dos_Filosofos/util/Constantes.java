/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 29/05/2026
* Ultima alteracao.: 29/05/2026
* Nome.............: Constantes.java
* Funcao...........: Constantes globais da aplicacao Jantar dos Filosofos
************************************************************************ */
package util;

/* ***************************************************************
* Classe: Constantes
* Funcao: Define as constantes utilizadas em toda a aplicacao.
*************************************************************** */
public class Constantes {

  public static final int N = 5;

  // Indice do vizinho esquerdo do filosofo i 
  public static int esquerda(int i) {
    return (i + N - 1) % N;
  }

  // Indice do vizinho direito do filosofo i 
  public static int direita(int i) {
    return (i + 1) % N;
  }

  public static final int DEFAULT_SPEED_MS = 3000;

  public static final int MIN_SPEED_MS = 500;

  public static final int MAX_SPEED_MS = 6000;

} // Fim da classe Constantes
