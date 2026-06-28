/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 21/06/2026
* Ultima alteracao.: 28/06/2026
* Nome.............: GerenciadorSemaforos.java
* Funcao...........: Cria e centraliza os Semaphore(1) de cada REGIAO
*                    CRITICA da malha, seguindo as 57 RCs descritas em
*                    regioes_criticas_transito_automato.txt. Cada RC
*                    ganha exatamente UM semaforo, devolvido sempre pela
*                    mesma instancia para quem perguntar pelo mesmo nome.
*
*                    Tambem oferece reiniciarTodos(), usado pelo RESET,
*                    para garantir que nenhuma zona fique com um
*                    semaforo "presa" (permits != 1) apos um reinicio.
************************************************************************ */

package util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class GerenciadorSemaforos {

    private final Map<String, Semaphore> semaforos = new LinkedHashMap<>();

    public GerenciadorSemaforos() {
        for (String nomeRegiao : Constantes.NOMES_REGIOES_CRITICAS) {
            // Semaphore(1) = no maximo 1 carro por vez dentro da RC (exclusao mutua)
            semaforos.put(nomeRegiao, new Semaphore(1, true)); // fair=true: respeita ordem de chegada
        }
    }

    /* ***************************************************************
    * Metodo: getSemaforo
    * Funcao: Devolve o Semaphore associado a uma regiao critica.
    * Parametros: @param nomeRegiao nome da RC (ex: "RC_01"), ou null
    * Retorno: @return Semaphore correspondente, ou null se nomeRegiao for
    *          null (trecho de uso exclusivo, sem regiao critica)
    *************************************************************** */
    public Semaphore getSemaforo(String nomeRegiao) {
        if (nomeRegiao == null) {
            return null;
        }
        return semaforos.get(nomeRegiao);
    }

    public boolean ehRegiaoValida(String nomeRegiao) {
        return nomeRegiao != null && semaforos.containsKey(nomeRegiao);
    }

    /* ***************************************************************
    * Metodo: reiniciarTodos
    * Funcao: Restaura todos os semaforos ao estado inicial (1 permissao
    *         livre cada), descartando qualquer permissao que tenha
    *         ficado retida por um carro no momento do RESET.
    * Parametros: nenhum
    * Retorno: sem retorno
    *************************************************************** */
    public void reiniciarTodos() {
        for (Map.Entry<String, Semaphore> entrada : semaforos.entrySet()) {
            Semaphore s = entrada.getValue();
            // drena qualquer permissao "extra" e garante exatamente 1 disponivel
            s.drainPermits();
            s.release();
        }
    }
}
