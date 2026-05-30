/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 29/05/2026
* Ultima alteracao.: 29/05/2026
* Nome.............: EstadoFilosofo.java
* Funcao...........: Enum que representa os 3 estados possiveis de
*                    cada filosofo conforme o pseudocodigo do livro
*                    de Tanenbaum:
*                      #define THINKING 0
*                      #define HUNGRY   1
*                      #define EATING   2
************************************************************************ */
package Jantar_Dos_Filosofos.model;

/* ***************************************************************
* Enum: EstadoFilosofo
* Funcao: Mapeia os tres estados do pseudocodigo C para Java.
*         PENSANDO = THINKING = 0
*         FAMINTO  = HUNGRY   = 1
*         COMENDO  = EATING   = 2
*************************************************************** */
public enum EstadoFilosofo {

  PENSANDO,
  FAMINTO,
  COMENDO
  
} // Fim do enum EstadoFilosofo
