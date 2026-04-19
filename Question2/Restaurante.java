import java.util.Random;
import java.util.concurrent.Semaphore;

public class Restaurante {
    private static final int TOTAL_MESAS = 20;
    private static final int TOTAL_ALUNOS = 30;

    private static final Semaphore mesas = new Semaphore(TOTAL_MESAS, true);
    private static final Random random = new Random();

    private static class Aluno implements Runnable {
        private final int id;

        Aluno(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            System.out.println("Aluno " + id + " chegou no restaurante.");
            try {
                mesas.acquire();
                System.out.println("Aluno " + id + " sentou na mesa.");

                // Simula tempo de refeicao
                Thread.sleep(300 + random.nextInt(700));

                System.out.println("Aluno " + id + " liberou a mesa.");
                mesas.release();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        Thread[] alunos = new Thread[TOTAL_ALUNOS];

        for (int i = 0; i < TOTAL_ALUNOS; i++) {
            alunos[i] = new Thread(new Aluno(i + 1));
            alunos[i].start();
        }

        for (int i = 0; i < TOTAL_ALUNOS; i++) {
            try {
                alunos[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("Todos os alunos terminaram de comer.");
    }
}
