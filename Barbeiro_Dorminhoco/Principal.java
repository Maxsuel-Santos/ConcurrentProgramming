/* *********************************************************************
* Autor............: Arthur Leite Felix
* Matricula........: 202511527
* Inicio...........: 12/06/2026
* Ultima alteracao.: 20/06/2026
* Nome.............: Principal.java
* Funcao...........: Inicializar a aplicacao JavaFX e gerenciar o carregamento das interfaces e threads principais
************************************************************************ */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import controller.ControllerBarbearia;
import controller.ControllerMenu;
import model.Barbearia;
import model.Barbeiro;
import model.GeradorClientes;

import java.net.URL;

public class Principal extends Application {

    /* *********************************************************************
    * Nome.............: start
    * Funcao...........: Configura o palco principal, carrega as views FXML, inicializa o modelo e dispara as threads
    ************************************************************************ */
    @Override
    public void start(Stage primaryStage) {
        try {
            URL fxmlBarbearia = getClass().getResource("/view/Barbearia.fxml");
            if (fxmlBarbearia == null)
                fxmlBarbearia = getClass().getResource("view/Barbearia.fxml");

            FXMLLoader loaderBarbearia = new FXMLLoader(fxmlBarbearia);
            Parent rootBarbearia = loaderBarbearia.load();
            ControllerBarbearia ctrlBarbearia = loaderBarbearia.getController();

            URL fxmlMenu = getClass().getResource("/view/Menu.fxml");
            if (fxmlMenu == null)
                fxmlMenu = getClass().getResource("view/Menu.fxml");

            FXMLLoader loaderMenu = new FXMLLoader(fxmlMenu);
            Parent rootMenu = loaderMenu.load();
            ControllerMenu ctrlMenu = loaderMenu.getController();

            Barbearia barbearia = new Barbearia(ctrlBarbearia);
            ctrlBarbearia.setBarbearia(barbearia);

            Barbeiro barbeiro = new Barbeiro(barbearia);
            GeradorClientes gerador = new GeradorClientes(barbearia, ctrlBarbearia,
                                                          ctrlMenu.getSliderClientes());

            ctrlMenu.setRefs(barbearia, barbeiro, gerador, ctrlBarbearia);

            primaryStage.setTitle("A Barbearia dos Vilões - MrCutt");
            primaryStage.setScene(new Scene(rootBarbearia));
            primaryStage.setResizable(false);
            primaryStage.setOnCloseRequest(e -> System.exit(0));
            primaryStage.show();

            Stage menuStage = new Stage();
            menuStage.setTitle("Painel de Controle");
            menuStage.setScene(new Scene(rootMenu));
            menuStage.setResizable(false);
            menuStage.setOnCloseRequest(e -> System.exit(0));
            menuStage.show();

            barbeiro.start();
            gerador.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* *********************************************************************
    * Nome.............: main
    * Funcao...........: Ponto de entrada da aplicacao; delega a inicializacao ao JavaFX via launch
    ************************************************************************ */
    public static void main(String[] args) {
        launch(args);
    }
}