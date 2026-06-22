/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 22/06/2026
* Ultima alteracao.: 22/06/2026
* Nome.............: TelaInicialController.java
* Funcao...........: Controller da tela (fxml) telaInicial que coordena 
*                    os eventos.
************************************************************************ */

package controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TelaInicialController {
    
    /* ***************************************************************
    * Metodo: iniciarJogo
    * Funcao: Responsavel por redirecionar o usuario a tela de simulacao
    *         do Transito Automato.
    * Parametros: @param event evento de clique
    * Retorno: sem retorno
    *************************************************************** */
    @FXML
    private void iniciarTransito(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(
            getClass().getResource("/view/Simulacao.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        Scene scene = new Scene(root);

        stage.setScene(scene);

        stage.show();
    } // Fim do metodo iniciarTransito    

}
