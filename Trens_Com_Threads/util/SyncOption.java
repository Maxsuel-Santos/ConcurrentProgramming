/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 15/04/2026
* Ultima alteracao.: 15/04/2026
* Nome.............: SyncOption.java
* Funcao...........: Representa as opcoes de sincronizacao/exclusao mutua
************************************************************************ */

package util;

/* ***************************************************************
* Enum: SyncOption
* Funcao: Representa as 4 opcoes de algoritmo de exclusao mutua
*         exibidas no painel de controle da simulacao.
*************************************************************** */
public enum SyncOption {

  OP1("TRENS COLIDINDO"),
  OP2("VARIAVEL DE TRAVAMENTO"),
  OP3("ESTRITA ALTERNANCIA"),
  OP4("SOLUCAO DE PETERSON");

  private final String title;

  /* ***************************************************************
  * Metodo: SyncOption (construtor)
  * Funcao: Inicializa a opcao com o texto do botao.
  * Parametros: @param title texto exibido no botao
  * Retorno: nao possui
  *************************************************************** */
  SyncOption(String title) {
    this.title = title;
  } // fim do construtor

  /* ***************************************************************
  * Metodo: getTitle
  * Funcao: Retorna o titulo da opcao para exibicao no botao.
  * Parametros: nao possui
  * Retorno: @return String com o titulo da opcao
  *************************************************************** */
  public String getTitle() {
    return title;
  } // fim do metodo getTitle

} // fim do enum SyncOption