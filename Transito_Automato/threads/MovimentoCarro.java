package threads;

import model.Carro;

@FunctionalInterface
public interface MovimentoCarro {
    void mover(Carro carro, long duracaoMs) throws InterruptedException;
}
