/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 22/06/2026
* Ultima alteracao.: 22/06/2026
* Nome.............: Principal.java
* Funcao...........: Ponto de entrada da aplicacao JavaFX.
*                    Carrega a tela inicial (TelaInicial.fxml) e
*                    configura o fechamento correto da aplicacao.
************************************************************************ */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import controller.*;

@SuppressWarnings("unused")
public class Principal extends Application {

  private static MediaPlayer musicaFundo;
  private static Clip musicaFundoWav;
    
  /* ***************************************************************
  * Metodo: start
  * Funcao: Metodo de entrada do JavaFX. Carrega TelaInicial.fxml
  *         e exibe a janela principal.
  * Parametros: @param stage janela principal fornecida pelo JavaFX
  * Retorno: void
  *************************************************************** */
  @Override
  public void start(Stage stage) throws Exception {

    Parent root = FXMLLoader.load(
        getClass().getResource("/view/TelaInicial.fxml")
    );

    stage.setTitle("Grande Transito Automato");
    stage.setScene(new Scene(root));
    stage.setResizable(false);

    // Encerra JVM e threads daemon ao fechar a janela
    stage.setOnCloseRequest(e -> {
      if (musicaFundo != null) {
        musicaFundo.stop();
      }
      if (musicaFundoWav != null) {
        musicaFundoWav.stop();
        musicaFundoWav.close();
      }
      Platform.exit();
      System.exit(0);
    });

    iniciarMusicaFundo();

    stage.show();

  } // Fim do metodo start

  /* ***************************************************************
  * Metodo: iniciarMusicaFundo
  * Funcao: Carrega e toca em loop a trilha sonora da pasta sound.
  *         Tenta MP3 primeiro; se o JavaFX Media nao conseguir criar o
  *         player neste ambiente, usa o WAV como fallback.
  * Parametros: nenhum
  * Retorno: sem retorno
  *************************************************************** */
  private void iniciarMusicaFundo() {
    if (musicaFundo != null || musicaFundoWav != null) {
      return;
    }

    if (tentarTocarMp3()) {
      return;
    }

    tocarWavFallback();
  }

  private boolean tentarTocarMp3() {
    try {
      var recursoMusica = getClass().getResource(
          "/sound/GTA_San_Andreas_Main_Theme.mp3"
      );

      if (recursoMusica == null) {
        return false;
      }

      Media media = new Media(recursoMusica.toExternalForm());
      musicaFundo = new MediaPlayer(media);
      musicaFundo.setCycleCount(MediaPlayer.INDEFINITE);
      musicaFundo.setVolume(0.35);
      musicaFundo.play();
      return true;
    } catch (RuntimeException e) {
      musicaFundo = null;
      return false;
    }
  }

  private void tocarWavFallback() {
    try {
      var recursoMusica = getClass().getResource(
          "/sound/GTA_San_Andreas_Main_Theme.wav"
      );

      if (recursoMusica == null) {
        return;
      }

      AudioInputStream audio = AudioSystem.getAudioInputStream(recursoMusica);
      musicaFundoWav = AudioSystem.getClip();
      musicaFundoWav.open(audio);

      if (musicaFundoWav.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        FloatControl volume = (FloatControl) musicaFundoWav.getControl(
            FloatControl.Type.MASTER_GAIN
        );
        volume.setValue(-12.0f);
      }

      musicaFundoWav.loop(Clip.LOOP_CONTINUOUSLY);
      musicaFundoWav.start();
    } catch (Exception e) {
      musicaFundoWav = null;
    }
  }

  /* ***************************************************************
  * Metodo: main
  * Funcao: Ponto de entrada da aplicacao.
  * Parametros: @param args argumentos da linha de comando
  * Retorno: void
  *************************************************************** */
  public static void main(String[] args) {
    launch(args);
  } // Fim do metodo main

}
