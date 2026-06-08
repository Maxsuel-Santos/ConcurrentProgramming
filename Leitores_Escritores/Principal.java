/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 08/06/2026
* Ultima alteracao.: 08/06/2026
* Nome.............: Principal.java
* Funcao...........: Ponto de entrada da aplicacao JavaFX.
*                    Carrega a tela inicial (TelaInicial.fxml) e
*                    configura o fechamento correto da aplicacao.
************************************************************************ */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/* ***************************************************************
* Classe: Principal
* Funcao: Inicializa o JavaFX, carrega o FXML da tela inicial
*         e configura o encerramento da JVM ao fechar a janela.
*************************************************************** */
public class Principal extends Application {

    /* ***************************************************************
    * Metodo: start
    * Funcao: Metodo de entrada do JavaFX. Carrega telaInicial.fxml
    *         e exibe a janela principal.
    * Parametros: @param stage janela principal fornecida pelo JavaFX
    * Retorno: void
    *************************************************************** */
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/TelaInicial.fxml"));

        Scene scene = new Scene(root, 1280, 720);
        stage.setTitle("O Diario Concorrente");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    } // Fim do metodo start

    /* ***************************************************************
    * Metodo: main
    * Funcao: Ponto de entrada da aplicacao.
    * Parametros: @param args argumentos da linha de comando
    * Retorno: void
    *************************************************************** */
    public static void main(String[] args) {
        launch(args);
    } // Fim do metodo main

} // Fim da classe Principal
