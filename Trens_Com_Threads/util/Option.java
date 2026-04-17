/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 15/04/2026
* Ultima alteracao.: 15/04/2026
* Nome.............: Option.java
* Funcao...........: Representa as opcoes de direcao dos trens
************************************************************************ */

package util;

/* *************************************************************** 
* Enum: Option 
* Funcao: Representa as opcoes de direcao dos trens 
*************************************************************** */
public enum Option {
    
    OP1("MESMA DIRECAO"),
    OP2("MESMA DIRECAO INVERSA"),
    OP3("DIRECAO OPOSTA"),
    OP4("DIRECAO OPOSTA INVERSA");

    private final String title;

    /* *************************************************************** 
    * Metodo: Option 
    * Funcao: Construtor do enum Option 
    * Parametros: @param title eh o texto da opcao 
    * Retorno: nao possui retorno 
    *************************************************************** */
    Option(String title) {
      this.title = title;
    }

    /* *************************************************************** 
    * Metodo: getTitle 
    * Funcao: Retorna o titulo da opcao 
    * Parametros: nao possui parametros 
    * Retorno: String contendo o titulo da opcao 
    *************************************************************** */
    public String getTitle() {
      return title;
    }

}
