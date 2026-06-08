/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 08/06/2026
* Ultima alteracao.: 08/06/2026
* Nome.............: TelaInicialController.java
* Funcao...........: Controller da tela (fxml) TelaInicial que coordena 
*                    os eventos.
************************************************************************ */

package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

/* ***************************************************************
* Classe: TelaInicialController
* Funcao: Controla o comportamento da tela inicial do JavaFX, 
*         conectando os botoes e eventos a logica do programa.
*************************************************************** */
public class TelaInicialController {

    /* ***************************************************************
    * Metodo: handleIniciar
    * Funcao: Responsavel por redirecionar o usuario a tela de simulacao
    *         dos Leitores/Escritores.
    * Parametros: @param event evento de clique
    * Retorno: sem retorno
    *************************************************************** */
    @FXML
    private void handleIniciar(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Simulacao.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1280, 720);
            stage.setScene(scene);
            stage.setTitle("O Diario Concorrente - Simulacao");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    } // Fim do metodo iniciarJogo

} // Fim da classe TelaInicialController
