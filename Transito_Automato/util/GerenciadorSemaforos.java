/* *********************************************************************
* Autor............: Maxsuel Aparecido Lima Santos
* Matricula........: 202511587
* Inicio...........: 21/06/2026
* Ultima alteracao.: 23/06/2026
* Nome.............: GerenciadorSemaforos.java
* Funcao...........: Cria e centraliza os Semaphore(1) de cada trecho
*                    compartilhado da malha (regiao critica). Cada
*                    trecho RHxx/RVxx que e' usado por 2 ou mais carros
*                    ganha exatamente UM semaforo, devolvido sempre pela
*                    mesma instancia para quem perguntar pelo mesmo
*                    nome - e' isso que garante exclusao mutua entre os
*                    carros que disputam aquele pedaco de rua.
*
*                    Tambem oferece reiniciarTodos(), usado pelo RESET,
*                    para garantir que nenhum carro fique com um
*                    semaforo "presa" (permits != 1) apos um reinicio.
************************************************************************ */

package util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class GerenciadorSemaforos {

    private final Map<String, Semaphore> semaforos = new LinkedHashMap<>();

    public GerenciadorSemaforos() {
        for (String trecho : Constantes.TRECHOS_COMPARTILHADOS) {
            // Semaphore(1) = no maximo 1 carro por vez no trecho (exclusao mutua)
            semaforos.put(trecho, new Semaphore(1, true)); // fair=true: respeita ordem de chegada
        }
    }

    /* ***************************************************************
    * Metodo: getSemaforo
    * Funcao: Devolve o Semaphore associado a um trecho da malha.
    * Parametros: @param nomeTrecho nome do trecho (ex: "RH01")
    * Retorno: @return Semaphore correspondente, ou null se o trecho
    *          nao for uma regiao critica (uso exclusivo de um carro)
    *************************************************************** */
    public Semaphore getSemaforo(String nomeTrecho) {
        return semaforos.get(nomeTrecho);
    }

    public boolean ehRegiaoCritica(String nomeTrecho) {
        return semaforos.containsKey(nomeTrecho);
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
